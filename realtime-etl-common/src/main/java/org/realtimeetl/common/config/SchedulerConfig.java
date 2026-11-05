package org.realtimeetl.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.RejectedExecutionHandler;

/**
 * @author yjwang
 * @date 2022/4/29 10:27
 */
@Configuration
@ConfigurationProperties(prefix = "realtime-etl.web.scheduler")
public class SchedulerConfig implements SchedulingConfigurer {

    /**
     * WorkThreads
     */
    private int poolSize = Runtime.getRuntime().availableProcessors();

    @Override
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setTaskScheduler(taskScheduler());
    }

    @Bean(name = "taskScheduler", destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // CoreThreadsPool Size
        scheduler.setPoolSize(poolSize);
        // ThreadsName65 prefix
        scheduler.setThreadNamePrefix("taskScheduler-");
        // SettingsThreadsJi.Close♪ Wait Executive time for allTasksBothFinishKeep destroying it.OtherYes.Bean
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        // SettingsThreadsIke.TasksWaitingTime，If it's not destroyed by this time, then it's mandatory to destroy it to ensure that the application is finally capable of being destroyed.Close，NotYesBlock it.
        scheduler.setAwaitTerminationSeconds(30);
        // ThreadsJiman, no strategy.
        scheduler.setRejectedExecutionHandler(rejectedExecutionHandler());
        return scheduler;
    }

    private RejectedExecutionHandler rejectedExecutionHandler() {
        return (r, executor)-> {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
