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

import org.realtimeetl.biz.vo.CpuVO;
import org.realtimeetl.biz.vo.DiskSpaceVO;
import org.realtimeetl.biz.vo.MemoryVO;
import org.realtimeetl.biz.vo.MetricResponseVO;
import org.realtimeetl.biz.vo.TpsVO;

import java.util.List;

public class AppReportMetric {

    /**
     * Pending
     */
    private long queueUp;

    /**
     * LineLength
     */
    private long queueCapacity;

    /**
     * EnduringPending
     */
    private long storageQueueUp;

    /**
     * EnduringLineLength
     */
    private long storageQueueCapacity;

    /**
     * StatisticsExecutor TPS
     */
    private TpsVO tps;

    private CpuVO cpu;

    private MemoryVO memory;

    private DiskSpaceVO disk;

    private List<MetricResponseVO> metrics;

    public long getQueueUp() {
        return queueUp;
    }

    public void setQueueUp(long queueUp) {
        this.queueUp = queueUp;
    }

    public long getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(long queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public long getStorageQueueUp() {
        return storageQueueUp;
    }

    public void setStorageQueueUp(long storageQueueUp) {
        this.storageQueueUp = storageQueueUp;
    }

    public long getStorageQueueCapacity() {
        return storageQueueCapacity;
    }

    public void setStorageQueueCapacity(long storageQueueCapacity) {
        this.storageQueueCapacity = storageQueueCapacity;
    }

    public TpsVO getTps() {
        return tps;
    }

    public void setTps(TpsVO tps) {
        this.tps = tps;
    }

    public CpuVO getCpu() {
        return cpu;
    }

    public void setCpu(CpuVO cpu) {
        this.cpu = cpu;
    }

    public MemoryVO getMemory() {
        return memory;
    }

    public void setMemory(MemoryVO memory) {
        this.memory = memory;
    }

    public DiskSpaceVO getDisk() {
        return disk;
    }

    public void setDisk(DiskSpaceVO disk) {
        this.disk = disk;
    }

    public List<MetricResponseVO> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricResponseVO> metrics) {
        this.metrics = metrics;
    }
}
