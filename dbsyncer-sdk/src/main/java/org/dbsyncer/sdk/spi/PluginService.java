/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.sdk.spi;

import org.dbsyncer.sdk.plugin.PluginContext;

/**
 * PluginExtension of AIInterface
 * <p>Full sync/Incremental sync,ExtensionConvert</p>
 *
 * @author AE86
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
