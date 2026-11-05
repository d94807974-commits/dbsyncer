/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.sdk.storage.strategy;

import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.Strategy;

/**
 * TasksImplementation Details：PressTasksminTable(EachTasksA watch., The same logic.realtime_etl_data)
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2026-07-17 17:00
 */
public final class TaskDetailStrategy implements Strategy {

    @Override
    public String createSharding(String separator, String collectionId) {
        return new StringBuilder(StorageEnum.TASK_DETAIL.getType()).append(separator).append(collectionId).toString();
    }
}
