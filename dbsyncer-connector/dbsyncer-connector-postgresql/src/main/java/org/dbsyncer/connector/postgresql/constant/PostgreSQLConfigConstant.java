/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.connector.postgresql.constant;

import org.dbsyncer.connector.postgresql.enums.MessageDecoderEnum;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-12-29 00:21
 */
public final class PostgreSQLConfigConstant {

    /**
     * Plugin {@link MessageDecoderEnum}
     */
    public static final String PLUGIN_NAME = "pluginName";

    /**
     * Delete slot，Incremental sync: automatically delete Slot when stopping the driver
     */
    public static final String DROP_SLOT_ON_CLOSE = "dropSlotOnClose";
}
