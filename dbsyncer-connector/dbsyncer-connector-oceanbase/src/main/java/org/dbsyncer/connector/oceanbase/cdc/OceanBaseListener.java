/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.oceanbase.cdc;

import org.dbsyncer.connector.mysql.binlog.BinaryLogClient;
import org.dbsyncer.connector.mysql.binlog.BinaryLogRemoteClient;
import org.dbsyncer.connector.mysql.cdc.MySQLListener;
import org.dbsyncer.connector.oceanbase.OceanBaseException;
import org.dbsyncer.sdk.config.DatabaseConfig;

/**
 * OceanBase IncrementalListening, based onBinlog Service OutputMySQL Binlog V4 Agreements
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-05 00:20
 */
public final class OceanBaseListener extends MySQLListener {

    @Override
    protected BinaryLogClient createBinlogClient(DatabaseConfig config) throws Exception {
        String host = OceanBaseBinlogConfig.getBinlogHost(config);
        int port = OceanBaseBinlogConfig.getBinlogPort(config);
        logger.info("Connect to OceanBase Binlog Service", host, port);
        return new BinaryLogRemoteClient(host, port, config.getUsername(), config.getPassword());
    }

    @Override
    protected String getAlreadyStartedMessage() {
        return "Transform the OceanBase Binlog listening device";
    }

    @Override
    protected String getCaptureSnapshotFailedMessage() {
        return "Failed to capture OceanBase Bin log bits: }";
    }

    @Override
    protected RuntimeException wrapException(Exception e) {
        return new OceanBaseException(e);
    }

    @Override
    protected RuntimeException wrapException(String message) {
        return new OceanBaseException(message);
    }
}
