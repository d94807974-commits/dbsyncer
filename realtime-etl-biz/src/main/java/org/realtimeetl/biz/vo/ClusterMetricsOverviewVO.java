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

import java.util.ArrayList;
import java.util.List;

/**
 * ClusterNodesOverview of indicators.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-20
 */
public class ClusterMetricsOverviewVO {

    private double totalTps;
    private long totalQueue;
    private long totalFullWorkItems;
    private long totalIncremental;
    private HistoryStackVO tps;
    private HistoryStackVO queue;
    private HistoryStackVO fullWorkItems;
    private List<ClusterNodeMetricVO> nodes = new ArrayList<>();

    public double getTotalTps() {
        return totalTps;
    }

    public void setTotalTps(double totalTps) {
        this.totalTps = totalTps;
    }

    public long getTotalQueue() {
        return totalQueue;
    }

    public void setTotalQueue(long totalQueue) {
        this.totalQueue = totalQueue;
    }

    public long getTotalFullWorkItems() {
        return totalFullWorkItems;
    }

    public void setTotalFullWorkItems(long totalFullWorkItems) {
        this.totalFullWorkItems = totalFullWorkItems;
    }

    public long getTotalIncremental() {
        return totalIncremental;
    }

    public void setTotalIncremental(long totalIncremental) {
        this.totalIncremental = totalIncremental;
    }

    public HistoryStackVO getTps() {
        return tps;
    }

    public void setTps(HistoryStackVO tps) {
        this.tps = tps;
    }

    public HistoryStackVO getQueue() {
        return queue;
    }

    public void setQueue(HistoryStackVO queue) {
        this.queue = queue;
    }

    public HistoryStackVO getFullWorkItems() {
        return fullWorkItems;
    }

    public void setFullWorkItems(HistoryStackVO fullWorkItems) {
        this.fullWorkItems = fullWorkItems;
    }

    public List<ClusterNodeMetricVO> getNodes() {
        return nodes;
    }

    public void setNodes(List<ClusterNodeMetricVO> nodes) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
    }
}
