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
package org.realtimeetl.connector.tidb;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.connector.mysql.MySQLConnector;
import org.realtimeetl.connector.tidb.constant.TiDBConstant;
import org.realtimeetl.connector.tidb.schema.TiDBSchemaResolver;
import org.realtimeetl.connector.tidb.validator.TiDBConfigValidator;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.schema.SchemaResolver;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TiDB Connector（CompatibilityMySQL )
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 00:10
 */
public final class TiDBConnector extends MySQLConnector {

    private static final Set<String> SYSTEM_DATABASES = Stream.of(
            "information_schema", "mysql", "performance_schema", "sys", "metrics_schema")
            .collect(Collectors.toSet());

    private final TiDBConfigValidator configValidator = new TiDBConfigValidator();
    private final TiDBSchemaResolver schemaResolver = new TiDBSchemaResolver();

    @Override
    public String getConnectorType() {
        return "TiDB";
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    @Override
    public ConnectorInstance connect(DatabaseConfig config, ConnectorServiceContext context) {
        TiDBConstant.enrichJdbcProperties(config);
        return super.connect(config, context);
    }

    @Override
    public List<String> getDatabases(DatabaseConnectorInstance connectorInstance) {
        return connectorInstance.execute(databaseTemplate -> {
            List<String> databases = databaseTemplate.queryForList("SHOW DATABASES", String.class);
            if (CollectionUtils.isEmpty(databases)) {
                return Collections.emptyList();
            }
            return databases.stream()
                    .filter(name -> !SYSTEM_DATABASES.contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        });
    }
}
