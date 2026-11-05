/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.oceanbaseoracle.validator;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.AbstractDataBaseConfigValidator;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;

/**
 * OceanBase Oracle ModeConnectConfigureVerify INTRODUCTION
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-31 14:10
 */
public final class OceanBaseOracleConfigValidator extends AbstractDataBaseConfigValidator {

    private static final String DEFAULT_DRIVER = "com.oceanbase.jdbc.Driver";

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName(DEFAULT_DRIVER);
        }
        connectorConfig.setUrl(connectorService.buildJdbcUrl(connectorConfig, connectorConfig.getDatabase()));
    }
}
