/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.postgresql;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.postgresql.cdc.PostgreSQLListener;
import org.realtimeetl.connector.postgresql.schema.PostgreSQLSchemaResolver;
import org.realtimeetl.connector.postgresql.validator.PostgreSQLConfigValidator;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.connector.database.ds.SimpleConnection;
import org.realtimeetl.sdk.constant.DatabaseConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.PageSql;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * PostgreSQLConnectorAchieved
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022-05-22 22:56
 */
public class PostgreSQLConnector extends AbstractDatabaseConnector {

    private final String QUERY_DATABASE = "SELECT datname FROM pg_database WHERE datistemplate = FALSE order by datname";
    private final String QUERY_SCHEMA = "SELECT schema_name FROM information_schema.schemata WHERE schema_name NOT LIKE 'pg_%' AND schema_name NOT IN ('information_schema') ORDER BY schema_name";

    /**
     * List normal tables/minSector EntitiesMain Table/view/materialize view;exclusionminArea, avoidandMain TableRepeat map.
     * minSector EntitiesMain TableTypeUnitTABLE，Compatible with existingTableTypeEnum andIncrementalFilter logic.
     */
    private static final String QUERY_TABLE = "SELECT c.relname AS table_name, "
            + "CASE c.relkind WHEN 'v' THEN 'VIEW' WHEN 'm' THEN 'MATERIALIZED VIEW' ELSE 'TABLE' END AS table_type "
            + "FROM pg_class c "
            + "INNER JOIN pg_namespace n ON n.oid = c.relnamespace "
            + "WHERE n.nspname = ? "
            + "AND c.relkind IN ('r', 'p', 'v', 'm') "
            + "AND COALESCE(c.relispartition, false) = false "
            + "ORDER BY c.relname";

    private final PostgreSQLConfigValidator configValidator = new PostgreSQLConfigValidator();
    private final PostgreSQLSchemaResolver schemaResolver = new PostgreSQLSchemaResolver();

    @Override
    public String getConnectorType() {
        return "PostgreSQL";
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
    public ConnectorInstance connect(DatabaseConfig config, ConnectorServiceContext context) {
        String catalog = context.getCatalog();
        if (StringUtil.isNotBlank(catalog)) {
            DatabaseConfig effectiveConfig = copyDatabaseConfig(config);
            effectiveConfig.setUrl(buildJdbcUrl(config, catalog));
            effectiveConfig.setDatabase(catalog);
            return new DatabaseConnectorInstance(effectiveConfig, catalog, context.getSchema());
        }
        return super.connect(config, context);
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
    public List<String> getDatabases(DatabaseConnectorInstance connectorInstance) {
        return connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(QUERY_DATABASE, String.class));
    }

    @Override
    public List<String> getSchemas(DatabaseConnectorInstance connectorInstance, String catalog) {
        if (StringUtil.isBlank(catalog)) {
            return connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(QUERY_SCHEMA, String.class));
        }
        DatabaseConfig effectiveConfig = copyDatabaseConfig(connectorInstance.getConfig());
        effectiveConfig.setUrl(buildJdbcUrl(connectorInstance.getConfig(), catalog));
        effectiveConfig.setDatabase(catalog);
        DatabaseConnectorInstance catalogInstance = new DatabaseConnectorInstance(effectiveConfig, catalog, null);
        try {
            return catalogInstance.execute(databaseTemplate -> databaseTemplate.queryForList(QUERY_SCHEMA, String.class));
        } finally {
            catalogInstance.close();
        }
    }

    @Override
    public String buildSqlWithQuotation() {
        return "\"";
    }

    /**
     * schema Notpublic TimeGenerate CREATE SCHEMA；NoCreate DataLibrary。
     * Database migrationYes.minTwo-step call: build firstLibrary（schema It's empty!Target databaseConstructionschema。
     */
    @Override
    public String buildCreateDatabaseSql(String databaseName, String schemaName) {
        if (StringUtil.isNotBlank(schemaName) && !isDefaultSchema(schemaName)) {
            return "CREATE SCHEMA IF NOT EXISTS " + buildWithQuotation(schemaName);
        }
        if (StringUtil.isBlank(databaseName)) {
            return StringUtil.EMPTY;
        }
        return "CREATE DATABASE " + buildWithQuotation(databaseName);
    }

