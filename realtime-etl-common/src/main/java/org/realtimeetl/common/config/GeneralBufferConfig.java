package org.realtimeetl.common.config;

import org.realtimeetl.common.util.ThreadPoolUtil;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * General actuatorConfigure
 *
 * @author AE86
 * @version 1.0.0
 * @date 2023/8/28 23:50
 */
@Configuration
@ConfigurationProperties(prefix = "realtime-etl.parser.general")
public class GeneralBufferConfig extends BufferActuatorConfig {

    /**
     * WorkThreads
     */
    private int threadCoreSize = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * Max.Threads
     */
    private int maxThreadSize = threadCoreSize * 2;;

    /**
     * WorklineTasksLine
     */
    private int threadQueueCapacity = 1000;

    @Bean(name = "generalExecutor", destroyMethod = "shutdown")
    public ThreadPoolTaskExecutor generalExecutor() {
        return ThreadPoolUtil.newThreadPoolTaskExecutor(threadCoreSize, maxThreadSize, threadQueueCapacity, 30, "GeneralExecutor-");
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
