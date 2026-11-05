/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
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
 * @author The clouds.
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
