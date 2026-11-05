/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.gaussdbmysql.validator;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.gaussdbmysql.constant.GaussDBMySQLConfigConstant;
import org.realtimeetl.connector.mysql.validator.MySQLConfigValidator;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;

/**
 * GaussDB MySQL CompatibilityConfigureVerify INTRODUCTION
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-29 14:50
 */
public final class GaussDBMySQLConfigValidator extends MySQLConfigValidator {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName(GaussDBMySQLConfigConstant.DEFAULT_DRIVER);
        }
    }
}
