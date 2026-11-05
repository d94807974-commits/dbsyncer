/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.model;

/**
 * Cache ExecutorMonitorSnapshotTable-group actuatororIncrementalPipeline.
 *
 * @author wuji
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
