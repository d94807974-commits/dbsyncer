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
package org.realtimeetl.sdk.model;

/**
 * Cache ExecutorMonitorSnapshotTable-group actuatororIncrementalPipeline.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-12
 */
public class BufferActuatorMetric {

    /**
     * Mapping Meta ID
     */
    private String taskId;

    /**
     * Presentation name: Open SourceSource Table Name，Business version is Pipename
     */
    private String name;

    /**
     * Cache Que InstitutionBacklogNumber
     */
    private int queueSize;

    /**
     * Cache Queue Capacity
     */
    private int queueCapacity;

    /**
     * WriteThreadsJi.LiveThreads
     */
    private int activeCount;

    /**
     * WriteThreadsThe pool is LandlockedThreads
     */
    private int maxPoolSize;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * WriteThreadsJi.CompletedTasksNumber
     */
    private long completedTaskCount;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getActiveCount() {
        return activeCount;
    }

    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(long completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }
}
