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
package org.realtimeetl.connector.oceanbaseoracle;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.oceanbaseoracle.schema.OceanBaseOracleSchemaResolver;
import org.realtimeetl.connector.oceanbaseoracle.validator.OceanBaseOracleConfigValidator;
import org.realtimeetl.connector.oracle.OracleConnector;
import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.connector.database.DatabaseTemplate;
import org.realtimeetl.sdk.connector.database.ds.SimpleConnection;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * OceanBase Oracle Compatibility ModeConnector。
 * <p>ReuseOracle dialects (two quotation marks,MERGE、Schema Namespace;Mappingand URL Useoceanbase-client。
 * First SupportFullandScheduledIncremental，Not supportedLog CDC。</p>
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-31 14:10
 */
public final class OceanBaseOracleConnector extends OracleConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(OceanBaseOracleConnector.class);

    private static final String QUERY_SCHEMA = "SELECT USERNAME FROM ALL_USERS ORDER BY USERNAME";

    /**
     * oceanbase-client Yes.DatabaseMetaData#getTables Yes.Oracle Mode CommonBackEmpty
     * （tableNamePattern=null / TypeIncreasing the number of filters is not compatible with the data dictionary.
     * <p>Database migrationtemporary physical tables;viewsSQL For later use:
     * {@code SELECT VIEW_NAME ... FROM ALL_VIEWS WHERE OWNER = ?}
     * {@code SELECT MVIEW_NAME ... FROM ALL_MVIEWS WHERE OWNER = ?}
     */
    private static final String QUERY_TABLES =
            "SELECT TABLE_NAME AS OBJECT_NAME, 'TABLE' AS OBJECT_TYPE FROM ALL_TABLES WHERE OWNER = ?";

    private static final String QUERY_COMPAT_MODE_V =
            "SELECT VALUE FROM V$OB_PARAMETERS WHERE NAME = 'ob_compatibility_mode'";

    private static final String QUERY_COMPAT_MODE_SHOW =
            "SHOW VARIABLES LIKE 'ob_compatibility_mode'";

    private static final String EXPECTED_MODE = "ORACLE";

    private final OceanBaseOracleConfigValidator configValidator = new OceanBaseOracleConfigValidator();
    private final OceanBaseOracleSchemaResolver schemaResolver = new OceanBaseOracleSchemaResolver();

    private final Set<String> SYSTEM_SCHEMAS = Stream.of(
                    "ANONYMOUS", "APPQOSSYS", "AUDSYS", "CTXSYS", "DBSFWUSER", "DBSNMP", "DVSYS",
                    "GSMADMIN_INTERNAL", "LBACSYS", "MDSYS", "OJVMSYS", "OLAPSYS",
                    "ORDDATA", "ORDSYS", "ORAAUDITOR", "OUTLN", "SYS", "SYSTEM",
                    "SYS_EXTERNAL_TBS", "WMSYS", "XDB", "XS$NULL",
                    "OCEANBASE", "__PUBLIC", "__RECYCLEBIN", "OCS")
            .collect(Collectors.toSet());

    @Override
    public String getConnectorType() {
        return "OceanBaseOracle";
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    /**
     * oceanbase-client Yes.{@code Connection#setSchema} Unrealised calls within{@code getSchema()}，
     * Throw{@link AbstractMethodError}。Not here.JDBC setSchema，schema OnlySaveFor exampleSQL OWNER Qualifications;
     * Toggle from{@code ALTER SESSION SET CURRENT_SCHEMA} Processing (e.g.,DDL）。
     */
    @Override
    public ConnectorInstance connect(DatabaseConfig config, ConnectorServiceContext context) {
        return new DatabaseConnectorInstance(config, context.getCatalog(), context.getSchema()) {
            @Override
            public Connection getConnection() throws Exception {
                return getDataSource().getConnection();
            }
        };
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isTiming(listenerType)) {
            return new DatabaseQuartzListener();
        }
        // Oracle Model tenants not applicableMySQL Binlog / Oracle LogMiner，Initial not supportedLogIncremental
        if (ListenerTypeEnum.isLog(listenerType)) {
            return null;
        }
        return null;
    }

    @Override
    public List<String> getSchemas(DatabaseConnectorInstance connectorInstance, String catalog) {
        String currentUser = resolveCurrentUser(connectorInstance);
        return connectorInstance.execute(databaseTemplate -> {
            List<String> schemas;
            try {
                schemas = databaseTemplate.queryForList(QUERY_SCHEMA, String.class);
            } catch (Exception e) {
                LOGGER.warn("Query ALL_USERS failed, fallback to current user: {}", e.getMessage());
                schemas = Collections.emptyList();
            }
            List<String> result = CollectionUtils.isEmpty(schemas)
                    ? new ArrayList<>()
                    : schemas.stream()
                    .filter(name -> name != null && !SYSTEM_SCHEMAS.contains(name.toUpperCase(Locale.ROOT)))
                    .collect(Collectors.toCollection(ArrayList::new));
            // OceanBase Oracle It's common for tenants.SYS@tenant Login；SYS Advances are filtered in the system list and keep the currentLoginUser
            ensureCurrentUserSchema(result, currentUser);
            if (result.isEmpty() && StringUtil.isNotBlank(currentUser)) {
                result.add(currentUser);
            }
            return result;
        });
    }

    /**
     * PressSchema(OWNER) Listing physical tables; not leavingJDBC DatabaseMetaData，Avoidoceanbase-client EmptyResult。
     * <p>Database migrationNot yet.BackView / Materialize view.
     */
    @Override
    public List<Table> getTable(DatabaseConnectorInstance connectorInstance, ConnectorServiceContext context) {
        String schema = context == null ? null : context.getSchema();
        if (StringUtil.isBlank(schema)) {
            schema = resolveCurrentUser(connectorInstance);
        }
        if (StringUtil.isBlank(schema)) {
            return Collections.emptyList();
        }
        final String owner = schema.trim().toUpperCase(Locale.ROOT);
        return connectorInstance.execute(databaseTemplate -> {
            // Database migrationNot Labor.ALL_VIEWS / ALL_MVIEWS
            List<Map<String, Object>> rows = databaseTemplate.queryForList(QUERY_TABLES, owner);
            if (CollectionUtils.isEmpty(rows)) {
                return Collections.emptyList();
            }
            List<Table> tables = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                Object nameValue = getIgnoreCase(row, "OBJECT_NAME");
                if (nameValue == null) {
                    nameValue = getIgnoreCase(row, "TABLE_NAME");
                }
                if (nameValue == null || StringUtil.isBlank(String.valueOf(nameValue))) {
                    continue;
                }
                Table table = new Table();
                table.setName(String.valueOf(nameValue));
                table.setType(resolveTableType(getIgnoreCase(row, "OBJECT_TYPE")));
                tables.add(table);
            }
            return tables;
        });
    }

    private static String resolveTableType(Object typeValue) {
        if (typeValue == null) {
            return TableTypeEnum.TABLE.getCode();
        }
        String type = String.valueOf(typeValue).trim().toUpperCase(Locale.ROOT);
        if (type.contains("VIEW") && type.contains("MATERIALIZED")) {
            return TableTypeEnum.MATERIALIZED_VIEW.getCode();
        }
        if ("VIEW".equals(type)) {
            return TableTypeEnum.VIEW.getCode();
        }
        return TableTypeEnum.TABLE.getCode();
    }

    private static Object getIgnoreCase(Map<String, Object> row, String key) {
        if (row == null || StringUtil.isBlank(key)) {
            return null;
        }
        Object value = row.get(key);
        if (value != null) {
            return value;
        }
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey() != null && key.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * FromConnectUsernameParsingSchema：{@code SYS@oracle_t1} → {@code SYS}。
     */
    private static String resolveCurrentUser(DatabaseConnectorInstance connectorInstance) {
        if (connectorInstance == null || connectorInstance.getConfig() == null) {
            return null;
        }
        String username = connectorInstance.getConfig().getUsername();
        if (StringUtil.isBlank(username)) {
            return null;
        }
        String trimmed = username.trim();
        int at = trimmed.indexOf('@');
        String user = at > 0 ? trimmed.substring(0, at) : trimmed;
        return StringUtil.isBlank(user) ? null : user.toUpperCase(Locale.ROOT);
    }

    private static void ensureCurrentUserSchema(List<String> schemas, String currentUser) {
        if (StringUtil.isBlank(currentUser) || schemas == null) {
            return;
        }
        boolean exists = schemas.stream().anyMatch(s -> currentUser.equalsIgnoreCase(s));
        if (!exists) {
            schemas.add(0, currentUser);
        }
    }

    @Override
    public String buildJdbcUrl(DatabaseConfig config, String database) {
        // jdbc:oceanbase://127.0.0.1:2881  （The tenant wroteUsername SYS@tenant）
        StringBuilder url = new StringBuilder();
        url.append("jdbc:oceanbase://").append(config.getHost()).append(":").append(config.getPort());
        if (StringUtil.isNotBlank(database)) {
            url.append("/").append(database.trim());
        }
        return url.toString();
    }

    /**
     * OceanBase Oracle Model criminalizesUser：Not supportedTEMPORARY TABLESPACE / QUOTA，OnlyIDENTIFIED BY[+DEFAULT TABLESPACE]。
     */
    @Override
    public String buildCreateDatabaseSql(String databaseName, String schemaName) {
        String user = schemaName.trim().toUpperCase(Locale.ROOT);
        String password = user + "@1234";
        String createUserSql = "CREATE USER " + user + " IDENTIFIED BY \"" + password + "\"";
        String grantSql = "GRANT CONNECT, RESOURCE TO " + user;
        return "DECLARE v_cnt NUMBER := 0; BEGIN " +
                "SELECT COUNT(1) INTO v_cnt FROM ALL_USERS WHERE USERNAME = '" + user + "'; " +
                "IF v_cnt = 0 THEN " +
                "EXECUTE IMMEDIATE '" + createUserSql + "'; " +
                "EXECUTE IMMEDIATE '" + grantSql + "'; " +
                "END IF; END;";
    }

    /**
     * RemoveSource table DDL：PriorityDBMS_METADATA；FailedTimeBackEmpty string, CitizensDatabase migrationPressFieldmetadata spelling statement.
     * <p>oceanbase-client / OB Oracle Yeah.GET_DDL、CLOB Supporting instability, incomprehensible.Oracle The same goes for a direct interruption of structural migration.
     */
    @Override
    public String getSourceTableDDL(DatabaseConnectorInstance sourceInstance, String sourceTableName) {
        if (sourceInstance == null || StringUtil.isBlank(sourceTableName)) {
            return StringUtil.EMPTY;
        }
        try {
            return sourceInstance.execute(databaseTemplate -> {
                String schema = resolveSchemaForDdl(sourceInstance);
                String table = sourceTableName.trim();
                String ddl = queryTableDdl(databaseTemplate, table, schema);
                if (StringUtil.isBlank(ddl)) {
                    String upperTable = table.toUpperCase(Locale.ROOT);
                    if (!upperTable.equals(table)) {
                        ddl = queryTableDdl(databaseTemplate, upperTable, schema);
                    }
                }
                return normalizeFetchedDdl(ddl, schema);
            });
        } catch (Exception e) {
            LOGGER.warn("OceanBaseOracle getSourceTableDDL failed for table={}, fallback to field DDL: {}",
                    sourceTableName, e.getMessage());
            return StringUtil.EMPTY;
        }
    }

    private static String resolveSchemaForDdl(DatabaseConnectorInstance sourceInstance) {
        String schema = sourceInstance.getSchema();
        if (StringUtil.isNotBlank(schema)) {
            return schema.trim().toUpperCase(Locale.ROOT);
        }
        return resolveCurrentUser(sourceInstance);
    }

    private String queryTableDdl(DatabaseTemplate databaseTemplate, String tableName, String schema) {
        try {
            if (StringUtil.isNotBlank(schema)) {
                return databaseTemplate.queryForObject(
                        "SELECT DBMS_METADATA.GET_DDL('TABLE', ?, ?) FROM DUAL",
                        (ResultSet rs, int rowNum) -> readDdlValue(rs),
                        tableName, schema);
            }
            return databaseTemplate.queryForObject(
                    "SELECT DBMS_METADATA.GET_DDL('TABLE', ?) FROM DUAL",
                    (ResultSet rs, int rowNum) -> readDdlValue(rs),
                    tableName);
        } catch (Exception e) {
            LOGGER.debug("DBMS_METADATA.GET_DDL failed table={}, schema={}: {}", tableName, schema, e.getMessage());
            return StringUtil.EMPTY;
        }
    }

    private static String readDdlValue(ResultSet rs) throws java.sql.SQLException {
        Object value = rs.getObject(1);
        if (value == null) {
            return StringUtil.EMPTY;
        }
        if (value instanceof Clob) {
            Clob clob = (Clob) value;
            if (clob.length() == 0) {
                return StringUtil.EMPTY;
            }
            return clob.getSubString(1, (int) clob.length());
        }
        return String.valueOf(value);
    }

    private static String normalizeFetchedDdl(String ddl, String schema) {
        if (StringUtil.isBlank(ddl)) {
            return StringUtil.EMPTY;
        }
        ddl = ddl.trim();
        if (StringUtil.isNotBlank(schema)) {
            String quotedSchema = "\"" + schema.toUpperCase(Locale.ROOT) + "\"";
            ddl = ddl.replace(quotedSchema + ".", "");
            quotedSchema = "\"" + schema + "\"";
            ddl = ddl.replace(quotedSchema + ".", "");
        }
        if (ddl.endsWith(";")) {
            ddl = ddl.substring(0, ddl.length() - 1).trim();
        }
        return ddl;
    }

    /**
     * OceanBase Yeah.PARALLEL hint Limited support, generalCOUNT It's okay.
     */
    @Override
    public String getQueryCountSql(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        String queryFilter = config.getQueryFilter();
        String query = "SELECT COUNT(*) FROM %s%s t %s";
        return String.format(query, config.getSchema(), database.buildWithQuotation(config.getTableName()), queryFilter);
    }

    /**
     * oceanbase-client Not achieved{@link java.sql.Connection#getSchema()}，Don't fall back.MappingMethodology. became the first step forward.
     */
    @Override
    protected String getSchema(String schema, java.sql.Connection connection) {
        if (StringUtil.isNotBlank(schema)) {
            return schema.toUpperCase(Locale.ROOT);
        }
        return schema;
    }

    @Override
    public boolean isAlive(DatabaseConnectorInstance connectorInstance) {
        boolean alive = super.isAlive(connectorInstance);
        if (alive) {
            validateCompatibilityMode(connectorInstance);
        }
        return alive;
    }

    /**
     * Verify the current tenant asOracle Compatible mode. Avoid error.MySQL Tenants cause a subsequent paymentData SQL Failed。
     */
    private void validateCompatibilityMode(DatabaseConnectorInstance connectorInstance) {
        String mode = resolveCompatibilityMode(connectorInstance);
        if (StringUtil.isBlank(mode)) {
            LOGGER.warn("Unable to detect OceanBase ob_compatibility_mode, skip mode check");
            return;
        }
        if (!EXPECTED_MODE.equalsIgnoreCase(mode.trim())) {
            throw new SdkException("Current Tenant Compatibility Mode " + mode + ", OceanBaseOracle connector only supports Oracle mode."
                    + "Please change to the OceanBase connector or connect to ob compatibility mode=oracle.");
        }
    }

    private String resolveCompatibilityMode(DatabaseConnectorInstance connectorInstance) {
        try {
            return connectorInstance.execute(databaseTemplate ->
                    databaseTemplate.queryForObject(QUERY_COMPAT_MODE_V, String.class));
        } catch (Exception e) {
            LOGGER.debug("Query V$OB_PARAMETERS failed: {}", e.getMessage());
        }
        try {
            return connectorInstance.execute(databaseTemplate -> {
                List<Map<String, Object>> rows = databaseTemplate.queryForList(QUERY_COMPAT_MODE_SHOW);
                if (CollectionUtils.isEmpty(rows)) {
                    return null;
                }
                Map<String, Object> row = rows.get(0);
                Object value = row.get("Value");
                if (value == null) {
                    value = row.get("VALUE");
                }
                if (value == null) {
                    value = row.get("value");
                }
                return value == null ? null : String.valueOf(value);
            });
        } catch (Exception e) {
            LOGGER.debug("SHOW VARIABLES ob_compatibility_mode failed: {}", e.getMessage());
            return null;
        }
    }
}
