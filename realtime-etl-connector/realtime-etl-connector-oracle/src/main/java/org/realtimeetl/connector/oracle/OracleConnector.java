/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.oracle;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.oracle.cdc.OracleListener;
import org.realtimeetl.connector.oracle.schema.OracleSchemaResolver;
import org.realtimeetl.connector.oracle.validator.OracleConfigValidator;
import org.realtimeetl.sdk.config.DDLConfig;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.connector.database.DatabaseTemplate;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.constant.DatabaseConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.PageSql;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.connector.oracle.schema.OracleBlobParameter;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.BindParameter;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;
import org.springframework.util.Assert;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * OracleConnectorAchieved
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2022-05-12 21:14
 */
public class OracleConnector extends AbstractDatabaseConnector {
    private final String QUERY_SCHEMA = "SELECT USERNAME FROM ALL_USERS where USERNAME not in('ANONYMOUS','APEX_030200','APEX_PUBLIC_USER','APPQOSSYS','BI','CTXSYS','DBSNMP','DIP','EXFSYS','FLOWS_FILES','HR','IX','MDDATA','MDSYS','MGMT_VIEW','OE','OLAPSYS','ORACLE_OCM','ORDDATA','ORDPLUGINS','ORDSYS','OUTLN','OWBSYS','OWBSYS_AUDIT','PM','SCOTT','SH','SI_INFORMTN_SCHEMA','SPATIAL_CSW_ADMIN_USR','SPATIAL_WFS_ADMIN_USR','SYS','SYSMAN','SYSTEM','WMSYS','XDB','XS$NULL') ORDER BY USERNAME";

    private final OracleConfigValidator configValidator = new OracleConfigValidator();
    private final OracleSchemaResolver schemaResolver = new OracleSchemaResolver();

