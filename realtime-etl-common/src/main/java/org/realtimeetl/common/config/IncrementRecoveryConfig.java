/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Incremental sync（HamFull + incremental）AutorecoverTry againConfigure。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-14 15:30
 */
@Configuration
@ConfigurationProperties(prefix = "realtime-etl.manager.increment.recovery")
public class IncrementRecoveryConfig {

    /**
     * AutorecoverStart CDC ListenFailedMaximumTry againNumber
     */
    private int retryTimes = 10;

    /**
     * Every timeTry againIntervalms）
     */
    private long retryInterval = 5000L;

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }
}
