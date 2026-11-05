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
package org.realtimeetl.manager.deployment;

import org.realtimeetl.common.enums.CommonTaskTypeEnum;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.service.TaskManager;
import org.realtimeetl.sdk.spi.ClusterService;

import javax.annotation.Resource;

/**
 * Single control surface: this is the operator and the method of movement is emptyActions。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-18
 */
public final class StandaloneService implements ClusterService {

    @Resource
    private TaskManager taskManager;

    @Override
    public void start(ConfigModel configModel, boolean autoRecovery) {
        taskManager.start(configModel, autoRecovery);
    }

    @Override
    public void stop(String taskId, CommonTaskTypeEnum taskType) {
        taskManager.stop(taskId);
    }

}
