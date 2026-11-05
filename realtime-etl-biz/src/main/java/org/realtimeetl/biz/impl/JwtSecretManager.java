/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.model.TokenInfo;
import org.realtimeetl.biz.util.JwtUtil;
import org.realtimeetl.common.model.JwtSecretConfig;
import org.realtimeetl.common.model.JwtSecretVersion;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.model.SystemConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * JWTKey Manager (service signing key management)
 * <p>
 * ResponsibleJWTKeyGenerate、Storage, acquisition and rotation.
 * Supports multiple historical keys for smooth rotation.
 * </p>
 *
 * <h3>and ApiKeyManager Between:</h3>
 * <table border="1">
 *   <tr><th>Dimensions</th><th>JwtSecretManager</th><th>ApiKeyManager</th></tr>
 *   <tr><td>Duties</td><td>OrganisationAuthorizationVisits)</td><td>Client identification ExerciseWho are you?）</td></tr>
 *   <tr><td>Key Holder</td><td>Service Internal</td><td>Client holds</td></tr>
 *   <tr><td>Use scene</td><td>GenerateAnd authenticationJWT Token</td><td>LoginOrganisation</td></tr>
 *   <tr><td>Key Format</td><td>Base64Encoding (for signature)</td><td>SHA1Hash storage (irreversible)</td></tr>
 *   <tr><td>Life cycle</td><td>Whatever.TokenExpired rotation</td><td>Long-term effectiveness</td></tr>
 * </table>
 *
 * <h3>Accreditation process:</h3>
 * <pre>
 * 1. ClientSubmit API Key（secret）
 * 2. ApiKeyManager.validate() Authentication
 * 3. After validation,JwtSecretManager Generate JWT Token
 * 4. Follow Certainly TransferJWT Token，ByJwtSecretManager Authentication
 * </pre>
 *
 * @author The clouds.
 * @version 2.0.0
 * @see ApiKeyManager
 */
@Component
public class JwtSecretManager {

    private static final Logger logger = LoggerFactory.getLogger(JwtSecretManager.class);

    @Resource
    private SystemConfigProfile systemConfigProfile;

    /**
     * CompensationLength（bytes)
     */
    private static final int DEFAULT_SECRET_LENGTH = 32;

    /**
     * Default maximum number of historical keys retained
     */
    private static final int DEFAULT_MAX_VERSION_SIZE = 3;

    /**
     * Get CurrentJWTKey (forGenerateNewToken）
     * If the key does not exist, automaticGenerate
     * 
     * @return JWTKey
     */
    public String generateToken() throws NoSuchAlgorithmException, InvalidKeyException {
        JwtSecretConfig config = getJwtSecretConfig();
        List<JwtSecretVersion> secrets = config.getSecrets();
        String secret = CollectionUtils.isEmpty(secrets) ? StringUtil.EMPTY : secrets.get(secrets.size() - 1).getSecret();
        return JwtUtil.generateToken(secret);
    }

    /**
     * AccessJWTKey (support key rotation)
     *
     * @param token Current Usetoken
     * @return GenerateNewJWTKey
     */
    public String refreshToken(String token) throws NoSuchAlgorithmException, InvalidKeyException {
        JwtSecretConfig config = getJwtSecretConfig();
        // Verify from the latest key
        for (int i = config.getSecrets().size() - 1; i >= 0; i--) {
            JwtSecretVersion version = config.getSecrets().get(i);
            String newToken = JwtUtil.refreshToken(token, version.getSecret());
            if (newToken != null) {
                return newToken;
            }
        }
        return null;
    }

