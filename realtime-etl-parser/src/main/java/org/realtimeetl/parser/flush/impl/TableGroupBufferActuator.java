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
package org.realtimeetl.parser.flush.impl;

import org.realtimeetl.common.config.TableGroupBufferConfig;
import org.realtimeetl.common.scheduled.ScheduledTaskService;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.common.util.ThreadPoolUtil;
import org.realtimeetl.common.util.UUIDUtil;
import org.realtimeetl.parser.flush.BufferRequest;
import org.realtimeetl.sdk.spi.TableGroupBufferActuatorService;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;

import java.util.concurrent.Executor;

/**
 * Table-group actuator（Table consumption data, ConcertedThreadsBulk, sequenced)
 *
 * @Version 1.0.0
 * @Author Realtime ETL
 * @Date 2023-03-27 16:50
 */
public class TableGroupBufferActuator extends GeneralBufferActuator implements TableGroupBufferActuatorService {

    @Resource
    private TableGroupBufferConfig tableGroupBufferConfig;

    @Resource
    private ScheduledTaskService scheduledTaskService;

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    private String taskKey;

    private String tableName;

    private volatile boolean running;

    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public boolean isRunning(BufferRequest request) {
        return running;
    }

    @Override
    public Executor getExecutor() {
        return threadPoolTaskExecutor;
    }

    /**
     * StartTable / Corporate Executer stoleclone Called later.
     */
    public void start() {
        buildConfig();
    }

    @Override
    protected void buildConfig() {
        super.setConfig(tableGroupBufferConfig);
        super.buildQueueConfig();
        taskKey = UUIDUtil.getUUID();
        int coreSize = tableGroupBufferConfig.getThreadCoreSize();
        int maxSize = tableGroupBufferConfig.getMaxThreadSize();
        int queueCapacity = tableGroupBufferConfig.getThreadQueueCapacity();
        String threadNamePrefix = "TableGroupExecutor-" + tableName + StringUtil.HORIZONTAL + tableName.hashCode() + StringUtil.HORIZONTAL;
        threadPoolTaskExecutor = ThreadPoolUtil.newThreadPoolTaskExecutor(coreSize, maxSize, queueCapacity, 30, threadNamePrefix);
        running = true;
        scheduledTaskService.start(taskKey, tableGroupBufferConfig.getBufferPeriodMillisecond(), this);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public void stop() {
        running = false;
        if (threadPoolTaskExecutor != null) {
            threadPoolTaskExecutor.shutdown();
        }
        scheduledTaskService.stop(taskKey);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
