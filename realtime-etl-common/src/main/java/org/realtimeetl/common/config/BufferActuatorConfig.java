package org.realtimeetl.common.config;

/**
 * Buffer zoneConfigure
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022/7/14 23:50
 */
public abstract class BufferActuatorConfig {

    /**
     * Single executionTasksNumber
     */
    private int bufferWriterCount = 100;

    /**
     * From every consumption cache queueTasksNumber
     */
    private int bufferPullCount = 1000;

    /**
     * Cache Queue Capacity
     */
    private int bufferQueueCapacity = 3_0000;

    /**
     * ScheduledCache queue interval(ms)
     */
    private int bufferPeriodMillisecond = 300;

    public int getBufferWriterCount() {
        return bufferWriterCount;
    }

    public void setBufferWriterCount(int bufferWriterCount) {
        this.bufferWriterCount = bufferWriterCount;
    }

    public int getBufferPullCount() {
        return bufferPullCount;
    }

    public void setBufferPullCount(int bufferPullCount) {
        this.bufferPullCount = bufferPullCount;
    }

    public int getBufferQueueCapacity() {
        return bufferQueueCapacity;
    }

    public void setBufferQueueCapacity(int bufferQueueCapacity) {
        this.bufferQueueCapacity = bufferQueueCapacity;
    }

    public int getBufferPeriodMillisecond() {
        return bufferPeriodMillisecond;
    }

    public void setBufferPeriodMillisecond(int bufferPeriodMillisecond) {
        this.bufferPeriodMillisecond = bufferPeriodMillisecond;
    }
}
