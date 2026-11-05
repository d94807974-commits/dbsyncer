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
package org.realtimeetl.parser.consumer;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.plugin.PluginFactory;
import org.realtimeetl.plugin.enums.ProcessEnum;
import org.realtimeetl.sdk.listener.ChangedEvent;
import org.realtimeetl.sdk.listener.QuartzListenerContext;
import org.realtimeetl.sdk.listener.Watcher;
import org.realtimeetl.sdk.spi.BufferActuatorRouterService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2023-11-12 01:32
 */
@Component
public final class ParserConsumer implements Watcher, Cloneable {

    @Resource
    private BufferActuatorRouterService bufferActuatorRouter;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private PluginFactory pluginFactory;

    @Resource
    private LogService logService;

    private String taskId;

    public void build(String taskId, List<TableGroup> tableGroups, int channelSize) {
        this.taskId = taskId;
        bufferActuatorRouter.bind(taskId, extractSourceTableNames(tableGroups), channelSize);
    }

    @Override
    public void changeEventBefore(QuartzListenerContext context) {
        pluginFactory.process(context, ProcessEnum.BEFORE);
    }

    @Override
    public void changeEvent(ChangedEvent event) {
        bufferActuatorRouter.execute(taskId, event);
    }

    @Override
    public void flushEvent(Map<String, String> snapshot) {
        Meta meta = taskMetaProfile.getMeta(taskId);
        if (meta != null) {
            meta.setSnapshot(snapshot);
            taskMetaProfile.updateMeta(meta);
        }
    }

    @Override
    public void errorEvent(Exception e) {
        logService.log(LogType.TableGroupLog.INCREMENT_FAILED, e.getMessage());
    }

    @Override
    public long getMetaUpdateTime() {
        Meta meta = taskMetaProfile.getMeta(taskId);
        return meta != null ? meta.getUpdateTime() : 0L;
    }

    @Override
    public ParserConsumer clone() {
        try {
            return (ParserConsumer) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    private List<String> extractSourceTableNames(List<TableGroup> tableGroups) {
        List<String> tableNames = new ArrayList<>();
        if (tableGroups == null) {
            return tableNames;
        }
        for (TableGroup tableGroup : tableGroups) {
            if (tableGroup == null || tableGroup.getSourceTable() == null
                    || StringUtil.isBlank(tableGroup.getSourceTable().getName())) {
                continue;
            }
            tableNames.add(tableGroup.getSourceTable().getName());
        }
        return tableNames;
    }
}
