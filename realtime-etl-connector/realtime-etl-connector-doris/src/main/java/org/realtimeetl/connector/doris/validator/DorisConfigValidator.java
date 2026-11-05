/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.doris.validator;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.doris.constant.DorisConstant;
import org.realtimeetl.connector.mysql.validator.MySQLConfigValidator;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-06 21:00
 */
public final class DorisConfigValidator extends MySQLConfigValidator {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        DorisConstant.enrichJdbcProperties(connectorConfig);
        DorisConstant.enrichExtInfo(connectorConfig, params.get(DorisConstant.HTTP_PORT), params.get(DorisConstant.WRITE_MODE));
    }
}
