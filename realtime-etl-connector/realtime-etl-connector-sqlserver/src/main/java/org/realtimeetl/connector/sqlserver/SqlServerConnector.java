/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.sqlserver;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.sqlserver.cdc.Lsn;
import org.realtimeetl.connector.sqlserver.cdc.SqlServerListener;
import org.realtimeetl.connector.sqlserver.schema.SqlServerSchemaResolver;
import org.realtimeetl.connector.sqlserver.validator.SqlServerConfigValidator;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.connector.database.DatabaseTemplate;
import org.realtimeetl.sdk.connector.database.ds.SimpleConnection;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.constant.DatabaseConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.PageSql;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;

import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * SqlServerConnectorAchieved
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2022-05-22 22:56
 */
public final class SqlServerConnector extends AbstractDatabaseConnector {
    private final String QUERY_DATABASE = "SELECT name FROM SYS.DATABASES WHERE database_id > 4 order by name";
    private final String QUERY_SCHEMA = "SELECT name FROM sys.schemas WHERE name NOT IN ('sys','INFORMATION_SCHEMA','db_owner','db_accessadmin','db_securityadmin','db_ddladmin','db_backupoperator','db_datareader','db_datawriter','db_denydatareader','db_denydatawriter') order by name";
    private final String QUERY_TABLE_IDENTITY = "select is_identity from sys.columns where object_id = object_id('%s') and is_identity > 0";
    private final String SET_TABLE_IDENTITY_ON = "set identity_insert %s.[%s] on;";
    private final String SET_TABLE_IDENTITY_OFF = ";set identity_insert %s.[%s] off;";

    private final SqlServerConfigValidator configValidator = new SqlServerConfigValidator();
    private final SqlServerSchemaResolver schemaResolver = new SqlServerSchemaResolver();

