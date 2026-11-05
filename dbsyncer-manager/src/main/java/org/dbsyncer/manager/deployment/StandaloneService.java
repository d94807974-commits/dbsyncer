/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.manager.deployment;

import org.dbsyncer.common.enums.CommonTaskTypeEnum;
import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.sdk.service.TaskManager;
import org.dbsyncer.sdk.spi.ClusterService;

import javax.annotation.Resource;

/**
 * Single control surface: this is the operator and the method of movement is emptyActions。
 *
 * @author wuji
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
