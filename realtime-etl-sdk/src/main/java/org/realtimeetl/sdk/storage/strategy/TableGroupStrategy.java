/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.sdk.storage.strategy;

import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.Strategy;

/**
 * Map relationConfigure：Form Storage
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2026-07-17 17:00
 */
public final class TableGroupStrategy implements Strategy {

    @Override
    public String createSharding(String separator, String collectionId) {
        return StorageEnum.TABLE_GROUP.getType();
    }
}
