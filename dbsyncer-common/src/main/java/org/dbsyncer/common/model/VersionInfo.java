/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.model;

/**
 * VersionInfo
 *
 * @version 1.0.0
 * @Author The clouds.
 * @Date 2026-01-22 00:30
 */
public final class VersionInfo {

    /**
     * LargeVersion-SmallVersion-Number-year-month-day-yearNo.
     */
    private long version;

    private String appName;

    private long createTime;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
