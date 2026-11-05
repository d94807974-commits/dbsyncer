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
package org.realtimeetl.manager.impl;

import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.common.event.ClosedEvent;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.manager.Puller;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.sdk.service.TaskManager;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Here.TasksExecutor: Synchronize route to correspondingPuller。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-09-08
 */
@Component
public final class TaskManagerImpl implements TaskManager, ApplicationListener<ClosedEvent> {

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private Map<String, Puller> map;

    @Override
    public void start(ConfigModel configModel, boolean autoRecovery) {
        Mapping mapping = (Mapping) configModel;
        getPuller(mapping).start(mapping, autoRecovery);
    }

    @Override
    public void stop(String taskId) {
        Mapping mapping = requireMapping(taskId);
        getPuller(mapping).close(taskId);
    }

    @Override
    public List<String> getRunningTaskId() {
        List<String> runningTaskId = new ArrayList<>();
        map.forEach((mapping, puller) -> {
            runningTaskId.addAll(puller.getTaskId());
        });
        return runningTaskId;
    }

    private Mapping requireMapping(String taskId) {
        Mapping mapping = mappingProfile.get(taskId);
        Assert.notNull(mapping, String.format("Synchronising tasks do not exist: %s", taskId));
        return mapping;
    }

    private Puller getPuller(Mapping mapping) {
        String model = mapping.getModel();
        return map.get(model.concat("Puller"));
    }

    @Override
    public void onApplicationEvent(ClosedEvent event) {
        changeMetaState(event.getTaskId(), CommonTaskStatusEnum.READY);
        // Cluster: Empty/receiverConnect（UserStop、The natural end 1/Failed）
        releaseMappingConnectors(event.getTaskId());
    }

    public void changeMetaState(String taskId, CommonTaskStatusEnum status) {
        Meta meta = taskMetaProfile.getMeta(taskId);
        int code = status.getCode();
        if (null != meta && meta.getState() != code) {
            long now = Instant.now().toEpochMilli();
            meta.setState(code);
            meta.setUpdateTime(now);
            // 进入运行中时记录本轮启动时间，供耗时（updateTime - startTime）计算
            if (CommonTaskStatusEnum.RUNNING == status) {
                meta.setStartTime(now);
            }
            taskMetaProfile.updateMeta(meta);
        }
    }

    /**
     * TasksCloseThen release the source/Target Connection（and PreloadTemplate.reConnect Same thing.instanceId）。
     *
     * @param taskId
     */
    private void releaseMappingConnectors(String taskId) {
        Mapping mapping = mappingProfile.get(taskId);
        if (mapping == null) {
            return;
        }
        connectorFactory.disconnect(ConnectorInstanceUtil.buildConnectorInstanceId(
                mapping.getId(), mapping.getSourceConnectorId(), ConnectorInstanceUtil.SOURCE_SUFFIX));
        connectorFactory.disconnect(ConnectorInstanceUtil.buildConnectorInstanceId(
                mapping.getId(), mapping.getTargetConnectorId(), ConnectorInstanceUtil.TARGET_SUFFIX));
    }

}
