/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.model;

/**
 * JWT KeyVersionInfo（History key to validate oldToken）
 *
 * @author The clouds.
 * @version 2.0.0
 */
public class JwtSecretVersion extends ConfigVersion {

    /**
     * JWT Signing key(s)Base64 Encoding)
     */
    private String secret;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
