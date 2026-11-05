/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * API KeyConfigure（Client voucherConfigure）
 * <p>
 * SupportINTERVersionKey Recommends: Key Management, for smooth rotation.
 * Client uses this key to authenticate, accessJWT Token。
 * </p>
 *
 * @author The clouds.
 * @version 2.0.0
 */
public class ApiKeyConfig {

    /**
     * API KeyThere could be more.Version，Support smooth rotation
     */
    private final List<SecretVersion> secrets = new ArrayList<>();

    public List<SecretVersion> getSecrets() {
        return secrets;
    }
}
