package org.dbsyncer.common.util;

import org.dbsyncer.common.model.RsaVersion;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import javax.crypto.Cipher;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtil {

    public static final String CHARSET = "UTF-8";

    public static final String RSA_ALGORITHM = "RSA";

    public static RsaVersion createKeys(int keySize) {
        // YesRSAAlgorithm CreateKeyPairGeneratorObject
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm-->[" + RSA_ALGORITHM + "]");
        }

        // InitializeKeyPairGeneratorObject,KeyLength
        kpg.initialize(keySize);
        // GenerateKeys.
        KeyPair keyPair = kpg.generateKeyPair();
        // Get Bermuda
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = Base64.encodeBase64URLSafeString(publicKey.getEncoded());
        // Get Private Keys
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = Base64.encodeBase64URLSafeString(privateKey.getEncoded());
        RsaVersion rsaConfig = new RsaVersion();
        rsaConfig.setKeyLength(keySize);
        rsaConfig.setPublicKey(publicKeyStr);
        rsaConfig.setPrivateKey(privateKeyStr);
        return rsaConfig;
    }

    /**
     * Get Bermuda
     *
     * @param publicKey Key Stringbase64Encoding)
     * @throws Exception
     */
    public static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Based on the pass.X509EncodedKeyCommand to get public key objects
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
        RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        return key;
    }

    /**
     * Get Private Keys
     *
     * @param privateKey Key Stringbase64Encoding)
     * @throws Exception
     */
    public static RSAPrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Based on the pass.PKCS#8EncodedKeyNo No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No No
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
        return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * Public Key Encryption
     */
    public static String publicEncrypt(String data, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), publicKey.getModulus().bitLength()));
        } catch (Exception e) {
            throw new RuntimeException("Encrypt String[s]" + data + "] encountered an exception", e);
        }
    }

    /**
     * Public Key Decrypt
     */
    public static String publicDecrypt(String data, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), publicKey.getModulus().bitLength()), CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("Decrypt String[s]" + data + "] encountered an exception", e);
        }
    }

    /**
     * Private key decryption
     */
    public static String privateDecrypt(String data, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), privateKey.getModulus().bitLength()), CHARSET);
        } catch (Exception e) {
            throw new RuntimeException("Decrypt String[s]" + data + "] encountered an exception", e);
        }
    }

    /**
     * Private Key Encryption
     */
    public static String privateEncrypt(String data, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), privateKey.getModulus().bitLength()));
        } catch (Exception e) {
            throw new RuntimeException("Encrypt String[s]" + data + "] encountered an exception", e);
        }
    }

    /**
     * RSA-SHA256Signature (with private key)
     * 
     * @param data Data to sign
     * @param privateKey RSA private key
     * @return Base64Encoded Signature
     */
    public static String signSHA256(String data, RSAPrivateKey privateKey) {
        try {
            java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(data.getBytes(CHARSET));
            byte[] signBytes = signature.sign();
            return Base64.encodeBase64URLSafeString(signBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign RSA-SHA256", e);
        }
    }

    /**
     * RSA-SHA256Validate signature (validate with public key)
     *
     * @param data Original data
     * @param signature Base64Encoded Signature
     * @param publicKey RSA Public Key
     * @return AuthenticationResult
     */
    public static boolean verifySHA256(String data, String signature, RSAPublicKey publicKey) {
        try {
            java.security.Signature sig = java.security.Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes(CHARSET));
            byte[] signBytes = Base64.decodeBase64(signature);
            return sig.verify(signBytes);
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize) {
        int maxBlock = 0;
        if (opmode == Cipher.DECRYPT_MODE) {
            maxBlock = keySize / 8;
        } else {
            maxBlock = keySize / 8 - 11;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] buff;
        int i = 0;
        try {
            while (datas.length > offSet) {
                if (datas.length - offSet > maxBlock) {
                    buff = cipher.doFinal(datas, offSet, maxBlock);
                } else {
                    buff = cipher.doFinal(datas, offSet, datas.length - offSet);
                }
                out.write(buff, 0, buff.length);
                i++;
                offSet = i * maxBlock;
            }
        } catch (Exception e) {
            throw new RuntimeException("Dec HNP" + maxBlock + "There's been an anomaly in the data.", e);
        }
        byte[] result = out.toByteArray();
        IOUtils.closeQuietly(out);
        return result;
    }

}