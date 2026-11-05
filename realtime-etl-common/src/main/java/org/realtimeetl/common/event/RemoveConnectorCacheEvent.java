/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.event;

import org.realtimeetl.common.message.impl.RemoveCacheMessage;
import org.springframework.context.ApplicationContext;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-09-23 01:14
 */
public final class RemoveConnectorCacheEvent extends RemoveCacheEvent {

    public RemoveConnectorCacheEvent(ApplicationContext source, RemoveCacheMessage commonMessage) {
        super(source, commonMessage);
    }
}
