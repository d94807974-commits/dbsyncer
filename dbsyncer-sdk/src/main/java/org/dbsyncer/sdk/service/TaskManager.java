/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.service;

import org.dbsyncer.common.model.ConfigModel;

import java.util.List;

/**
 * Here.TasksExecutor: BySpring Injection for cluster/single control orStop Puller。
 *
 * @author wuji
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