    /**
     * Verify INTRODUCTIONJWTKey (support key rotation)
     *
     * @param token Current Usetoken
     * @return Verify INTRODUCTIONResulttrue/false
     */
    public boolean verifyToken(String token) throws NoSuchAlgorithmException, InvalidKeyException {
        if (StringUtil.isBlank(token)) {
            return false;
        }
        JwtSecretConfig config = getJwtSecretConfig();
        // Verify from the latest key
        for (int i = config.getSecrets().size() - 1; i >= 0; i--) {
            JwtSecretVersion version = config.getSecrets().get(i);
            TokenInfo tokenInfo = JwtUtil.verifyToken(token, version.getSecret());
            if (tokenInfo != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sign any payload.
     *
     * @param payload Load
     * @return JWT
     */
    public String signPayload(Object payload) throws NoSuchAlgorithmException, InvalidKeyException {
        JwtSecretConfig config = getJwtSecretConfig();
        List<JwtSecretVersion> secrets = config.getSecrets();
        Assert.isTrue(!CollectionUtils.isEmpty(secrets), "JWT Key Not Initialized");
        String secret = secrets.get(secrets.size() - 1).getSecret();
        return JwtUtil.signPayload(payload, secret);
    }

    /**
     * Sign and parse load (support key rotation; do not verify expiry).
     *
     * @param token JWT
     * @param type  LoadType
     * @param <T>   LoadType
     * @return Load,FailedBack null
     */
    public <T> T verifyPayload(String token, Class<T> type) throws NoSuchAlgorithmException, InvalidKeyException {
        if (StringUtil.isBlank(token) || type == null) {
            return null;
        }
        JwtSecretConfig config = getJwtSecretConfig();
        for (int i = config.getSecrets().size() - 1; i >= 0; i--) {
            JwtSecretVersion version = config.getSecrets().get(i);
            T payload = JwtUtil.verifyPayload(token, version.getSecret(), type);
            if (payload != null) {
                return payload;
            }
        }
        return null;
    }

    /**
     * 从系统配置中获取JWT密钥配置
     *
     * @return JWTKeyConfigure，If not presentBacknull
     */
    private JwtSecretConfig getJwtSecretConfig() {
        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        Assert.notNull(systemConfig, "System of service not available, please try again");
        JwtSecretConfig config = systemConfig.getJwtSecretConfig();
        if (config == null || CollectionUtils.isEmpty(config.getSecrets())) {
            generateAndSaveSecret(systemConfig);
            config = systemConfig.getJwtSecretConfig();
        }
        return config;
    }

    /**
     * GenerateNewJWTKey PlusSave
     * ♪ If an old key exists ♪AddTo History KeyMap, smooth rotation
     * The old historical key is automatically cleared, only the nearestNindividualVersion
     */
    private synchronized void generateAndSaveSecret(SystemConfig systemConfig) {
        try {
            JwtSecretConfig config = systemConfig.getJwtSecretConfig();
            if (config == null) {
                config = new JwtSecretConfig();
            }
            List<JwtSecretVersion> versions = config.getSecrets();

            // Calculate newVersionNo. No.
            int newVersion = 1;
            if (!versions.isEmpty()) {
                int maxVersion = versions.stream().mapToInt(JwtSecretVersion::getVersion).max().orElse(0);
                newVersion = maxVersion + 1;
            }

            // GenerateNew Key
            JwtSecretVersion newVersionObj = new JwtSecretVersion();
            newVersionObj.setSecret(generateSecret());
            newVersionObj.setVersion(newVersion);
            newVersionObj.setCreateTime(Instant.now().toEpochMilli());
            newVersionObj.setEnabled(true);
            versions.add(newVersionObj);

            // Cleared old historical keys, only recentNindividualVersion
            cleanupOldVersions(versions);

            // SavePresent.System settings
            systemConfig.setJwtSecretConfig(config);
            systemConfigProfile.saveSystemConfig(systemConfig);
            logger.info("Generate new JWT keys successfully, version: {, number of historical keys: {}", newVersion, versions.size());
        } catch (Exception e) {
            logger.error("Failed to generate and save JWT keys", e);
            throw new BizException("Failed to generate and save JWT keys", e);
        }
    }

    /**
     * Cleaning old keysVersion，Keep only lastNindividualVersion
     *
     * @param versions CertificateConfigure List
     */
    private void cleanupOldVersions(List<JwtSecretVersion> versions) {
        // IfVersionThe number is beyond the limit.DeleteThe oldest.Version
        if (versions.size() > DEFAULT_MAX_VERSION_SIZE) {
            // DeleteThe oldest.Version
            JwtSecretVersion remove = versions.remove(0);
            logger.info("Cleans up old JWT keys, secret: }, version: }", remove.getSecret(), remove.getVersion());
        }
    }

    /**
     * GenerateWith Secret Key
     *
     * @return Base64Encoding Key
     */
    private String generateSecret() {
        try {
            SecureRandom secureRandom = new SecureRandom();
            byte[] secretBytes = new byte[DEFAULT_SECRET_LENGTH];
            secureRandom.nextBytes(secretBytes);
            return Base64.getEncoder().encodeToString(secretBytes);
        } catch (Exception e) {
            logger.error("Failed to generate with secret key", e);
            throw new RuntimeException("Failed to generate with secret key", e);
        }
    }

}
