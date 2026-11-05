/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.biz.impl;

import org.realtimeetl.common.model.ApiKeyConfig;
import org.realtimeetl.common.model.SecretVersion;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.SHA1Util;
import org.realtimeetl.common.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * API KeyManager (customer voucher management)
 * <p>
 * Responsible for managing external system accessAPI Key（API Key），ForOpenAPIIdentification.
 * SupportINTERVersionKey, smooth rotation.
 * </p>
 *
 * <h3>and JwtSecretManager Between:</h3>
 * <table border="1">
 *   <tr><th>Dimensions</th><th>ApiKeyManager</th><th>JwtSecretManager</th></tr>
 *   <tr><td>Duties</td><td>Client identification ExerciseWho are you?）</td><td>OrganisationAuthorizationVisits)</td></tr>
 *   <tr><td>Key Holder</td><td>Client holds</td><td>Service Internal</td></tr>
 *   <tr><td>Use scene</td><td>LoginOrganisation</td><td>GenerateAnd authenticationJWT Token</td></tr>
 *   <tr><td>Key Format</td><td>SHA1Hash storage (irreversible)</td><td>Base64Encoding (for signature)</td></tr>
 *   <tr><td>Life cycle</td><td>Long-term effectiveness</td><td>Whatever.TokenExpired rotation</td></tr>
 * </table>
 *
 * <h3>Accreditation process:</h3>
 * <pre>
 * 1. ClientSubmit API Key（secret）
 * 2. ApiKeyManager.validateCredential() Authentication
 * 3. After validation,JwtSecretManager Generate JWT Token
 * 4. Follow Certainly TransferJWT Token，ByJwtSecretManager Authentication
 * </pre>
 *
 * @author Realtime ETL.
 * @version 2.0.0
 * @see JwtSecretManager
 */
@Component
public class ApiKeyManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default Max Save KeyVersionNumber
     */
    private static final int DEFAULT_MAX_VERSION_SIZE = 3;

    /**
     * AuthenticationAPI Key
     * SupportINTERVersionKey, pressVersionNo. Administratively high to low
     *
     * @param config CurrentConfigure
     * @param secret API Key（Original Suggested Key.
     * @return AuthenticationYesNoBased on the pass.
     */
    public boolean validate(ApiKeyConfig config, String secret) {
        Assert.hasText(secret, "secret is empty");
        if (config == null || CollectionUtils.isEmpty(config.getSecrets())) {
            logger.warn("addenda API key configuration not enabled");
            return false;
        }

        // Hash processing of input keys
        String hashedSecret = SHA1Util.b64_sha1(secret);

        // PressVersionNo. From high to low, try the latest.Version
        for (int i = config.getSecrets().size() - 1; i >= 0; i--) {
            SecretVersion version = config.getSecrets().get(i);
            if (!version.isEnabled()) {
                continue;
            }
            if (StringUtil.equals(version.getHashedSecret(), hashedSecret)) {
                return true;
            }
        }
        return false;
    }

    /**
     * AddAPI Key
     * Create new if existingVersionKeys, keep oldVersionFor validation
     *
     * @param config CurrentConfigure
     * @param secret API Key（Original key, automatically stored by Hashi)
     */
    public synchronized ApiKeyConfig addCredential(ApiKeyConfig config, String secret) {
        Assert.hasText(secret, "secret is empty");

        if (config == null) {
            config = new ApiKeyConfig();
        }

        // Get Existing KeysVersionList
        List<SecretVersion> versions = config.getSecrets();

        // Hash treatment of keys
        String hashedSecret = SHA1Util.b64_sha1(secret);

        // Exists
        Optional<SecretVersion> exist = versions.stream().filter(v->StringUtil.equals(v.getHashedSecret(), hashedSecret)).findFirst();
        if (exist.isPresent()) {
            return config;
        }

        // Calculate newVersionNo. No.
        int newVersion = 1;
        if (!versions.isEmpty()) {
            int maxVersion = versions.stream().mapToInt(SecretVersion::getVersion).max().orElse(0);
            newVersion = maxVersion + 1;
        }
        // Create NewVersion
        SecretVersion newVersionObj = new SecretVersion();
        newVersionObj.setSecret(secret);
        newVersionObj.setHashedSecret(hashedSecret);
        newVersionObj.setVersion(newVersion);
        newVersionObj.setCreateTime(Instant.now().toEpochMilli());
        newVersionObj.setEnabled(true);
        // AddPresent.VersionList
        versions.add(newVersionObj);

        logger.info("Adding API Key Successful, Version: {", newVersion);
        // Cleaning up old ones.Version
        cleanupOldVersions(versions);

        return config;
    }

    /**
     * Cleaning old keysVersion，Keep only lastNindividualVersion
     *
     * @param versions CertificateConfigure List
     */
    private void cleanupOldVersions(List<SecretVersion> versions) {
        // IfVersionThe number is beyond the limit.DeleteThe oldest.Version
        if (versions.size() > DEFAULT_MAX_VERSION_SIZE) {
            // DeleteThe oldest.Version
            SecretVersion remove = versions.remove(0);
            logger.info("Cleans up old API keys, secret: {, version: }", remove.getSecret(), remove.getVersion());
        }
    }

}
