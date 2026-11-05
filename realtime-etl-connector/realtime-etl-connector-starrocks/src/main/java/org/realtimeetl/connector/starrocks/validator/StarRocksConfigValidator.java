/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.starrocks.validator;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.mysql.validator.MySQLConfigValidator;
import org.realtimeetl.connector.starrocks.constant.StarRocksConstant;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-07 03:00
 */
public final class StarRocksConfigValidator extends MySQLConfigValidator {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        StarRocksConstant.enrichJdbcProperties(connectorConfig);
        StarRocksConstant.enrichExtInfo(connectorConfig, params.get(StarRocksConstant.HTTP_PORT), params.get(StarRocksConstant.WRITE_MODE));
    }
}
