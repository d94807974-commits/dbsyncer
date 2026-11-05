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
package org.realtimeetl.common.rsa;

import org.realtimeetl.common.CommonException;
import org.realtimeetl.common.model.OpenApiData;
import org.realtimeetl.common.model.RsaConfig;
import org.realtimeetl.common.model.RsaVersion;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.CryptoUtil;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.RSAUtil;
import org.realtimeetl.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * RSAManager
 *
 * @author Realtime ETL.
 * @version 2.0.0
 * @see RsaManager
 */
@Component
public class RsaManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Default Max Save KeyVersionNumber
     */
    private static final int DEFAULT_MAX_VERSION_SIZE = 3;

    /**
     * DecryptRequest parameters
     * SupportINTERVersionKey, pressVersionNo. Administratively high to low
     *
     * @param config          CurrentConfigure
     * @param data            Encrypt data
     * @param isPublicNetwork Is it a public network?Request
     * @return AuthenticationYesNoBased on the pass.
     */
    public String decrypt(RsaConfig config, OpenApiData data, boolean isPublicNetwork) {
        if (config == null || CollectionUtils.isEmpty(config.getRsaVersions())) {
            throw new CommonException("RSA key configuration not enabled");
        }

        // Try Needs AllEnableKeyVersion
        for (int i = config.getRsaVersions().size() - 1; i >= 0; i--) {
            RsaVersion version = config.getRsaVersions().get(i);
            if (!version.isEnabled()) {
                continue;
            }
            try {
                // Internal: not aloneConfigure hmacSecret , using the currentRSA Public Key AsHMAC KeyandClient agreement allows for signature) and performance effects are negligible.
                // Internet: not usedHMAC，Empty it.
                String hmacSecret = isPublicNetwork ? StringUtil.EMPTY : version.getPublicKey();
                RSAPublicKey publicKey = RSAUtil.getPublicKey(version.getPublicKey());
                return CryptoUtil.decryptRequest(data, publicKey, hmacSecret, isPublicNetwork);
            } catch (Exception e) {
                logger.error("Decrypt failed, version: {", version.getVersion());
            }
        }
        throw new CommonException("Decrypt failed. Key authentication failed.");
    }

    /**
     * Encrypt data
     *
     * @param config          CurrentConfigure
     * @param data            BackData Fact
     * @param isPublicNetwork Is it a public network?Request
     */
    public OpenApiData encrypt(RsaConfig config, Object data, boolean isPublicNetwork) {
        if (config == null || data == null || CollectionUtils.isEmpty(config.getRsaVersions())) {
            return null;
        }
        String encryptedData = JsonUtil.objToJson(data);

        // Try Needs AllEnableKeyVersion
        for (int i = config.getRsaVersions().size() - 1; i >= 0; i--) {
            RsaVersion version = config.getRsaVersions().get(i);
            if (!version.isEnabled()) {
                continue;
            }
            try {
                String hmacSecret = isPublicNetwork ? StringUtil.EMPTY : version.getPublicKey();
                RSAPrivateKey privateKey = RSAUtil.getPrivateKey(version.getPrivateKey());
                return CryptoUtil.encryptResponse(encryptedData, privateKey, hmacSecret, isPublicNetwork);
            } catch (Exception e) {
                logger.error("Encryption failed, version: {", version.getVersion());
            }
        }
        throw new CommonException("Encryption Failed");
    }

    /**
     * AddAPI Key
     * Create new if existingVersionKeys, keep oldVersionFor validation
     *
     * @param config     CurrentConfigure
     * @param publicKey  Public Key
     * @param privateKey Private Combined Key
     * @param keyLength  KeyLength
     * @return NewConfigure
     */
    public synchronized RsaConfig addCredential(RsaConfig config, String publicKey, String privateKey, int keyLength) {
        if (config == null) {
            config = new RsaConfig();
        }

        // Get Existing KeysVersionList
        List<RsaVersion> versions = config.getRsaVersions();

        // Exists
        Optional<RsaVersion> exist = versions.stream().filter(v -> StringUtil.equals(v.getPublicKey(), publicKey) && StringUtil.equals(v.getPrivateKey(), privateKey)).findFirst();
        if (exist.isPresent()) {
            return config;
        }

        // Calculate newVersionNo. No.
        int newVersion = 1;
        if (!versions.isEmpty()) {
            int maxVersion = versions.stream().mapToInt(RsaVersion::getVersion).max().orElse(0);
            newVersion = maxVersion + 1;
        }
        // Create NewVersion
        RsaVersion newVersionObj = new RsaVersion();
        newVersionObj.setPublicKey(publicKey);
        newVersionObj.setPrivateKey(privateKey);
        newVersionObj.setKeyLength(keyLength);
        newVersionObj.setVersion(newVersion);
        newVersionObj.setCreateTime(Instant.now().toEpochMilli());
        newVersionObj.setEnabled(true);
        // AddPresent.VersionList
        versions.add(newVersionObj);

        logger.info("Add RSA key successfully, version: {", newVersion);

        // Cleaning up old ones.Version
        cleanupOldVersions(versions);
        return config;
    }

    /**
     * Cleaning old keysVersion，Keep only lastNindividualVersion
     *
     * @param versions CertificateConfigure List
     */
    private void cleanupOldVersions(List<RsaVersion> versions) {
        // IfVersionThe number is beyond the limit.DeleteThe oldest.Version
        if (versions.size() > DEFAULT_MAX_VERSION_SIZE) {
            // PressVersionNo. Low to High Sort
            versions.sort(Comparator.comparingInt(RsaVersion::getVersion));
            // DeleteThe oldest.Version
            RsaVersion remove = versions.remove(0);
            logger.info("Cleans up old RSA keys, publickey: }, version: }", remove.getPublicKey(), remove.getVersion());
        }
    }

}