    /**
     * Verify onlyLibrary：schema is empty orpublic Time only.pg_database；
     * Verifyschema：Caller must haveConnectPresent.Target database，CASELibraryYes.pg_namespace。
     */
    @Override
    public boolean databaseExists(DatabaseConnectorInstance connectorInstance, String databaseName, String schemaName) {
        if (StringUtil.isBlank(schemaName) || isDefaultSchema(schemaName)) {
            if (StringUtil.isBlank(databaseName)) {
                return true;
            }
            Integer count = connectorInstance.execute(databaseTemplate ->
                    databaseTemplate.queryForObject("SELECT COUNT(1) FROM pg_database WHERE datname = ?", Integer.class, databaseName));
            return count != null && count > 0;
        }
        Integer count = connectorInstance.execute(databaseTemplate ->
                databaseTemplate.queryForObject(
                        "SELECT COUNT(1) FROM pg_namespace WHERE nspname = ?",
                        Integer.class,
                        schemaName));
        return count != null && count > 0;
    }

    private static boolean isDefaultSchema(String schemaName) {
        return StringUtil.equalsIgnoreCase("public", schemaName);
    }

    @Override
    public String getTargetTableDDL(DatabaseConnectorInstance targetInstance, String tableName, String sourceDDL) {
        String schema = targetInstance != null ? targetInstance.getSchema() : null;
        String qualifiedTable = qualifyTableName(schema, tableName);
        return "CREATE TABLE IF NOT EXISTS " + qualifiedTable + " (" + sourceDDL + ")";
    }

    @Override
    public String getSourceTableDDL(DatabaseConnectorInstance sourceInstance, String sourceTableName) {
        return StringUtil.EMPTY;
    }

    @Override
    public String buildDropTableSql(DatabaseConnectorInstance targetInstance, String tableName) {
        String schema = targetInstance != null ? targetInstance.getSchema() : null;
        return "DROP TABLE IF EXISTS " + qualifyTableName(schema, tableName) + " CASCADE";
    }

    String qualifyTableName(String schema, String tableName) {
        String table = buildWithQuotation(tableName);
        if (StringUtil.isBlank(schema)) {
            return table;
        }
        return buildWithQuotation(schema) + "." + table;
    }

    private DatabaseConfig copyDatabaseConfig(DatabaseConfig source) {
        DatabaseConfig target = new DatabaseConfig();
        target.setConnectorType(source.getConnectorType());
        target.setDriverClassName(source.getDriverClassName());
        target.setHost(source.getHost());
        target.setPort(source.getPort());
        target.setUsername(source.getUsername());
        target.setPassword(source.getPassword());
        target.setMaxActive(source.getMaxActive());
        target.setKeepAlive(source.getKeepAlive());
        target.setDatabase(source.getDatabase());
        target.setServiceName(source.getServiceName());
        target.setUrl(source.getUrl());
        if (source.getProperties() != null) {
            Properties properties = new Properties();
            properties.putAll(source.getProperties());
            target.setProperties(properties);
        }
        if (source.getExtInfo() != null) {
            Properties extInfo = new Properties();
            extInfo.putAll(source.getExtInfo());
            target.setExtInfo(extInfo);
        }
        return target;
    }

    @Override
    public String getPageSql(PageSql config) {
        StringBuilder sql = new StringBuilder(config.getQuerySql());
        // Use of the base category methodAddORDER BY（PressPrimary keySort, promiseminPage)
        appendOrderByPrimaryKeys(sql, config);
        sql.append(DatabaseConstant.POSTGRESQL_PAGE_SQL);
        return sql.toString();
    }

    @Override
    public Object[] getPageArgs(ReaderContext context) {
        int pageSize = context.getPageSize();
        int pageIndex = context.getPageIndex();
        return new Object[]{pageSize, (pageIndex - 1) * pageSize};
    }

    @Override
    public String getPageCursorSql(PageSql config) {
        // Cursor query not supported
        if (!PrimaryKeyUtil.isSupportedCursor(config.getFields())) {
            return StringUtil.EMPTY;
        }

        StringBuilder sql = new StringBuilder(config.getQuerySql());
        // Use of base category publicTotalMethod BuildWHEREConditionandORDER BY
        buildCursorConditionAndOrderBy(sql, config);
        sql.append(DatabaseConstant.POSTGRESQL_PAGE_SQL);
        return sql.toString();
    }

    @Override
    public Object[] getPageCursorArgs(ReaderContext context) {
        int pageSize = context.getPageSize();
        Object[] cursors = context.getCursors();
        if (null == cursors || cursors.length == 0) {
            return new Object[]{pageSize, 0};
        }
        // Use of base category publicTotalMethod to Build CursorConditionParameter
        Object[] cursorArgs = buildCursorArgs(cursors);
        if (cursorArgs == null) {
            return new Object[]{pageSize, 0};
        }

        // PostgreSQLUseLIMIT ? OFFSET ?，ParameterOrder is[CursorParameter..., pageSize, 0]
        Object[] newCursors = new Object[cursorArgs.length + 2];
        System.arraycopy(cursorArgs, 0, newCursors, 0, cursorArgs.length);
        newCursors[cursorArgs.length] = pageSize; // LIMIT
        newCursors[cursorArgs.length + 1] = 0; // OFFSET
        return newCursors;
    }

