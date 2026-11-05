/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.model;

/**
 * KeyVersionInfo
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-02-02 00:04
 */
public class SecretVersion extends ConfigVersion {

    /**
     * Key
     */
    private String secret;

    /**
     * KeyHash.Value（SHA-1After Hash.Base64Encoding)
     */
    private String hashedSecret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getHashedSecret() {
        return hashedSecret;
    }

    public void setHashedSecret(String hashedSecret) {
        this.hashedSecret = hashedSecret;
    }

}
