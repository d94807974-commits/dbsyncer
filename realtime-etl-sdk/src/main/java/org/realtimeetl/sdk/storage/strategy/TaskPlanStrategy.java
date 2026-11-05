/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.storage.strategy;

import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.Strategy;

/**
 * TasksminPlan: Form Storage{@code realtime_etl_cluster_task_plan}。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-09-05
 */
public final class TaskPlanStrategy implements Strategy {

    @Override
    public String createSharding(String separator, String collectionId) {
        return StorageEnum.TASK_PLAN.getType();
    }
}
