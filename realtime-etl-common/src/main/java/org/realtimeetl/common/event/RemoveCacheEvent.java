package org.realtimeetl.common.event;

import org.realtimeetl.common.message.impl.RemoveCacheMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * DeleteConfigureCacheEvent
 *
 * @version 1.0.0
 * @author AE86
 * @date 2020-04-26 22:45
 */
public class RemoveCacheEvent extends ApplicationContextEvent {

    private final RemoveCacheMessage commonMessage;

    public RemoveCacheEvent(ApplicationContext source, RemoveCacheMessage commonMessage) {
        super(source);
        this.commonMessage = commonMessage;
    }

    public RemoveCacheMessage getCommonMessage() {
        return commonMessage;
    }
}
