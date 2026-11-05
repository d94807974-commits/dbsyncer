/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.storage.strategy;

import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.Strategy;

/**
 * Alert notificationRule: Form Storage
 *
 * @author wuji
 * @version 1.0.0
 */
public final class NoticeRuleStrategy implements Strategy {

    @Override
    public String createSharding(String separator, String collectionId) {
        return StorageEnum.NOTICE_RULE.getType();
    }
}
