/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * JWTKeyConfigure（Service Signing KeyConfigure）
 * <p>
 * Support keyVersionManages and multiple historical keys for smooth rotation.
 * ForGenerateAnd authenticationJWT Token。
 * </p>
 *
 * @author The clouds.
 * @version 2.0.0
 */
public class JwtSecretConfig {

    /**
     * KeyList（PressVersionNumber storage to verify oldToken）
     */
    private final List<JwtSecretVersion> secrets = new ArrayList<>();

    public List<JwtSecretVersion> getSecrets() {
        return secrets;
    }

}
