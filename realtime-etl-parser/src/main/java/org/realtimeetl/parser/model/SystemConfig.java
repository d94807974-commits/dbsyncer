/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.parser.model;

import org.realtimeetl.common.model.ApiKeyConfig;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.IpWhitelistConfig;
import org.realtimeetl.common.model.JwtSecretConfig;
import org.realtimeetl.common.model.RsaConfig;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.model.NoticeConfig;

/**
 * System settings
 *
 * @author AE86
 * @version 1.0.0
 * @date 2020-05-29 20:13
 */
public class SystemConfig extends ConfigModel implements Cloneable {

    public SystemConfig() {
        super.setType(ConfigConstant.SYSTEM);
    }

    /**
     * Sync dataExpiration time（days）
     */
    private int expireDataDays = 30;

    /**
     * System LogExpiration time（days）
     */
    private int expireLogDays = 30;

    /**
     * LogDocumentationExpiration time（days）
     */
    private int expireFileLogDays = 15;

    /**
     * Login failedLockScheduledLongminutes）
     */
    private int loginLockMinutes = 15;

    /**
     * YesNoRecord synchronised successful data（false-Close; true-On）
     */
    private boolean enableStorageWriteSuccess;

    /**
     * YesNoRecording Synchronising Failed Data（false-Close; true-On）
     */
    private boolean enableStorageWriteFail = true;

    /**
     * RecordsSync failedLogMaxLength
     */
    private int maxStorageErrorLength = 8192;

    /**
     * YesNoRecord full volume data（false-Close; true-On）
     */
    private boolean enableStorageWriteFull;

    /**
     * YesNoEnableWatermark
     */
    private boolean enableWatermark;

    /**
     * Watermark Content Income
     */
    private String watermark;

    /**
     * TableExecut MOUUpper CWC
     */
    private int maxBufferActuatorSize = 50;

    /**
     * YesNoCan not open message（false-Close; true-On）
     */
    private boolean enablePrintTraceInfo;

    /**
     * YesNoOpenAPI
     */
    private boolean enableOpenAPI;

    /**
     * RSAConfigure
     */
    private RsaConfig rsaConfig;

    /**
     * JWTKeyConfigure
     */
    private JwtSecretConfig jwtSecretConfig;

    /**
     * API KeyConfigure（Client voucher)
     */
    private ApiKeyConfig apiKeyConfig;

    /**
     * IP whitelistConfigure
     */
    private IpWhitelistConfig ipWhitelistConfig;

    /**
     * AnnouncementsConfigure
     */
    private NoticeConfig noticeConfig;

    /**
     * ?
     */
    private int maxTableCount = 10;

    /**
     * ParallelminNumber of pieces
     */
    private int maxPlanCount = 10;

    /**
     * Total number of segments（andTasks readNum×threadNum The number of slices per wave divided)
     */
    private int shardTotal = 1_000_000;

    /**
     * min(b) Run the piece over time (c)ms）
     */
    private long shardTimeoutMs = 300_000L;

    /**
     * Internal interfaces between cluster nodes shared with key and production cluster nodes must be configured with the same values to prevent undiscovered start-ups。
     */
    private String clusterSecret = "9r+KZ1FNjSpob+shIVXPeuP+u6m2d9z/7RgeY/1vzp0=";

    public int getExpireDataDays() {
        return expireDataDays;
    }

    public void setExpireDataDays(int expireDataDays) {
        this.expireDataDays = expireDataDays;
    }

    public int getExpireLogDays() {
        return expireLogDays;
    }

    public void setExpireLogDays(int expireLogDays) {
        this.expireLogDays = expireLogDays;
    }

    public int getExpireFileLogDays() {
        return expireFileLogDays;
    }

    public void setExpireFileLogDays(int expireFileLogDays) {
        this.expireFileLogDays = expireFileLogDays;
    }

    public int getLoginLockMinutes() {
        return loginLockMinutes;
    }

    public void setLoginLockMinutes(int loginLockMinutes) {
        this.loginLockMinutes = loginLockMinutes;
    }

    public boolean isEnableStorageWriteSuccess() {
        return enableStorageWriteSuccess;
    }

