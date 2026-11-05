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

import org.realtimeetl.common.config.StorageConfig;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.flush.AbstractBufferActuator;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.StorageRequest;
import org.realtimeetl.parser.model.StorageResponse;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

/**
 * Storage actuator
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2023-03-27 16:50
 */
@Component
public final class StorageBufferActuator extends AbstractBufferActuator<StorageRequest, StorageResponse> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private StorageConfig storageConfig;

    @Resource
    private StorageService storageService;

    @Resource
    private Executor storageExecutor;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @PostConstruct
    private void init() {
        setConfig(storageConfig);
        buildConfig();
    }

    @Override
    protected String getPartitionKey(StorageRequest request) {
        return request.getTaskDetailShardId();
    }

    @Override
    protected void partition(StorageRequest request, StorageResponse response) {
        response.setTaskDetailShardId(request.getTaskDetailShardId());
        response.getDataList().add(request.getRow());
    }

    @Override
    public void pull(StorageResponse response) {
        String shardId = response.getTaskDetailShardId();
        // Must copy:process() finally Yes.clearDataList，I can't do it.TotalUse the sameLinkedList（Parallelclear Yes.Nodes item Setnull → NPE）
        List<Map> batch = new ArrayList<>(response.getDataList());
        storageExecutor.execute(() -> storageService.addBatch(StorageEnum.TASK_DETAIL, shardId, batch));
    }

    @Override
    protected void offerFailed(BlockingQueue<StorageRequest> queue, StorageRequest request) {
        String shardId = request.getTaskDetailShardId();
        Meta meta = taskMetaProfile.getMeta(shardId);
        if (meta != null) {
            Mapping mapping = mappingProfile.get(meta.getTaskId());
            if (mapping != null) {
                logger.info("{}, data={}", mapping.getName(), request.getRow());
            }
        }
    }

    @Override
    public Executor getExecutor() {
        return storageExecutor;
    }
}