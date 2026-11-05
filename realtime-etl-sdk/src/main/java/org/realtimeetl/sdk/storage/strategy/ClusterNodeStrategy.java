/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.storage.strategy;

import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.Strategy;

/**
 * ClusterSection 235Point: Form storage {@code realtime_etl_cluster_node}。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-08-18
 */
public final class ClusterNodeStrategy implements Strategy {

    @Override
    public String createSharding(String separator, String collectionId) {
        return StorageEnum.CLUSTER_NODE.getType();
    }
}
