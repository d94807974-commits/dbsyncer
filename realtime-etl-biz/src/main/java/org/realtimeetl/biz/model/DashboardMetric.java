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
package org.realtimeetl.biz.model;

import org.realtimeetl.biz.vo.SyncTrendStackVO;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2025-12-16 23:59
 */
public final class DashboardMetric extends MappingReportMetric {

    /**
     * Total number of tasks
     */
    private long totalMeta;

    /**
     * Last week.TasksNumber
     */
    private long lastWeekMeta;

    /**
     * RunTasksNumber
     */
    private long runningMeta;

    /**
     * Failed TaskNumber
     */
    private long failMeta;

    /**
     * Sync TrendsData Fact
     */
    private SyncTrendStackVO trend = new SyncTrendStackVO();

    public long getTotalMeta() {
        return totalMeta;
    }

    public void setTotalMeta(long totalMeta) {
        this.totalMeta = totalMeta;
    }

    public long getLastWeekMeta() {
        return lastWeekMeta;
    }

    public void setLastWeekMeta(long lastWeekMeta) {
        this.lastWeekMeta = lastWeekMeta;
    }

    public long getRunningMeta() {
        return runningMeta;
    }

    public void setRunningMeta(long runningMeta) {
        this.runningMeta = runningMeta;
    }

    public long getFailMeta() {
        return failMeta;
    }

    public void setFailMeta(long failMeta) {
        this.failMeta = failMeta;
    }

    public SyncTrendStackVO getTrend() {
        return trend;
    }

    public void setTrend(SyncTrendStackVO trend) {
        this.trend = trend;
    }

    public void reset() {
        super.reset();
        this.totalMeta = 0;
        this.lastWeekMeta = 0;
        this.runningMeta = 0;
        this.failMeta = 0;
        trend = new SyncTrendStackVO();
    }
}