    @Override
    public String getConnectorType() {
        return "SqlServer";
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
            return new SqlServerListener();
        }
        return null;
    }

    @Override
    public List<String> getDatabases(DatabaseConnectorInstance connectorInstance) {
        return connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(QUERY_DATABASE, String.class));
    }

    @Override
    public List<String> getSchemas(DatabaseConnectorInstance connectorInstance, String catalog) {
        return connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(QUERY_SCHEMA, String.class));
    }

    @Override
    public String buildWithQuotation(String key) {
        return "[" + key + "]";
    }

    @Override
    public String buildCreateDatabaseSql(String databaseName, String schemaName) {
        if (StringUtil.isBlank(databaseName)) {
            return StringUtil.EMPTY;
        }
        String quoted = buildWithQuotation(databaseName);
        return "IF NOT EXISTS (SELECT 1 FROM sys.databases WHERE name = N'" + databaseName + "') CREATE DATABASE " + quoted;
    }

    @Override
    public boolean databaseExists(DatabaseConnectorInstance connectorInstance, String databaseName, String schemaName) {
        if (StringUtil.isBlank(databaseName)) {
            return false;
        }
        Integer count = connectorInstance.execute(databaseTemplate ->
                databaseTemplate.queryForObject("SELECT COUNT(1) FROM sys.databases WHERE name = ?", Integer.class, databaseName));
        return count != null && count > 0;
    }

    @Override
    public String getTargetTableDDL(DatabaseConnectorInstance targetInstance, String tableName, String sourceDDL) {
        String createSql = "CREATE TABLE " + tableName + " (" + sourceDDL + ")";
        String escapedTableName = tableName.replace("'", "''");
        return "IF OBJECT_ID(N'" + escapedTableName + "', N'U') IS NULL BEGIN " + createSql + " END";
    }

    @Override
    public String getSourceTableDDL(DatabaseConnectorInstance sourceInstance, String sourceTableName) {
        return StringUtil.EMPTY;
    }

    @Override
    public String buildDropTableSql(DatabaseConnectorInstance targetInstance, String tableName) {
        String quoted = buildWithQuotation(tableName);
        String dropSql = "DROP TABLE " + quoted;
        String escapedTableName = tableName.replace("'", "''");
        return "IF OBJECT_ID(N'" + escapedTableName + "', N'U') IS NOT NULL " + dropSql;
    }

    @Override
    public String getPageSql(PageSql config) {
        List<String> primaryKeys = buildPrimaryKeys(config.getPrimaryKeys());
        String orderBy = StringUtil.join(primaryKeys, StringUtil.COMMA);
        return String.format(DatabaseConstant.SQLSERVER_PAGE_SQL, orderBy, config.getQuerySql());
    }

    @Override
    public Object[] getPageArgs(ReaderContext context) {
        int pageSize = context.getPageSize();
        int pageIndex = context.getPageIndex();
        return new Object[]{(pageIndex - 1) * pageSize + 1, pageIndex * pageSize};
    }

    @Override
    public String getPageCursorSql(PageSql config) {
        // Cursor query not supported
        if (!PrimaryKeyUtil.isSupportedCursor(config.getFields())) {
            return StringUtil.EMPTY;
        }

        // Build with CursorConditionQuerySQL（OnlyAddWHERECondition，Oh, no.AddORDER BY）
        StringBuilder innerSql = new StringBuilder(config.getQuerySql());
        buildCursorConditionOnly(innerSql, config);

        // UseROW_NUMBER() ModalitiesminPage（CompatibilitySQL Server 2008+）
        // OuterORDER BY Already.ROW_NUMBER() OVER(ORDER BY ...) Processing
        List<String> primaryKeys = buildPrimaryKeys(config.getPrimaryKeys());
        String orderBy = StringUtil.join(primaryKeys, StringUtil.COMMA);
        return String.format(DatabaseConstant.SQLSERVER_PAGE_SQL, orderBy, innerSql);
    }

    @Override
    public Object[] getPageCursorArgs(ReaderContext context) {
        int pageSize = context.getPageSize();
        Object[] cursors = context.getCursors();
        if (null == cursors || cursors.length == 0) {
            // First footnote:Page：BETWEEN 1 AND pageSize
            return new Object[]{1, pageSize};
        }
        // Use of base category publicTotalMethod to Build CursorConditionParameter
        Object[] cursorArgs = buildCursorArgs(cursors);
        if (cursorArgs == null) {
            return new Object[]{1, pageSize};
        }

        // SQL ServerUseROW_NUMBER() Modalities Underlines:[CursorParameter..., 1, pageSize]
        Object[] newCursors = new Object[cursorArgs.length + 2];
        System.arraycopy(cursorArgs, 0, newCursors, 0, cursorArgs.length);
        newCursors[cursorArgs.length] = 1; // BETWEEN Start
        newCursors[cursorArgs.length + 1] = pageSize; // BETWEEN End
        return newCursors;
    }

    @Override
    public String buildModifyColumnsSql(DatabaseConnectorInstance targetInstance, ValidateSyncTask task,
                                        String targetTableName, List<Field> sourceDefinitions,
                                        List<String> targetColumnNames) {
        if (CollectionUtils.isEmpty(sourceDefinitions) || CollectionUtils.isEmpty(targetColumnNames)) {
            return StringUtil.EMPTY;
        }
        int size = Math.min(sourceDefinitions.size(), targetColumnNames.size());

        String qualifiedTable = qualifyTable(task, targetTableName);
        List<String> sqlList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Field sourceField = sourceDefinitions.get(i);
            String targetColumn = targetColumnNames.get(i);
            if (sourceField == null || StringUtil.isBlank(targetColumn)) {
                continue;
            }
            String col = buildWithQuotation(targetColumn);
            String type = formatPhysicalType(sourceField);
            sqlList.add(String.format(Locale.ROOT, "ALTER TABLE %s ALTER COLUMN %s %s", qualifiedTable, col, type));
        }
        if (sqlList.isEmpty()) {
            return StringUtil.EMPTY;
        }
        return StringUtil.join(sqlList, ";");
    }

    @Override
    public String formatPhysicalType(Field sourceDefinition) {
        if (sourceDefinition == null || StringUtil.isBlank(sourceDefinition.getTypeName())) {
            return super.formatPhysicalType(sourceDefinition);
        }
        String t = sourceDefinition.getTypeName().trim().toUpperCase(Locale.ROOT);
        int size = Math.max(0, sourceDefinition.getColumnSize());

        if (t.contains("VARBINARY")) {
            if (size > 8000) {
                return t + "(MAX)";
            }
        }

        if (t.contains("VARCHAR")) {
            // Length > 8000 → It has to be used.MAX
            if (size > 8000) {
                return t + "(MAX)";
            }
        }
        return super.formatPhysicalType(sourceDefinition);
    }

    /**
     * OptionalLibraryPrefix{@code [db].[schema].[table]}；Onlyschema + Table nameYeah.
     */
    private String qualifyTable(ValidateSyncTask task, String tableName) {
        String schema = StringUtil.isNotBlank(task.getTargetSchema()) ? task.getTargetSchema() : "dbo";
        String schemaTable = buildWithQuotation(schema) + "." + buildWithQuotation(tableName);
        if (StringUtil.isBlank(task.getTargetDatabase())) {
            return schemaTable;
        }
        return "[" + task.getTargetDatabase() + "]." + schemaTable;
    }

    @Override
    public Map<String, String> getTargetCommand(CommandConfig commandConfig) {
        Map<String, String> targetCommand = super.getTargetCommand(commandConfig);
        String tableName = commandConfig.getTable().getName();
        // Table of judgementYesNoInclude identification as added
        DatabaseConnectorInstance db = (DatabaseConnectorInstance) commandConfig.getConnectorInstance();
        List<Integer> result = db.execute(databaseTemplate -> databaseTemplate.queryForList(String.format(QUERY_TABLE_IDENTITY, tableName), Integer.class));
        // Allows visible insertion of marking columnsValue
        if (!CollectionUtils.isEmpty(result)) {
            String schema = resolveSchema(commandConfig.getSchema());
            String insert = String.format(SET_TABLE_IDENTITY_ON, schema, tableName) + targetCommand.get(ConnectorConstant.OPERTION_INSERT)
                    + String.format(SET_TABLE_IDENTITY_OFF, schema, tableName);
            targetCommand.put(ConnectorConstant.OPERTION_INSERT, insert);

            String upset = String.format(SET_TABLE_IDENTITY_ON, schema, tableName) + targetCommand.get(ConnectorConstant.OPERTION_UPSERT)
                    + String.format(SET_TABLE_IDENTITY_OFF, schema, tableName);
            targetCommand.put(ConnectorConstant.OPERTION_UPSERT, upset);

        }
        return targetCommand;
    }

    /**
     * SQL Server Objective Combined schema；NotConfigureTime Defaultdbo（and {@link #getSchema(String, Connection)} (d) Unanimously.
     */
    private String resolveSchema(String schema) {
        return StringUtil.isNotBlank(schema) ? schema : "dbo";
    }

    @Override
    public String buildUpsertSql(DatabaseConnectorInstance connectorInstance, SqlBuilderConfig config) {
        Database database = config.getDatabase();

        List<String> fs = new ArrayList<>();
        List<String> sfs = new ArrayList<>();
        List<String> vs = new ArrayList<>();
        List<String> updateSets = new ArrayList<>();
        List<String> pkFieldNames = new ArrayList<>();

        config.getFields().forEach(f -> {
            String fieldName = database.buildWithQuotation(f.getName());
            fs.add(fieldName);
            sfs.add("s." + fieldName);
            // Deal with specialType
            if (!database.buildCustomValue(vs, f)) {
                vs.add("?");
            }
            if (f.isPk()) {
                pkFieldNames.add(fieldName);
            } else {
                updateSets.add(String.format("%s = s.%s", fieldName, fieldName));
            }
        });

        StringBuilder sql = new StringBuilder(database.generateUniqueCode());
        sql.append("MERGE ").append(config.getSchema());
        sql.append(database.buildWithQuotation(config.getTableName())).append(" AS t ");
        sql.append("USING (VALUES (").append(StringUtil.join(vs, StringUtil.COMMA)).append(")) AS s (");
        sql.append(StringUtil.join(fs, StringUtil.COMMA)).append(") ");
        sql.append("ON ");

        // BuildON Condition：t.pk = s.pk AND ...
        StringBuilder onCondition = new StringBuilder();
        for (int i = 0; i < pkFieldNames.size(); i++) {
            if (i > 0) {
                onCondition.append(" AND ");
            }
            String pkFieldName = pkFieldNames.get(i);
            onCondition.append("t.").append(pkFieldName).append(" = s.").append(pkFieldName);
        }
        sql.append(onCondition).append(" ");

        sql.append("WHEN MATCHED THEN UPDATE SET ");
        sql.append(StringUtil.join(updateSets, StringUtil.COMMA)).append(" ");
        sql.append("WHEN NOT MATCHED THEN INSERT (");
        sql.append(StringUtil.join(fs, StringUtil.COMMA)).append(") VALUES (");
        sql.append(StringUtil.join(sfs, StringUtil.COMMA)).append(");");

        return sql.toString();
    }

    @Override
    public Object getPosition(DatabaseConnectorInstance connectorInstance) {
        String sql = "SELECT * from cdc.lsn_time_mapping order by tran_begin_time desc";
        List<Map<String, Object>> result = connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(sql));
        if (!CollectionUtils.isEmpty(result)) {
            List<Object> list = new ArrayList<>();
            result.forEach(r -> {
                r.computeIfPresent("start_lsn", (k, lsn) -> new Lsn((byte[]) lsn).toString());
                r.computeIfPresent("tran_begin_lsn", (k, lsn) -> new Lsn((byte[]) lsn).toString());
                r.computeIfPresent("tran_id", (k, lsn) -> new Lsn((byte[]) lsn).toString());
                r.computeIfPresent("tran_begin_time", (k, tranBeginTime) -> DateFormatUtil.timestampToString((Timestamp) tranBeginTime));
                r.computeIfPresent("tran_end_time", (k, tranEndTime) -> DateFormatUtil.timestampToString((Timestamp) tranEndTime));
                list.add(r);
            });
            return list;
        }
        return result;
    }

    @Override
    protected String getSchema(String schema, Connection connection) {
        return StringUtil.isNotBlank(schema) ? schema : "dbo";
    }

    @Override
    public String buildJdbcUrl(DatabaseConfig config, String database) {
        // jdbc:sqlserver://127.0.0.1:1433;databaseName=test;encrypt=false;trustServerCertificate=true
        StringBuilder url = new StringBuilder();
        url.append("jdbc:sqlserver://").append(config.getHost()).append(":").append(config.getPort());
        if (StringUtil.isNotBlank(database)) {
            url.append(";databaseName=").append(database);
        }
        return url.toString();
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    @Override
    public boolean buildCustom(List<String> fs, Field field) {
        switch (field.getTypeName()) {
            /**
             * SRID	    Name	                    Purpose unit
             * 0	    Undefined/local coordinates YesSQL Server geometry Default Any Unit
             * 4326	    WGS84	                GPS、Global coordinates (latitude)
             * 3857	    Web Mercator	        Google Maps、OpenStreetMap	Rice
             * 4490	    CGCS2000	            China2000Coordinate system
             * 4547	    CGCS2000/Gauss-Kruger	China Project nomenclature
             * 26910	NAD83/UTM zone 10N	    North American Coordinate System
             * 32610	WGS84/UTM zone 10N	    GlobalUTMProjection
             */
            case "geometry":
            case "geography":
                // UseSTAsText() AccessWKT Format, and containSRID Info，Mostly in thePOINT (...) | 4326
                fs.add(String.format("CAST([%s].STAsText() AS NVARCHAR(MAX)) + ' | ' + CAST([%s].STSrid AS NVARCHAR(10)) AS [%s]", field.getName(), field.getName(), field.getName()));
                return true;
            case "hierarchyid":
                fs.add(String.format("CAST([%s].ToString() AS NVARCHAR(MAX)) AS [%s]", field.getName(), field.getName()));
                return true;
            default:
                break;
        }
        return super.buildCustom(fs, field);
    }

    @Override
    public boolean buildCustomValue(List<String> fs, Field field) {
        switch (field.getTypeName()) {
            case "geometry":
                // POINT (133.4 38.5) | 4326
                fs.add("geometry::STGeomFromText(NULLIF(NULLIF(?, ''), 'NULL'),?)");
                return true;
            case "geography":
                fs.add("geography::STGeomFromText(NULLIF(NULLIF(?, ''), 'NULL'),?)");
                return true;
            case "hierarchyid":
                fs.add("hierarchyid::Parse(NULLIF(NULLIF(?, ''), 'NULL'))");
                return true;
            default:
                break;
        }
        return super.buildCustomValue(fs, field);
    }

    /**
     * SQL Server Batch writing:IDENTITY TableminLiYes.ConclusionCommentSexual opinionsON/OFF；VisibleJDBC TypeAvoidgetParameterMetaData()。
     */
    @Override
    protected int[] batchUpdate(DatabaseTemplate databaseTemplate, String executeSql, List<Field> fields, List<Map> data) throws Exception {
        SimpleConnection connection = databaseTemplate.getSimpleConnection();
        try {
            connection.setAutoCommit(false);
            int[] result = databaseTemplate.batchUpdate(executeSql, batchRows(fields, data), batchArgTypes(fields));
            connection.commit();
            return result;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * BatchParameter JDBC Type，andThe placeholder is in the same order.
     */
    private int[] batchArgTypes(List<Field> fields) {
        List<Integer> types = new ArrayList<>();
        for (Field field : fields) {
            if (field == null) {
                continue;
            }
            switch (field.getTypeName()) {
                case "geometry":
                case "geography":
                    types.add(Types.VARCHAR);
                    types.add(Types.INTEGER);
                    break;
                case "hierarchyid":
                    types.add(Types.VARCHAR);
                    break;
                default:
                    types.add(field.getType() != 0 ? field.getType() : Types.VARCHAR);
            }
        }
        return types.stream().mapToInt(Integer::intValue).toArray();
    }

}