    public void setEnableStorageWriteSuccess(boolean enableStorageWriteSuccess) {
        this.enableStorageWriteSuccess = enableStorageWriteSuccess;
    }

    public boolean isEnableStorageWriteFail() {
        return enableStorageWriteFail;
    }

    public void setEnableStorageWriteFail(boolean enableStorageWriteFail) {
        this.enableStorageWriteFail = enableStorageWriteFail;
    }

    public int getMaxStorageErrorLength() {
        return maxStorageErrorLength;
    }

    public void setMaxStorageErrorLength(int maxStorageErrorLength) {
        this.maxStorageErrorLength = maxStorageErrorLength;
    }

    public boolean isEnableStorageWriteFull() {
        return enableStorageWriteFull;
    }

    public void setEnableStorageWriteFull(boolean enableStorageWriteFull) {
        this.enableStorageWriteFull = enableStorageWriteFull;
    }

    public boolean isEnableWatermark() {
        return enableWatermark;
    }

    public void setEnableWatermark(boolean enableWatermark) {
        this.enableWatermark = enableWatermark;
    }

    public String getWatermark() {
        return watermark;
    }

    public void setWatermark(String watermark) {
        this.watermark = watermark;
    }

    public int getMaxBufferActuatorSize() {
        return maxBufferActuatorSize;
    }

    public void setMaxBufferActuatorSize(int maxBufferActuatorSize) {
        this.maxBufferActuatorSize = maxBufferActuatorSize;
    }

    public boolean isEnablePrintTraceInfo() {
        return enablePrintTraceInfo;
    }

    public void setEnablePrintTraceInfo(boolean enablePrintTraceInfo) {
        this.enablePrintTraceInfo = enablePrintTraceInfo;
    }

    public boolean isEnableOpenAPI() {
        return enableOpenAPI;
    }

    public void setEnableOpenAPI(boolean enableOpenAPI) {
        this.enableOpenAPI = enableOpenAPI;
    }

    public RsaConfig getRsaConfig() {
        return rsaConfig;
    }

    public void setRsaConfig(RsaConfig rsaConfig) {
        this.rsaConfig = rsaConfig;
    }

    public JwtSecretConfig getJwtSecretConfig() {
        return jwtSecretConfig;
    }

    public void setJwtSecretConfig(JwtSecretConfig jwtSecretConfig) {
        this.jwtSecretConfig = jwtSecretConfig;
    }

    public ApiKeyConfig getApiKeyConfig() {
        return apiKeyConfig;
    }

    public void setApiKeyConfig(ApiKeyConfig apiKeyConfig) {
        this.apiKeyConfig = apiKeyConfig;
    }

    public IpWhitelistConfig getIpWhitelistConfig() {
        return ipWhitelistConfig;
    }

    public void setIpWhitelistConfig(IpWhitelistConfig ipWhitelistConfig) {
        this.ipWhitelistConfig = ipWhitelistConfig;
    }

    public NoticeConfig getNoticeConfig() {
        return noticeConfig;
    }

    public void setNoticeConfig(NoticeConfig noticeConfig) {
        this.noticeConfig = noticeConfig;
    }

    public int getMaxTableCount() {
        return maxTableCount;
    }

    public void setMaxTableCount(int maxTableCount) {
        this.maxTableCount = maxTableCount;
    }

    public int getMaxPlanCount() {
        return maxPlanCount;
    }

    public void setMaxPlanCount(int maxPlanCount) {
        this.maxPlanCount = maxPlanCount;
    }

    public int getShardTotal() {
        return shardTotal;
    }

    public void setShardTotal(int shardTotal) {
        this.shardTotal = shardTotal;
    }

    public long getShardTimeoutMs() {
        return shardTimeoutMs;
    }

    public void setShardTimeoutMs(long shardTimeoutMs) {
        this.shardTimeoutMs = shardTimeoutMs;
    }

    public String getClusterSecret() {
        return clusterSecret;
    }

    public void setClusterSecret(String clusterSecret) {
        this.clusterSecret = clusterSecret;
    }

    @Override
    public SystemConfig clone() {
        try {
            return (SystemConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
