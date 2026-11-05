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
package org.realtimeetl.connector.gaussdbmysql;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.connector.gaussdbmysql.schema.GaussDBMySQLSchemaResolver;
import org.realtimeetl.connector.gaussdbmysql.validator.GaussDBMySQLConfigValidator;
import org.realtimeetl.connector.mysql.MySQLConnector;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.schema.SchemaResolver;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * GaussDB MySQL CompatibilityConnector（Applicable toGaussDB for MySQL）
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-29 14:50
 */
public final class GaussDBMySQLConnector extends MySQLConnector {

    private static final Set<String> SYSTEM_DATABASES = Stream.of(
                    "information_schema", "mysql", "performance_schema", "sys")
            .collect(Collectors.toSet());

    private final GaussDBMySQLConfigValidator configValidator = new GaussDBMySQLConfigValidator();
    private final GaussDBMySQLSchemaResolver schemaResolver = new GaussDBMySQLSchemaResolver();

    @Override
    public String getConnectorType() {
        return "GaussDBMySQL";
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
