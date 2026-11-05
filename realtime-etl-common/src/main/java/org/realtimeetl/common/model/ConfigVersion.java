/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.model;

/**
 * ConfigureVersionInfo
 *
 * @version 1.0.0
 * @Author The clouds.
 * @Date 2026-02-06 08:30
 */
public abstract class ConfigVersion {

    /**
     * VersionNumber (from)1Start Incremental)
     */
    private int version;

    /**
     * Created at（msTimePoke)
     */
    private Long createTime;

    /**
     * YesNoEnable
     */
    private boolean enabled = true;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
