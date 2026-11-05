/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.event;

import org.dbsyncer.common.message.impl.RemoveCacheMessage;
import org.springframework.context.ApplicationContext;

/**
 * Police strategy CorruptConfigureCache failed.
 *
 * @author wuji
 * @version 1.0.0
 */
public final class RemoveNoticeStrategyCacheEvent extends RemoveCacheEvent {

    public RemoveNoticeStrategyCacheEvent(ApplicationContext source, RemoveCacheMessage commonMessage) {
        super(source, commonMessage);
    }
}
