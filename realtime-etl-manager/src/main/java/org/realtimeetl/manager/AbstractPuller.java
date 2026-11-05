package org.realtimeetl.manager;

import org.realtimeetl.common.event.ClosedEvent;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;

public abstract class AbstractPuller implements Puller {

    @Resource
    private ApplicationContext applicationContext;

    protected void publishClosedEvent(String taskId) {
        applicationContext.publishEvent(new ClosedEvent(applicationContext, taskId));
    }
}
