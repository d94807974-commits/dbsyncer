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
package org.realtimeetl.biz.vo;

import java.math.BigDecimal;

/**
 * ClusterNodesOperational indicators (in-house collection orHTTP Pull.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-20
 */
public class ClusterNodeMetricVO {

    private String nodeId;
    private String name;
    private boolean local;
    private String ip;
    private int httpPort;
    private int status;
    private boolean reachable;

    private BigDecimal cpuPercent;
    private BigDecimal memoryUsed;
    private BigDecimal memoryTotal;
    private long threadLive;
    private BigDecimal diskUsed;
    private BigDecimal diskTotal;
    private double tps;
    /**
     * Near1 minutesEverysecondsS 5.5%Sequences for clustersPageConvergence charts.
     */
    private HistoryStackVO tpsSeries;
    private long queueUp;
    private long storageQueueUp;
    private int fullWorkItemCount;
    private int incrementalCount;
    private int pluginSize;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public BigDecimal getCpuPercent() {
        return cpuPercent;
    }

    public void setCpuPercent(BigDecimal cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public BigDecimal getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(BigDecimal memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public BigDecimal getMemoryTotal() {
        return memoryTotal;
    }

    public void setMemoryTotal(BigDecimal memoryTotal) {
        this.memoryTotal = memoryTotal;
    }

    public long getThreadLive() {
        return threadLive;
    }

    public void setThreadLive(long threadLive) {
        this.threadLive = threadLive;
    }

    public BigDecimal getDiskUsed() {
        return diskUsed;
    }

    public void setDiskUsed(BigDecimal diskUsed) {
        this.diskUsed = diskUsed;
    }

    public BigDecimal getDiskTotal() {
        return diskTotal;
    }

    public void setDiskTotal(BigDecimal diskTotal) {
        this.diskTotal = diskTotal;
    }

    public double getTps() {
        return tps;
    }

    public void setTps(double tps) {
        this.tps = tps;
    }

    public HistoryStackVO getTpsSeries() {
        return tpsSeries;
    }

    public void setTpsSeries(HistoryStackVO tpsSeries) {
        this.tpsSeries = tpsSeries;
    }

    public long getQueueUp() {
        return queueUp;
    }

    public void setQueueUp(long queueUp) {
        this.queueUp = queueUp;
    }

    public long getStorageQueueUp() {
        return storageQueueUp;
    }

    public void setStorageQueueUp(long storageQueueUp) {
        this.storageQueueUp = storageQueueUp;
    }

    public int getFullWorkItemCount() {
        return fullWorkItemCount;
    }

    public void setFullWorkItemCount(int fullWorkItemCount) {
        this.fullWorkItemCount = fullWorkItemCount;
    }

    public int getIncrementalCount() {
        return incrementalCount;
    }

    public void setIncrementalCount(int incrementalCount) {
        this.incrementalCount = incrementalCount;
    }

    public int getPluginSize() {
        return pluginSize;
    }

    public void setPluginSize(int pluginSize) {
        this.pluginSize = pluginSize;
    }
}
