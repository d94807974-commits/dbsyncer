/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * RSAConfigure
 *
 * @version 1.0.0
 * @Author The clouds.
 * @Date 2026-01-15 08:30
 */
public class RsaConfig {

    private final List<RsaVersion> rsaVersions = new ArrayList<>();

    public List<RsaVersion> getRsaVersions() {
        return rsaVersions;
    }
}