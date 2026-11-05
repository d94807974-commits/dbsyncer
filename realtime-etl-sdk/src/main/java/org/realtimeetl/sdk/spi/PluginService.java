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
package org.realtimeetl.sdk.spi;

import org.realtimeetl.sdk.plugin.PluginContext;

/**
 * PluginExtension of AIInterface
 * <p>Full sync/Incremental sync,ExtensionConvert</p>
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2021/02/22 20:26
 */
public interface PluginService {

    /**
     * Initialise Loading
     */
    default void init() {
    }

    /**
     * Full sync/ScheduledIncremental syncPreprocess
     *
     * @param pluginContext Context
     */
    default void postProcessBefore(PluginContext pluginContext) {
    }

    /**
     * Full sync/Incremental sync
     *
     * @param pluginContext Context
     */
    void convert(PluginContext pluginContext);

    /**
     * Full sync/Incremental syncPost-processing
     *
     * @param pluginContext Context
     */
    default void postProcessAfter(PluginContext pluginContext) {
    }

    /**
     * VersionNo. No.
     *
     * @return
     */
    default String getVersion() {
        return "1.0.0";
    }

    /**
     * PluginName
     *
     * @return
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * CloseServices(UploadSamePluginor servicesCloseTime Trigger)
     */
    default void close() {
    }
}
