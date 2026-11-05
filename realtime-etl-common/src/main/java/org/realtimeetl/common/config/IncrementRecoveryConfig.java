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
package org.realtimeetl.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Incremental sync（HamFull + incremental）AutorecoverTry againConfigure。
 *
 * @author Realtime ETL
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
