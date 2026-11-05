/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.storage.strategy;

import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.Strategy;

/**
 * UserRoleRelationship: Form storage
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public final class UserRoleStrategy implements Strategy {

    @Override
    public String createSharding(String separator, String collectionId) {
        return StorageEnum.USER_ROLE.getType();
    }
}
