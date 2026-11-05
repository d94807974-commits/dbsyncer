package org.realtimeetl.common.event;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;

/**
 * TasksCloseEvent
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020-04-26 22:45
 */
public final class ClosedEvent extends ApplicationContextEvent {

    private final String taskId;

    public ClosedEvent(ApplicationContext source, String taskId) {
        super(source);
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }
}
