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
package org.realtimeetl.connector.gaussdb;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.gaussdb.schema.GaussDBSchemaResolver;
import org.realtimeetl.connector.gaussdb.validator.GaussDBConfigValidator;
import org.realtimeetl.connector.postgresql.PostgreSQLConnector;
import org.realtimeetl.connector.postgresql.cdc.PostgreSQLListener;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.connector.database.ds.SimpleConnection;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.schema.SchemaResolver;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GaussDB Connector（PostgreSQL Agreement, applicable toopenGauss / GaussDB）
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-29 14:50
 */
public final class GaussDBConnector extends PostgreSQLConnector {

    /**
     * GaussDB / openGauss None pg_class.relispartition（PostgreSQL 10+），
     * Usepg_inherits Remove the inheritance watch, avoid the child.minSector EntitiesandMain TableRepeat.
     */
    private static final String QUERY_TABLE = "SELECT c.relname AS table_name, "
            + "CASE c.relkind WHEN 'v' THEN 'VIEW' WHEN 'm' THEN 'MATERIALIZED VIEW' ELSE 'TABLE' END AS table_type "
            + "FROM pg_class c "
            + "INNER JOIN pg_namespace n ON n.oid = c.relnamespace "
            + "WHERE n.nspname = ? "
            + "AND c.relkind IN ('r', 'p', 'v', 'm') "
            + "AND NOT EXISTS (SELECT 1 FROM pg_inherits i WHERE i.inhrelid = c.oid) "
            + "ORDER BY c.relname";

    private final GaussDBConfigValidator configValidator = new GaussDBConfigValidator();
    private final GaussDBSchemaResolver schemaResolver = new GaussDBSchemaResolver();

    @Override
    public String getConnectorType() {
        return "GaussDB";
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isTiming(listenerType)) {
            return new DatabaseQuartzListener();
        }
        if (ListenerTypeEnum.isLog(listenerType)) {
            return new PostgreSQLListener();
        }
        return null;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    @Override
    public List<Table> getTable(DatabaseConnectorInstance connectorInstance, ConnectorServiceContext context) {
        return connectorInstance.execute(databaseTemplate -> {
            SimpleConnection connection = databaseTemplate.getSimpleConnection();
            Connection conn = connection.getConnection();
            String effectiveSchema = getSchema(context.getSchema(), conn);
            List<Map<String, Object>> rows = databaseTemplate.queryForList(QUERY_TABLE, effectiveSchema);
            if (CollectionUtils.isEmpty(rows)) {
                return new ArrayList<>();
            }
            List<Table> tables = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                Object nameValue = row.get("table_name");
                if (nameValue == null) {
                    continue;
                }
                Table table = new Table();
                table.setName(String.valueOf(nameValue));
                Object typeValue = row.get("table_type");
                table.setType(typeValue == null ? TableTypeEnum.TABLE.getCode() : String.valueOf(typeValue));
                tables.add(table);
            }
            return tables;
        });
    }

    @Override
    public String buildJdbcUrl(DatabaseConfig config, String database) {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:postgresql://").append(config.getHost()).append(":").append(config.getPort()).append("/");
        if (StringUtil.isNotBlank(database)) {
            url.append(database);
        }
        return url.toString();
    }

    /**
     * Conflict Neglect:GaussDB / openGauss UniversalON DUPLICATE KEY UPDATE NOTHING
     * （Do Not UsePostgreSQL Yes.ON CONFLICT / OVERRIDING SYSTEM VALUE）
     */
    @Override
    public String buildInsertSql(SqlBuilderConfig config) {
        UpsertParts parts = buildUpsertParts(config);
        return String.format("%sINSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE NOTHING",
                parts.uniqueCode, parts.table, parts.fieldNames, parts.values);
    }

    /**
     * OverwriteUpdate:GaussDB / openGauss UniversalON DUPLICATE KEY UPDATE col = VALUES(col)
     */
    @Override
    public String buildUpsertSql(DatabaseConnectorInstance connectorInstance, SqlBuilderConfig config) {
        UpsertParts parts = buildUpsertParts(config);
        if (StringUtil.isBlank(parts.duplicateUpdates)) {
            return String.format("%sINSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE NOTHING",
                    parts.uniqueCode, parts.table, parts.fieldNames, parts.values);
        }
        return String.format("%sINSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s",
                parts.uniqueCode, parts.table, parts.fieldNames, parts.values, parts.duplicateUpdates);
    }

    private UpsertParts buildUpsertParts(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        List<String> fieldNames = new ArrayList<>();
        List<String> valuePlaceholders = new ArrayList<>();
        List<String> duplicateUpdates = new ArrayList<>();

        for (Field field : config.getFields()) {
            String name = database.buildWithQuotation(field.getName());
            fieldNames.add(name);

            List<String> customValues = new ArrayList<>();
            if (database.buildCustomValue(customValues, field)) {
                valuePlaceholders.add(customValues.get(0));
            } else {
                valuePlaceholders.add("?");
            }

            if (!field.isPk()) {
                duplicateUpdates.add(String.format("%s = VALUES(%s)", name, name));
            }
        }

        UpsertParts parts = new UpsertParts();
        parts.uniqueCode = database.generateUniqueCode();
        parts.table = config.getSchema() + database.buildWithQuotation(config.getTableName());
        parts.fieldNames = StringUtil.join(fieldNames, StringUtil.COMMA);
        parts.values = StringUtil.join(valuePlaceholders, StringUtil.COMMA);
        parts.duplicateUpdates = StringUtil.join(duplicateUpdates, StringUtil.COMMA);
        return parts;
    }

    /**
     * INSERT / UPSERT Collapse MiddleResult
     */
    private static final class UpsertParts {
        private String uniqueCode;
        private String table;
        private String fieldNames;
        private String values;
        private String duplicateUpdates;
    }
}
