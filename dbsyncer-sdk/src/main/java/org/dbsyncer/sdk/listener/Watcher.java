package org.dbsyncer.sdk.listener;

import java.util.Map;

/**
 * Data Change Listener
 *
 * @version 1.0.0
 * @Author AE86
 * @Date 2020-05-11 22:50
 */
public interface Watcher {

    /**
     * Data FactChangeFrontSetEvent
     *
     * @param context
     */
    void changeEventBefore(QuartzListenerContext context);

    /**
     * Data FactChangeEvent
     *
     * @param event
     */
    void changeEvent(ChangedEvent event);

    /**
     * EnduringIncrementalPoints 71Event
     *
     * @param snapshot
     */
    void flushEvent(Map<String, String> snapshot);

    /**
     * ErrorEvent
     *
     * @param e
     */
    void errorEvent(Exception e);

    /**
     * AccessMetaUpdated at
     *
     * @return
     */
    long getMetaUpdateTime();
}
