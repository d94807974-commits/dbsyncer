/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.common.config;

import org.realtimeetl.common.util.ThreadPoolUtil;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Movement controlTasksExecut MOUConfigure
 *
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-06-22 23:20
 */
@Configuration
@ConfigurationProperties(prefix = "realtime-etl.common.dispatch")
public class DispatchTaskConfig {

    /**
     * WorkThreads
     */
    private int threadCoreSize = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * Max.Threads
     */
    private int maxThreadSize = 16;

    /**
     * WorklineTasksLine
     */
    private int threadQueueCapacity = 64;

    @Bean(name = "dispatchTaskExecutor", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor dispatchTaskExecutor() {
        return ThreadPoolUtil.newThreadPoolTaskExecutor(threadCoreSize, maxThreadSize, threadQueueCapacity, 30, "DispatchTaskExecutor-");
    }

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
