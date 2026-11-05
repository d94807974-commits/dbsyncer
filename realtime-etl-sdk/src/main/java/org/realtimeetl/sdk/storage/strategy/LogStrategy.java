/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.sdk.storage.strategy;

import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.Strategy;

/**
 * Log：Connector、Mapping、TableGroup、Meta、System Log
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2019-11-16 23:22
 */
public final class LogStrategy implements Strategy {

    @Override
    public String createSharding(String separator, String collectionId) {
        return StorageEnum.LOG.getType();
    }
}