    @Override
    public String getConnectorType() {
        return "Oracle";
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
            return new OracleListener();
        }
        return null;
    }

    @Override
    public List<String> getSchemas(DatabaseConnectorInstance connectorInstance, String catalog) {
        return connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(QUERY_SCHEMA, String.class));
    }

    @Override
    public String buildSqlWithQuotation() {
        return "\"";
    }

    /**
     * Oracle Here.schema（User）Namespace for migration; priority{@code schemaName}，Compatible Only{@code databaseName} The old data.
     */
    @Override
    public String buildCreateDatabaseSql(String databaseName, String schemaName) {
        String user = schemaName.trim().toUpperCase(Locale.ROOT);
        String password = defaultSchemaPassword(user);
        String createUserSql = "CREATE USER " + user + " IDENTIFIED BY \"" + password + "\" " +
                "DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP QUOTA UNLIMITED ON USERS";
        String grantSql = "GRANT CONNECT, RESOURCE TO " + user;
        return "DECLARE v_cnt NUMBER := 0; BEGIN " +
                "SELECT COUNT(1) INTO v_cnt FROM ALL_USERS WHERE USERNAME = '" + user + "'; " +
                "IF v_cnt = 0 THEN " +
                "EXECUTE IMMEDIATE '" + createUserSql + "'; " +
                "EXECUTE IMMEDIATE '" + grantSql + "'; " +
                "END IF; END;";
    }

    /**
     * Decisionschema（User）YesNoExist IMPLEMENTATION; priority{@code schemaName}，Compatible Only{@code databaseName} The old data.
     */
    @Override
    public boolean databaseExists(DatabaseConnectorInstance connectorInstance, String databaseName, String schemaName) {
        Integer count = connectorInstance.execute(databaseTemplate ->
                databaseTemplate.queryForObject(
                        "SELECT COUNT(1) FROM ALL_USERS WHERE USERNAME = UPPER(?)",
                        Integer.class,
                        schemaName
                ));
        return count != null && count > 0;
    }

    private static String defaultSchemaPassword(String schemaName) {
        return schemaName + "@1234";
    }

    @Override
    public String buildDropTableSql(DatabaseConnectorInstance targetInstance, String tableName) {
        String owner = targetInstance != null ? targetInstance.getSchema() : null;
        String dropSql = "DROP TABLE " + qualifyTableName(owner, tableName) + " CASCADE CONSTRAINTS";
        return wrapDropTableIfExists(owner, tableName, dropSql);
    }

    @Override
    public String getTargetTableDDL(DatabaseConnectorInstance targetInstance, String tableName, String sourceDDL) {
        String owner = targetInstance != null ? targetInstance.getSchema() : null;
        String createSql = "CREATE TABLE " + qualifyTableName(owner, tableName) + " (" + sourceDDL + ")";
        return wrapCreateTableIfNotExists(owner, tableName, createSql);
    }

    @Override
    public String getSourceTableDDL(DatabaseConnectorInstance sourceInstance, String sourceTableName) {
        if (sourceInstance == null || StringUtil.isBlank(sourceTableName)) {
            return StringUtil.EMPTY;
        }
        return fetchRawTableDdl(sourceInstance, sourceTableName);
    }

    private String fetchRawTableDdl(DatabaseConnectorInstance connectorInstance, String tableName) {
        return connectorInstance.execute(databaseTemplate -> {
            String schema = connectorInstance.getSchema();
            try {
                String ddl = databaseTemplate.queryForObject(
                        "SELECT DBMS_METADATA.GET_DDL('TABLE', ?, ?) FROM DUAL",
                        (ResultSet rs, int rowNum) -> {
                            Clob clob = rs.getClob(1);
                            if (clob == null || clob.length() == 0) {
                                return StringUtil.EMPTY;
                            }
                            return clob.getSubString(1, (int) clob.length());
                        },
                        tableName, schema);
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
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("ORA-31603")) {
                    return StringUtil.EMPTY;
                }
                throw new RuntimeException("Failed to get DDL for table: " + tableName, e);
            }
        });
    }

    private String wrapCreateTableIfNotExists(String owner, String tableName, String createSql) {
        String tableLiteral = escapeForExecuteImmediate(tableName);
        String escapedCreateSql = escapeForExecuteImmediate(createSql);
        String tableExistsPredicate = "UPPER(TABLE_NAME) = UPPER('" + tableLiteral + "')";
        if (StringUtil.isBlank(owner)) {
            return "DECLARE v_cnt NUMBER := 0; BEGIN " +
                    "SELECT COUNT(1) INTO v_cnt FROM USER_TABLES WHERE " + tableExistsPredicate + "; " +
                    "IF v_cnt = 0 THEN EXECUTE IMMEDIATE '" + escapedCreateSql + "'; END IF; END;";
        }
        String ownerUpper = normalizeOwner(owner);
        return "DECLARE v_cnt NUMBER := 0; BEGIN " +
                "SELECT COUNT(1) INTO v_cnt FROM ALL_TABLES WHERE OWNER = '" + ownerUpper + "' AND " + tableExistsPredicate + "; " +
                "IF v_cnt = 0 THEN EXECUTE IMMEDIATE '" + escapedCreateSql + "'; END IF; END;";
    }

    private String wrapDropTableIfExists(String owner, String tableName, String dropSql) {
        String tableLiteral = escapeForExecuteImmediate(tableName);
        String escapedDropSql = escapeForExecuteImmediate(dropSql);
        String tableExistsPredicate = "UPPER(TABLE_NAME) = UPPER('" + tableLiteral + "')";
        if (StringUtil.isBlank(owner)) {
            return "DECLARE v_cnt NUMBER := 0; BEGIN " +
                    "SELECT COUNT(1) INTO v_cnt FROM USER_TABLES WHERE " + tableExistsPredicate + "; " +
                    "IF v_cnt > 0 THEN EXECUTE IMMEDIATE '" + escapedDropSql + "'; END IF; END;";
        }
        String ownerUpper = normalizeOwner(owner);
        return "DECLARE v_cnt NUMBER := 0; BEGIN " +
                "SELECT COUNT(1) INTO v_cnt FROM ALL_TABLES WHERE OWNER = '" + ownerUpper + "' AND " + tableExistsPredicate + "; " +
                "IF v_cnt > 0 THEN EXECUTE IMMEDIATE '" + escapedDropSql + "'; END IF; END;";
    }

    /**
     * schema.table Double quotes and retentionTable nameCase,andDatabase migrationFormDDL Unanimously.
     */
    private String qualifyTableName(String owner, String tableName) {
        String quotedTable = buildWithQuotation(escapeForExecuteImmediate(tableName));
        if (StringUtil.isBlank(owner)) {
            return quotedTable;
        }
        return buildWithQuotation(normalizeOwner(owner)) + "." + quotedTable;
    }

    private static String normalizeOwner(String owner) {
        return owner == null ? StringUtil.EMPTY : owner.trim().toUpperCase(Locale.ROOT);
    }

