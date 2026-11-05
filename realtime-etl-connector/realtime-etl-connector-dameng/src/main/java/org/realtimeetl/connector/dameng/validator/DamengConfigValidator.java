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
package org.realtimeetl.connector.dameng.validator;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.dameng.constant.DamengConstant;
import org.realtimeetl.connector.oracle.validator.OracleConfigValidator;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;
import java.util.Properties;

/**
 * @author Realtime ETL.
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
