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
package org.realtimeetl.connector.oceanbase.validator;

import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.oceanbase.cdc.OceanBaseBinlogConfig;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.AbstractDataBaseConfigValidator;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import org.springframework.util.Assert;

import java.util.Map;

/**
 * OceanBase ConnectConfigureVerify INTRODUCTION
 */
public class OceanBaseConfigValidator extends AbstractDataBaseConfigValidator {

    private static final String DEFAULT_DRIVER = "com.oceanbase.jdbc.Driver";

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName(DEFAULT_DRIVER);
        }
        putExtInfo(connectorConfig, OceanBaseBinlogConfig.BINLOG_HOST, params.get("binlogHost"));
        String binlogPort = params.get("binlogPort");
        if (StringUtil.isNotBlank(binlogPort)) {
            int port = NumberUtil.toInt(binlogPort.trim());
            Assert.isTrue(port > 0, "Binlog port is invalid");
            connectorConfig.getExtInfo().setProperty(OceanBaseBinlogConfig.BINLOG_PORT, String.valueOf(port));
        }
    }

    private void putExtInfo(DatabaseConfig config, String key, String value) {
        if (StringUtil.isNotBlank(value)) {
            config.getExtInfo().setProperty(key, value.trim());
        }
    }
}