    @Override
    public String buildModifyColumnsSql(DatabaseConnectorInstance targetInstance, ValidateSyncTask task,
                                        String targetTableName, List<Field> sourceDefinitions,
                                        List<String> targetColumnNames) {
        if (CollectionUtils.isEmpty(sourceDefinitions) || CollectionUtils.isEmpty(targetColumnNames)) {
            return StringUtil.EMPTY;
        }
        int loopSize = Math.min(sourceDefinitions.size(), targetColumnNames.size());
        String qualifiedTable = qualifyTable(task, targetTableName);
        List<String> clauses = new ArrayList<>(loopSize);
        for (int i = 0; i < loopSize; i++) {
            Field sourceField = sourceDefinitions.get(i);
            String targetColumn = targetColumnNames.get(i);
            if (sourceField == null || StringUtil.isBlank(targetColumn)) {
                continue;
            }
            String col = buildWithQuotation(targetColumn);
            String type = formatPhysicalType(sourceField);
            String usingExpr = buildUsingExpr(col, type);
            clauses.add(String.format(Locale.ROOT, "ALTER COLUMN %s TYPE %s USING %s", col, type, usingExpr));
        }
        if (clauses.isEmpty()) {
            return StringUtil.EMPTY;
        }
        return String.format(Locale.ROOT, "ALTER TABLE %s %s", qualifiedTable, StringUtil.join(clauses, ", "));
    }

