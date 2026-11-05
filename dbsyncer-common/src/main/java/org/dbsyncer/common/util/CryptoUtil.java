/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.util;

import org.dbsyncer.common.model.AesData;
import org.dbsyncer.common.model.OpenApiData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * Encryption Tool Class
 * SupportAES-GCMEncrypt andRSAKey Exchange
 * Takes note of the signing algorithm that supports the public/intranet scene
 *
 * @author AE86
 * @version 2.0.0
 */
public final class CryptoUtil {

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtil.class);

    // AES-GCMConfigure
    private static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 16;
    private static final int GCM_IV_LENGTH = 12;

    /**
     * GenerateAESKey pair
     */
    public static String generateAESKeyPair() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            keyGenerator.init(AES_KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();

            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            logger.error("Failed to generate AES keys", e);
            throw new RuntimeException("Failed to generate AES keys", e);
        }
    }

    /**
     * AES-GCMEncrypt data
     */
    public static AesData encryptData(String data, String aesKey) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[GCM_IV_LENGTH];
            random.nextBytes(iv);

            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(aesKey), KEY_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            AesData aesData = new AesData();
            aesData.setEncryptedData(Base64.getEncoder().encodeToString(encryptedData));
            aesData.setIv(Base64.getEncoder().encodeToString(iv));

            return aesData;
        } catch (Exception e) {
            logger.error("Failed to encrypt AES data", e);
            throw new RuntimeException("Failed to encrypt AES data", e);
        }
    }

    /**
     * AES-GCMDecrypt Data
     */
    public static String decryptData(String encryptedData, String iv, String aesKey) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(aesKey), KEY_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, Base64.getDecoder().decode(iv));

            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmParameterSpec);

            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Could not close temporary folder: %s", e);
            throw new RuntimeException("Could not close temporary folder: %s", e);
        }
    }

    /**
     * Build Encrypted Transfer Request
     *
     * @param data            Data to Encrypt
     * @param privateKey      Private Combined Key
     * @param hmacSecret      HMAC密钥（用于签名，内网场景必需）
     * @param isPublicNetwork YesNoIt's a public view.true-Internet useRSA-SHA256Signature,false-Intranet useHMAC-SHA256English
     * @return Encrypted RequestJSONString
     */
    public static OpenApiData encryptResponse(String data, RSAPrivateKey privateKey, String hmacSecret, boolean isPublicNetwork) {
        // 1. GenerateTemporaryAESKey
        String aesKey = generateAESKeyPair();

        // 2. AESEncrypt data
        AesData aesData = encryptData(data, aesKey);

        // 3. RSAEncryptionAESKey
        String encryptedAESKey = RSAUtil.privateEncrypt(aesKey, privateKey);

        // 4. Build Transfer Structure
        OpenApiData request = new OpenApiData();
        request.setTimestamp(System.currentTimeMillis());
        request.setNonce(CryptoUtil.generateNonce());
        request.setEncryptedKey(encryptedAESKey);
        request.setEncryptedData(aesData.getEncryptedData());
        request.setIv(aesData.getIv());

        // 5. GenerateSignature (selection of algorithms according to scene)
        String dataToSign = buildUnifiedSignData(request.getEncryptedData(), request.getTimestamp(), request.getNonce());
        String signature = isPublicNetwork ? RSAUtil.signSHA256(dataToSign, privateKey) : hmacSha256Sign(dataToSign, hmacSecret);
        logger.info("Signature:", signature);
        request.setSignature(signature);
        return request;
    }

    /**
     * Parsing Encrypted Transfer Request - Service User
     *
     * @param data            Encrypt data
     * @param publicKey       Public Key
     * @param hmacSecret      HMACKeys (for authentication of signatures, internal content required)
     * @param isPublicNetwork YesNoTo the public view.
     * @return Decrypt Data
     */
    public static String decryptRequest(OpenApiData data, RSAPublicKey publicKey, String hmacSecret, boolean isPublicNetwork) {
        String signature = data.getSignature();
        if (signature == null || signature.isEmpty()) {
            throw new RuntimeException("Signature cannot be empty");
        }

        // 1. Authenticate signature
        String dataToVerify = buildUnifiedSignData(data.getEncryptedData(), data.getTimestamp(), data.getNonce());
        boolean verifySignature = isPublicNetwork ? RSAUtil.verifySHA256(dataToVerify, signature, publicKey) : signature.equals(hmacSha256Sign(dataToVerify, hmacSecret));
        if (!verifySignature) {
            throw new RuntimeException("Signature authentication failed");
        }

        String iv = data.getIv();
        if (iv == null || iv.isEmpty()) {
            throw new RuntimeException("IV cannot be empty.");
        }
        // 2. RSADecryptAESKey
        String aesKey = RSAUtil.publicDecrypt(data.getEncryptedKey(), publicKey);

        // 3. AESDecrypt Data
        return decryptData(data.getEncryptedData(), iv, aesKey);
    }

    /**
     * Uniform signature string construction method
     * Encryption and reconciliation must use the same method.
     */
    private static String buildUnifiedSignData(String encryptedData, long timestamp, String nonce) {
        return String.format("%s|%d|%s", encryptedData, timestamp, nonce);
    }

    /**
     * GenerateRandomnonce
     */
    private static String generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }

    /**
     * HMAC-SHA256Signature (inline scene)
     */
    private static String hmacSha256Sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            logger.error("HMAC-SHA256 signature failed", e);
            throw new RuntimeException("HMAC-SHA256 signature failed", e);
        }
    }

}
