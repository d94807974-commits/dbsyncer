/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
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
