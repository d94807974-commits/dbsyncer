package org.realtimeetl.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Table-group actuatorConfigure
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2023/8/28 23:50
 */
@Configuration
@ConfigurationProperties(prefix = "realtime-etl.parser.table.group")
public class TableGroupBufferConfig extends BufferActuatorConfig {

    /**
     * WorkThreads
     */
    private int threadCoreSize = 1;

    /**
     * Max.Threads
     */
    private int maxThreadSize = threadCoreSize * 2;

    /**
     * WorklineTasksLine
     */
    private int threadQueueCapacity = 1000;

    public int getThreadCoreSize() {
        return threadCoreSize;
    }

    public void setThreadCoreSize(int threadCoreSize) {
        this.threadCoreSize = threadCoreSize;
    }

    public int getMaxThreadSize() {
        return maxThreadSize;
    }

    public void setMaxThreadSize(int maxThreadSize) {
        this.maxThreadSize = maxThreadSize;
    }

    public int getThreadQueueCapacity() {
        return threadQueueCapacity;
    }

    public void setThreadQueueCapacity(int threadQueueCapacity) {
        this.threadQueueCapacity = threadQueueCapacity;
    }
}
