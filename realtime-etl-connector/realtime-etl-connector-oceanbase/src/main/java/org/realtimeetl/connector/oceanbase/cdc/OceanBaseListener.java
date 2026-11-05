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
package org.realtimeetl.connector.oceanbase.cdc;

import org.realtimeetl.connector.mysql.binlog.BinaryLogClient;
import org.realtimeetl.connector.mysql.binlog.BinaryLogRemoteClient;
import org.realtimeetl.connector.mysql.cdc.MySQLListener;
import org.realtimeetl.connector.oceanbase.OceanBaseException;
import org.realtimeetl.sdk.config.DatabaseConfig;

/**
 * OceanBase IncrementalListening, based onBinlog Service OutputMySQL Binlog V4 Agreements
 *
 * @author Realtime ETL.
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
