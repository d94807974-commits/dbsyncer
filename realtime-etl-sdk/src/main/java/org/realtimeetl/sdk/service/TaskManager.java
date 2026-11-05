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
package org.realtimeetl.sdk.service;

import org.realtimeetl.common.model.ConfigModel;

import java.util.List;

/**
 * Here.TasksExecutor: BySpring Injection for cluster/single control orStop Puller。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-09-07
 */
public interface TaskManager {

    /**
     * Pull it up.TasksExecutor.
     */
    void start(ConfigModel configModel, boolean autoRecovery);

    /**
     * Here.StopTasksExecutor.
     *
     * @param taskId Tasks ID（Mapping ID）
     */
    void stop(String taskId);

    List<String> getRunningTaskId();
}