    private String qualifyTable(ValidateSyncTask task, String tableName) {
        String schema = StringUtil.isNotBlank(task.getTargetSchema()) ? task.getTargetSchema() : "public";
        return buildWithQuotation(schema) + "." + buildWithQuotation(tableName);
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    @Override
    protected String getSchema(String schema, Connection connection) {
        return StringUtil.isNotBlank(schema) ? schema : "public";
    }

    @Override
    public String buildJdbcUrl(DatabaseConfig config, String database) {
        // jdbc:postgresql://127.0.0.1:5432/postgres
        StringBuilder url = new StringBuilder();
        url.append("jdbc:postgresql://").append(config.getHost()).append(":").append(config.getPort()).append("/");
        if (StringUtil.isNotBlank(database)) {
            url.append(database);
        }
        return url.toString();
    }

    @Override
    public String buildInsertSql(SqlBuilderConfig config) {
        // PostgreSQL UseON CONFLICT DO NOTHING AchievedINSERT IGNORE Effects
        UpsertContext context = buildUpsertContext(config);
        StringBuilder sql = new StringBuilder(config.getDatabase().generateUniqueCode());

        // BuildINSERT INTO ... VALUES (...)
        buildInsertIntoClause(sql, config, context);

        // BuildON CONFLICT (...) DO NOTHING
        buildOnConflictClause(sql, context);
        sql.append(" DO NOTHING");

        return sql.toString();
    }

    @Override
    public String buildUpsertSql(DatabaseConnectorInstance connectorInstance, SqlBuilderConfig config) {
        UpsertContext context = buildUpsertContext(config);
        StringBuilder sql = new StringBuilder(config.getDatabase().generateUniqueCode());

        // BuildINSERT INTO ... VALUES (...)
        buildInsertIntoClause(sql, config, context);

        // BuildON CONFLICT (...) DO UPDATE SET
        buildOnConflictClause(sql, context);
        sql.append(" DO UPDATE SET ");
        sql.append(StringUtil.join(context.updateSets, StringUtil.COMMA));

        return sql.toString();
    }

    /**
     * BuildINSERT INTO ... VALUES (...) Sub sentences
     */
    private void buildInsertIntoClause(StringBuilder sql, SqlBuilderConfig config, UpsertContext context) {
        sql.append("INSERT INTO ").append(config.getSchema());
        sql.append(config.getDatabase().buildWithQuotation(config.getTableName()));
        sql.append("(").append(StringUtil.join(context.fieldNames, StringUtil.COMMA)).append(") ");
        sql.append("OVERRIDING SYSTEM VALUE VALUES (").append(StringUtil.join(context.valuePlaceholders, StringUtil.COMMA)).append(")");
    }

    /**
     * BuildON CONFLICT (...) Sub sentences
     */
    private void buildOnConflictClause(StringBuilder sql, UpsertContext context) {
        sql.append(" ON CONFLICT (");
        sql.append(StringUtil.join(context.pkFieldNames, StringUtil.COMMA));
        sql.append(")");
    }

    private String buildUsingExpr(String col, String type) {
        String normalizedType = StringUtil.trim(type).toUpperCase(Locale.ROOT);
        if (isIntegerType(normalizedType)) {
            // Non-integer string set toNULL，Avoidtimestamp/text -> int Direct error interrupted whole batchDDL
            return String.format(Locale.ROOT,
                    "CASE WHEN %1$s IS NULL THEN NULL WHEN (%1$s)::text ~ '^-?[0-9]+$' THEN (%1$s)::text::%2$s ELSE NULL END",
                    col, type);
        }
        if (isDecimalType(normalizedType)) {
            // DecimalObjective CombinedTypeSame safe.Convert，NoneMethod Resolution As FacilitatingNULL
            return String.format(Locale.ROOT,
                    "CASE WHEN %1$s IS NULL THEN NULL WHEN (%1$s)::text ~ '^-?[0-9]+(\\.[0-9]+)?$' THEN (%1$s)::text::%2$s ELSE NULL END",
                    col, type);
        }
        return String.format(Locale.ROOT, "%s::text::%s", col, type);
    }

    private boolean isIntegerType(String type) {
        return "INT2".equals(type) || "INT4".equals(type) || "INT8".equals(type)
                || "SMALLINT".equals(type) || "INTEGER".equals(type) || "BIGINT".equals(type);
    }

    private boolean isDecimalType(String type) {
        return "REAL".equals(type) || "DOUBLE PRECISION".equals(type)
                || type.startsWith("NUMERIC") || type.startsWith("DECIMAL");
    }

    @Override
    public String formatPhysicalType(Field sourceDefinition) {
        if (sourceDefinition == null || StringUtil.isBlank(sourceDefinition.getTypeName())) {
            return super.formatPhysicalType(sourceDefinition);
        }
        String t = sourceDefinition.getTypeName().trim().toUpperCase(Locale.ROOT);
        String serialMapped = mapPostgreSqlSerialToInteger(t);
        if (serialMapped != null) {
            return serialMapped;
        }
        String floatMapped = mapPostgreSqlFloatAliases(t);
        if (floatMapped != null) {
            return floatMapped;
        }
        return super.formatPhysicalType(sourceDefinition);
    }

    /**
     * ALTER TYPE Unable to useSERIAL FalseType，Map to Bottom IntegerType。
     */
    private static String mapPostgreSqlSerialToInteger(String t) {
        if ("SERIAL".equals(t)) {
            return "INT4";
        }
        if ("BIGSERIAL".equals(t)) {
            return "INT8";
        }
        if ("SMALLSERIAL".equals(t)) {
            return "INT2";
        }
        if ("BYTEA".equals(t)) {
            return "BYTEA";
        }
        String rawTypeName = PostgreSQLSchemaResolver.normalizeTypeName(t);
        if (PostgreSQLSchemaResolver.isArrayType(rawTypeName)) {
            return t;
        }
        return null;
    }

    /**
     * FLOAT4/8 Wait.andBasebandOutlook saluteIn ADOPTION OF CONFLICT, replaceDDL Norms inName。
     */
    private static String mapPostgreSqlFloatAliases(String t) {
        if ("FLOAT4".equals(t) || "REAL".equals(t)) {
            return "REAL";
        }
        if ("FLOAT8".equals(t) || "DOUBLE PRECISION".equals(t)) {
            return "DOUBLE PRECISION";
        }
        return null;
    }

    /**
     * BuildUPSERT ContextField、Primary keyWait.Info）
     */
    private UpsertContext buildUpsertContext(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        UpsertContext context = new UpsertContext();

        config.getFields().forEach(f -> {
            String fieldName = database.buildWithQuotation(f.getName());
            context.fieldNames.add(fieldName);

            // BuildVALUES Placeholder
            List<String> fieldVs = new ArrayList<>();
            if (database.buildCustomValue(fieldVs, f)) {
                // CustomValueOrganisationgeometry Type）
                context.valuePlaceholders.add(fieldVs.get(0));
            } else {
                context.valuePlaceholders.add("?");
            }

            if (f.isPk()) {
                context.pkFieldNames.add(fieldName);
            } else {
                // UPDATE SET fieldName = EXCLUDED.fieldName
                context.updateSets.add(String.format("%s = EXCLUDED.%s", fieldName, fieldName));
            }
        });

        return context;
    }

    /**
     * UPSERT ContextWow.
     */
    private static class UpsertContext {

        List<String> fieldNames = new ArrayList<>();
        List<String> valuePlaceholders = new ArrayList<>();
        List<String> pkFieldNames = new ArrayList<>();
        List<String> updateSets = new ArrayList<>();
    }

}