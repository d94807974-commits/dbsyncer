/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.dameng.validator;

import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.connector.dameng.constant.DamengConstant;
import org.dbsyncer.connector.oracle.validator.OracleConfigValidator;
import org.dbsyncer.sdk.config.DatabaseConfig;
import org.dbsyncer.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;
import java.util.Properties;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-07 02:00
 */
public final class DamengConfigValidator extends OracleConfigValidator {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName("dm.jdbc.driver.DmDriver");
        }
        DamengConstant.enrichJdbcProperties(connectorConfig);
        applySchemaProperty(connectorConfig, params.get("schema"));
        connectorConfig.setUrl(connectorService.buildJdbcUrl(connectorConfig, connectorConfig.getDatabase()));
    }

    /**
     * Dream through.ConnectProperties{@code schema} specifying current mode;when not completedMappingDefault UseandUsernameThe same name model.
     */
    private void applySchemaProperty(DatabaseConfig connectorConfig, String schema) {
        if (StringUtil.isBlank(schema)) {
            return;
        }
        Properties properties = connectorConfig.getProperties();
        if (properties == null) {
            properties = new Properties();
            connectorConfig.setProperties(properties);
        }
        properties.setProperty("schema", schema.trim());
    }
}
