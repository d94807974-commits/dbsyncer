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

import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.config.DatabaseConfig;

/**
 * OceanBase Binlog ServicesConnectConfigure（MySQL Binlog V4 )
 */
public final class OceanBaseBinlogConfig {

    public static final String BINLOG_HOST = "binlogHost";
    public static final String BINLOG_PORT = "binlogPort";

    private OceanBaseBinlogConfig() {
    }

    public static String getBinlogHost(DatabaseConfig config) {
        String binlogHost = config.getExtInfo().getProperty(BINLOG_HOST);
        return StringUtil.isBlank(binlogHost) ? config.getHost() : binlogHost.trim();
    }

    public static int getBinlogPort(DatabaseConfig config) {
        String binlogPort = config.getExtInfo().getProperty(BINLOG_PORT);
        if (StringUtil.isBlank(binlogPort)) {
            return config.getPort();
        }
        return NumberUtil.toInt(binlogPort.trim(), config.getPort());
    }
}
