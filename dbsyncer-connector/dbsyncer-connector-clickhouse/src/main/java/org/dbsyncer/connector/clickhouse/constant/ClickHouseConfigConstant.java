/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.clickhouse.constant;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-05-29 23:50
 */
public final class ClickHouseConfigConstant {

    private ClickHouseConfigConstant() {
    }

    /**
     * IncrementalInterrogation interval (seconds）
     */
    public static final String POLL_INTERVAL_SECONDS = "pollIntervalSeconds";

    /**
     * IncrementalBatch Size
     */
    public static final String POLL_BATCH_SIZE = "pollBatchSize";
}
