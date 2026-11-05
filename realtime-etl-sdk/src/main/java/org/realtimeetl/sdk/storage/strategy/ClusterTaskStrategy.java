/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.storage.strategy;

import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.Strategy;

/**
 * ClusterTasksSystems{@code realtime_etl_cluster_task}。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-09-01
 */
public final class ClusterTaskStrategy implements Strategy {

    @Override
    public String createSharding(String separator, String collectionId) {
        return StorageEnum.CLUSTER_TASK.getType();
    }
}