//    private static String trimIdentifier(String name) {
//        return name == null ? StringUtil.EMPTY : name.trim();
//    }
//
//    private static String escapeSqlLiteral(String value) {
//        return escapeForExecuteImmediate(value);
//    }

    private void applySessionSchema(DatabaseTemplate databaseTemplate, String schema) {
        if (StringUtil.isBlank(schema)) {
            return;
        }
        databaseTemplate.execute("ALTER SESSION SET CURRENT_SCHEMA = " + normalizeOwner(schema));
    }

    @Override
    public Result writerDDL(DatabaseConnectorInstance connectorInstance, DDLConfig config) {
        Result result = new Result();
        try {
            Assert.hasText(config.getSql(), "Executing SQL cannot be empty.");
            connectorInstance.execute(databaseTemplate -> {
                applySessionSchema(databaseTemplate, connectorInstance.getSchema());
                databaseTemplate.execute(DatabaseConstant.DBS_UNIQUE_CODE.concat(config.getSql()));
                return true;
            });
            Map<String, String> successMap = new HashMap<>();
            successMap.put(ConfigConstant.BINLOG_DATA, config.getSql());
            result.addSuccessData(Collections.singletonList(successMap));
        } catch (Exception e) {
            result.getError().append(String.format("Execute ddl: %s, anomaly: %s", config.getSql(), e.getMessage()));
        }
        return result;
    }

    @Override
    public String getQueryCountSql(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        String queryFilter = config.getQueryFilter();
        // PARALLEL hint Use multi-nucleusCPU Parallel processing, enhanced performance without compromising accuracy
        // Jean.Oracle Optimizer automatically selects the optimal access path (IndexScan or full table scan)
        String query = "SELECT /*+ PARALLEL(t, 8) */ COUNT(*) FROM %s%s t %s";

        return String.format(query, config.getSchema(), database.buildWithQuotation(config.getTableName()), queryFilter);
    }

    @Override
    public String getPageSql(PageSql config) {
        // Use a three-layer nested query:SELECT * FROM (SELECT A.*, ROWNUM RN FROM (...) A WHERE ROWNUM <= ?) WHERE RN > ?
        StringBuilder sql = new StringBuilder();
        sql.append(DatabaseConstant.ORACLE_PAGE_SQL_START);
        sql.append(config.getQuerySql());
        // Use of the base category methodAddORDER BY（PressPrimary keySort, promiseminPage)
        appendOrderByPrimaryKeys(sql, config);
        sql.append(DatabaseConstant.ORACLE_PAGE_SQL_END);
        return sql.toString();
    }

    @Override
    public Object[] getPageArgs(ReaderContext context) {
        int pageSize = context.getPageSize();
        int pageIndex = context.getPageIndex();
        return new Object[]{pageIndex * pageSize, (pageIndex - 1) * pageSize};
    }

    @Override
    public String getPageCursorSql(PageSql config) {
        // Cursor query not supported
        if (!PrimaryKeyUtil.isSupportedCursor(config.getFields())) {
            return StringUtil.EMPTY;
        }

        StringBuilder sql = new StringBuilder();
        sql.append(DatabaseConstant.ORACLE_PAGE_CURSOR_SQL_START);
        sql.append(config.getQuerySql());
        // Use of base category publicTotalMethod BuildWHEREConditionandORDER BY
        buildCursorConditionAndOrderBy(sql, config);
        sql.append(DatabaseConstant.ORACLE_PAGE_CURSOR_SQL_END);
        return sql.toString();
    }

    @Override
    public Object[] getPageCursorArgs(ReaderContext context) {
        int pageSize = context.getPageSize();
        Object[] cursors = context.getCursors();
        if (null == cursors || cursors.length == 0) {
            return new Object[]{pageSize};
        }
        // Use of base category publicTotalMethod to Build CursorConditionParameter
        Object[] cursorArgs = buildCursorArgs(cursors);
        if (cursorArgs == null) {
            return new Object[]{pageSize};
        }

        // OracleNo need.OFFSET，Just...AddpageSizeParameter
        Object[] newCursors = new Object[cursorArgs.length + 1];
        System.arraycopy(cursorArgs, 0, newCursors, 0, cursorArgs.length);
        newCursors[cursorArgs.length] = pageSize;
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

        String qualifiedTable = qualifyTable(targetInstance, task, targetTableName);
        List<String> alterStatements = new ArrayList<>(loopSize);
        for (int i = 0; i < loopSize; i++) {
            Field sourceField = sourceDefinitions.get(i);
            String targetColumn = targetColumnNames.get(i);
            if (sourceField == null || StringUtil.isBlank(targetColumn)) {
                continue;
            }
            String col = buildWithQuotation(targetColumn);
            String type = formatPhysicalType(sourceField);
            if (isUnsupportedOnlineModifyType(type)) {
                continue;
            }
            // Single ColumnMODIFY：Write multiple columns in oneMODIFY(...) Easy to triggerORA-22859；writerDDL Once Programmingexecute，For this reasonPL/SQL BatchEXECUTE IMMEDIATE
            alterStatements.add(String.format(Locale.ROOT, "ALTER TABLE %s MODIFY (%s %s)", qualifiedTable, col, type));
        }
        if (alterStatements.isEmpty()) {
            return StringUtil.EMPTY;
        }
        if (alterStatements.size() == 1) {
            return alterStatements.get(0);
        }
        StringBuilder plsql = new StringBuilder(alterStatements.size() * 80);
        plsql.append("BEGIN\n");
        for (String stmt : alterStatements) {
            plsql.append("  EXECUTE IMMEDIATE '")
                    .append(escapeForExecuteImmediate(stmt))
                    .append("';\n");
        }
        plsql.append("END;");
        return plsql.toString();
    }

    /**
     * To DynamicSQL EmbeddedEXECUTE IMMEDIATE , the single quotation marks shall be doubled.
     */
    private static String escapeForExecuteImmediate(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replace("'", "''");
    }

    /**
     * Oracle LONG/LONG RAW Present.LOB Yes.ConvertI can't pass.MODIFY Update
     */
    private boolean isUnsupportedOnlineModifyType(String type) {
        if (StringUtil.isBlank(type)) {
            return false;
        }
        String normalized = type.trim().toUpperCase(Locale.ROOT);
        return "CLOB".equals(normalized)
                || "NCLOB".equals(normalized)
                || "BLOB".equals(normalized)
                || "LONG".equals(normalized)
                || "LONG RAW".equals(normalized);
    }

    private String qualifyTable(DatabaseConnectorInstance targetInstance, ValidateSyncTask task,
                                String tableName) {
        String schema = StringUtil.isNotBlank(task.getTargetSchema())
                ? task.getTargetSchema()
                : targetInstance.getCatalog();
        if (StringUtil.isBlank(schema)) {
            return buildWithQuotation(tableName);
        }
        return buildWithQuotation(schema) + "." + buildWithQuotation(tableName);
    }

    @Override
    public String getValidationQuery() {
        return "select 1 from dual";
    }

    @Override
    protected String getCatalog(String database, Connection connection) {
        return null;
    }

    @Override
    protected String getSchema(String schema, Connection connection) throws SQLException {
        if (StringUtil.isBlank(schema)) {
            schema = connection.getSchema();
        }
        if (StringUtil.isNotBlank(schema)) {
            schema = schema.toUpperCase();
        }
        return schema;
    }

    @Override
    public String buildJdbcUrl(DatabaseConfig config, String database) {
        // jdbc:oracle:thin:@127.0.0.1:1521:ORCL
        StringBuilder url = new StringBuilder();
        url.append("jdbc:oracle:thin:@").append(config.getHost()).append(":").append(config.getPort());
        String serviceName = config.getServiceName();
        if (StringUtil.isNotBlank(serviceName)) {
            // UseService Name
            url.append("/").append(serviceName);
        } else if (StringUtil.isNotBlank(database)) {
            // UseSID (Traditional approaches)
            url.append(":").append(database);
        } else {
            throw new IllegalArgumentException("Oracle needs to specify serviceName or database");
        }

        return url.toString();
    }

    @Override
    public String buildInsertSql(SqlBuilderConfig config) {
        // Oracle UseMERGE AchievedINSERT IGNORE EffectsPrimary keyIgnored during conflict)
        MergeContext context = buildMergeContext(config);

        StringBuilder sql = new StringBuilder(config.getDatabase().generateUniqueCode());
        // BuildMERGE Head
        buildMergeHeader(sql, config, context);

        // 64.WHEN NOT MATCHED The sentence,Primary keyNothing in conflict.INSERT IGNORE behaviour)
        buildInsertClause(sql, context);

        return sql.toString();
    }

    @Override
    public String buildUpsertSql(DatabaseConnectorInstance connectorInstance, SqlBuilderConfig config) {
        MergeContext context = buildMergeContext(config);

        StringBuilder sql = new StringBuilder(config.getDatabase().generateUniqueCode());
        // BuildMERGE Head
        buildMergeHeader(sql, config, context);

        // WHEN MATCHED Sub sentences - Update NotPrimary keyField
        sql.append("WHEN MATCHED THEN UPDATE SET ");
        sql.append(StringUtil.join(context.updateSets, StringUtil.COMMA)).append(" ");

        // WHEN NOT MATCHED Sub sentences - Insert
        buildInsertClause(sql, context);

        return sql.toString();
    }

    /**
     * BuildMERGE ContextField、Primary keyWait.Info）
     */
    private MergeContext buildMergeContext(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        MergeContext context = new MergeContext();

        config.getFields().forEach(f -> {
            String fieldName = database.buildWithQuotation(f.getName());
            context.fieldNames.add(fieldName);

            // BuildSELECT DepartmentminYes.FieldAlias
            List<String> fieldVs = new ArrayList<>();
            if (database.buildCustomValue(fieldVs, f)) {
                context.selectFields.add(fieldVs.get(0) + " AS " + fieldName);
            } else {
                context.selectFields.add("? AS " + fieldName);
            }

            if (f.isPk()) {
                context.pkFieldNames.add(fieldName);
            } else {
                context.updateSets.add(String.format("t.%s = s.%s", fieldName, fieldName));
            }
        });

        return context;
    }

    /**
     * BuildMERGE Efforts:MERGE INTO ... USING ... ON ...）
     */
    private void buildMergeHeader(StringBuilder sql, SqlBuilderConfig config, MergeContext context) {
        Database database = config.getDatabase();

        sql.append("MERGE INTO ").append(config.getSchema());
        sql.append(database.buildWithQuotation(config.getTableName())).append(" t ");

        // Oracle UseDUAL Table constructionSource
        sql.append("USING (SELECT ");
        sql.append(StringUtil.join(context.selectFields, StringUtil.COMMA));
        sql.append(" FROM DUAL) s ");

        // BuildON Condition：t.pk = s.pk AND ...
        sql.append("ON (");
        for (int i = 0; i < context.pkFieldNames.size(); i++) {
            if (i > 0) {
                sql.append(" AND ");
            }
            String pkFieldName = context.pkFieldNames.get(i);
            sql.append("t.").append(pkFieldName).append(" = s.").append(pkFieldName);
        }
        sql.append(") ");
    }

    /**
     * BuildINSERT Sub-statementWHEN NOT MATCHED THEN INSERT ...）
     */
    private void buildInsertClause(StringBuilder sql, MergeContext context) {
        sql.append("WHEN NOT MATCHED THEN INSERT (");
        sql.append(StringUtil.join(context.fieldNames, StringUtil.COMMA)).append(") VALUES (");

        // VALUES Subword Uses.fieldName
        List<String> sFieldNames = new ArrayList<>();
        context.fieldNames.forEach(f -> sFieldNames.add("s." + f));
        sql.append(StringUtil.join(sFieldNames, StringUtil.COMMA)).append(")");
    }

    @Override
    public String formatPhysicalType(Field sourceDefinition) {
        String typeName = sourceDefinition == null ? null : sourceDefinition.getTypeName();
        if (StringUtil.isBlank(typeName)) {
            return "VARCHAR2(255)";
        }
        String t = typeName.trim().toUpperCase(Locale.ROOT);
        String oracleSpecific = mapOracleSpecialNumericAndTemporal(t, sourceDefinition);
        if (oracleSpecific != null) {
            return oracleSpecific;
        }
        return super.formatPhysicalType(sourceDefinition);
    }

    /**
     * MODIFY SubstatementBINARY_FLOAT/DOUBLE、TIMESTAMP、RAW andBase class default is not compatible.
     */
    private static String mapOracleSpecialNumericAndTemporal(String t, Field sourceDefinition) {
        if ("BINARY_FLOAT".equals(t) || t.startsWith("BINARY_FLOAT(")) {
            return "BINARY_FLOAT";
        }
        if ("BINARY_DOUBLE".equals(t) || t.startsWith("BINARY_DOUBLE(")) {
            return "BINARY_DOUBLE";
        }
        return null;
    }

    /**
     * Binary column visible binding:BLOB Come on.ConnectCreatedLOB，AvoidMERGE DownsetObject(byte[]) Report17004。
     */
    @Override
    protected Object wrapBindParameter(Field field, Object val) {
        if (val instanceof OracleBlobParameter) {
            return val;
        }
        String type = field == null || StringUtil.isBlank(field.getTypeName())
                ? StringUtil.EMPTY
                : field.getTypeName().trim().toUpperCase(Locale.ROOT);
        // SQL NULL → setNull(Types.BLOB)，AvoidTYPE_UNKNOWN Tie-in.ErrorOr it's over.BLOB
        if (val == null && isBlobType(type)) {
            return new OracleBlobParameter(null);
        }
        if (!(val instanceof byte[])) {
            return val;
        }
        final byte[] bytes = (byte[]) val;
        if (isBlobType(type)) {
            return new OracleBlobParameter(bytes);
        }
        // RAW / LONG RAW
        return (BindParameter) (ps, paramIndex, connection) -> ps.setBytes(paramIndex, bytes);
    }

    private static boolean isBlobType(String type) {
        return StringUtil.isNotBlank(type) && type.contains("BLOB");
    }

    /**
     * MERGE ContextWow.
     */
    private static class MergeContext {

        List<String> fieldNames = new ArrayList<>();
        List<String> selectFields = new ArrayList<>();
        List<String> pkFieldNames = new ArrayList<>();
        List<String> updateSets = new ArrayList<>();
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }
}