/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.sdk.listener;

import org.realtimeetl.sdk.model.ChangedOffset;

import java.util.Collections;
import java.util.Map;

/**
 * ListenerInterface，Provision for realizationIncremental syncFunctions, supportScheduledandLogParsing
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-11-21 22:48
 */
public interface Listener {

    /**
     * Initialize
     */
    default void init() {
    };

    /**
     * Capture CurrentIncrementalBitsFull + incrementalModeStartFull(Appendix)
     *
     * @return bit keys capturedValue，Not capturedBackEmptyMap
     */
    default Map<String, String> captureSnapshot() {
        return Collections.emptyMap();
    }

    /**
     * StartScheduled/LogExtractTasks
     */
    void start();

    /**
     * CloseTasks
     */
    void close();

    /**
     * Registered listeningEvent（AccessIncrementalData)
     *
     * @param watcher
     */
    void register(Watcher watcher);

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
     * UpdateIncrementalPoints 71
     *
     * @param offset
     */
    void refreshEvent(ChangedOffset offset);

    /**
     * B OBJECT.InsertMeasures.Event
     */
    void flushEvent();

    /**
     * Force BrushInsertMeasures.Event
     */
    void forceFlushEvent();

    /**
     * ErrorEvent
     *
     * @param e
     */
    void errorEvent(Exception e);
}
