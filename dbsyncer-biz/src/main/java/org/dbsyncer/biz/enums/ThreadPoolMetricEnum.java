package org.dbsyncer.biz.enums;

/**
 * ThreadsPool indicators
 *
 * @author AE86
 * @version 1.0.0
 * @date 2021/07/23 0:19
 */
public enum ThreadPoolMetricEnum {

    /**
     * CoreThreads
     */
    CORE_SIZE("Threads"),
    /**
     * Queued
     */
    QUEUE_UP("Queued"),
    /**
     * Processing
     */
    ACTIVE("Processing"),
    /**
     * Completed
     */
    COMPLETED("Finish"),
    /**
     * Idle queue
     */
    REMAINING_CAPACITY("Idle queue");

    private final String metricName;

    ThreadPoolMetricEnum(String metricName) {
        this.metricName = metricName;
    }

    public String getMetricName() {
        return metricName;
    }
}
