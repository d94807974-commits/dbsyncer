/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.mysql;

import net.sf.jsqlparser.statement.alter.Alter;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.mysql.cdc.MySQLListener;
import org.realtimeetl.connector.mysql.constant.MySQLConstant;
import org.realtimeetl.connector.mysql.schema.MySQLSchemaResolver;
import org.realtimeetl.connector.mysql.storage.MySQLStorageService;
import org.realtimeetl.connector.mysql.validator.MySQLConfigValidator;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.constant.DatabaseConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.PageSql;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.storage.StorageService;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MySQLConnectorAchieved
 *
 * @author AE86
 * @version 1.0.0
 * @date 2021-11-22 23:55
 */
public class MySQLConnector extends AbstractDatabaseConnector {

    private final MySQLConfigValidator configValidator = new MySQLConfigValidator();
    private final MySQLSchemaResolver schemaResolver = new MySQLSchemaResolver();
    private final Set<String> SYSTEM_DATABASES = Stream.of("information_schema", "mysql", "performance_schema", "sys").collect(Collectors.toSet());

    @Override
    public String getConnectorType() {
        return "MySQL";
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public ConnectorInstance connect(DatabaseConfig config, ConnectorServiceContext context) {
        MySQLConstant.enrichJdbcProperties(config);
        return super.connect(config, context);
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isTiming(listenerType)) {
            return new DatabaseQuartzListener();
        }

        if (ListenerTypeEnum.isLog(listenerType)) {
            return new MySQLListener();
        }
        return null;
    }

    @Override
    public List<String> getDatabases(DatabaseConnectorInstance connectorInstance) {
        return connectorInstance.execute(databaseTemplate -> {
            List<String> databases = databaseTemplate.queryForList("SHOW DATABASES", String.class);
            if (!CollectionUtils.isEmpty(databases)) {
                return databases.stream().filter(name -> !SYSTEM_DATABASES.contains(name.toLowerCase())).collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
    }

    @Override
    public StorageService getStorageService() {
        return new MySQLStorageService();
    }

    @Override
    public String generateUniqueCode() {
        return DatabaseConstant.DBS_UNIQUE_CODE;
    }

    @Override
    public String buildSqlWithQuotation() {
        return "`";
    }

    @Override
    public String buildCreateDatabaseSql(String databaseName, String schemaName) {
        if (StringUtil.isBlank(databaseName)) {
            return StringUtil.EMPTY;
        }
        return "CREATE DATABASE IF NOT EXISTS " + buildWithQuotation(databaseName);
    }

    @Override
    public boolean databaseExists(DatabaseConnectorInstance connectorInstance, String databaseName, String schemaName) {
        if (StringUtil.isBlank(databaseName)) {
            return false;
        }
        return connectorInstance.execute(databaseTemplate ->
                !CollectionUtils.isEmpty(databaseTemplate.queryForList("SHOW DATABASES LIKE ?", String.class, databaseName)));
    }

    @Override
    public String getTargetTableDDL(DatabaseConnectorInstance targetInstance, String tableName, String sourceDDL) {
        return "CREATE TABLE IF NOT EXISTS " + tableName + " (" + sourceDDL + ")";
    }

    @Override
    public String getSourceTableDDL(DatabaseConnectorInstance sourceInstance, String sourceTableName) {
        if (sourceInstance == null || StringUtil.isBlank(sourceTableName)) {
            return StringUtil.EMPTY;
        }
        String sql = "SHOW CREATE TABLE " + buildWithQuotation(sourceTableName);
        return sourceInstance.execute(databaseTemplate -> {
            List<java.util.Map<String, Object>> rows = databaseTemplate.queryForList(sql);
            if (CollectionUtils.isEmpty(rows)) {
                return StringUtil.EMPTY;
            }
            java.util.Map<String, Object> ddlRow = rows.get(0);
            if (ddlRow == null || ddlRow.isEmpty()) {
                return StringUtil.EMPTY;
            }
            String ddl = StringUtil.EMPTY;
            for (java.util.Map.Entry<String, Object> entry : ddlRow.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase("create table")
                        && entry.getValue() != null) {
                    ddl = String.valueOf(entry.getValue());
                    break;
                }
            }
            if (StringUtil.isBlank(ddl)) {
                return StringUtil.EMPTY;
            }
            return ddl.replaceFirst("(?i)^CREATE\\s+TABLE\\s+", "CREATE TABLE IF NOT EXISTS ");
        });
    }

    @Override
    public String buildDropTableSql(DatabaseConnectorInstance targetInstance, String tableName) {
        if (StringUtil.isBlank(tableName)) {
            return StringUtil.EMPTY;
        }
        return "DROP TABLE IF EXISTS " + buildWithQuotation(tableName);
    }

    @Override
    public String getPageSql(PageSql config) {
        StringBuilder sql = new StringBuilder(config.getQuerySql());
        // Use of the base category methodAddORDER BY（PressPrimary keySort, promiseminPage)
        appendOrderByPrimaryKeys(sql, config);
        sql.append(DatabaseConstant.MYSQL_PAGE_SQL);
        return sql.toString();
    }

    @Override
    public Object[] getPageArgs(ReaderContext context) {
        int pageSize = context.getPageSize();
        int pageIndex = context.getPageIndex();
        return new Object[]{(pageIndex - 1) * pageSize, pageSize};
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
        sql.append(DatabaseConstant.MYSQL_PAGE_SQL);
        return sql.toString();
    }

    @Override
    public Object[] getPageCursorArgs(ReaderContext context) {
        int pageSize = context.getPageSize();
        Object[] cursors = context.getCursors();
        if (null == cursors || cursors.length == 0) {
            return new Object[]{0, pageSize};
        }
        // Use of base category publicTotalMethod to Build CursorConditionParameter
        Object[] cursorArgs = buildCursorArgs(cursors);
        if (cursorArgs == null) {
            return new Object[]{0, pageSize};
        }
        // MySQLYes.OFFSET=0andLIMIT=pageSizeParameter
        Object[] newCursors = new Object[cursorArgs.length + 2];
        System.arraycopy(cursorArgs, 0, newCursors, 0, cursorArgs.length);
        newCursors[cursorArgs.length] = 0; // OFFSET
        newCursors[cursorArgs.length + 1] = pageSize; // LIMIT
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
        //Collapse DataLibraryandTable name db.table
        String qualifiedTable = qualifyTable(targetInstance, task, targetTableName);
        List<String> clauses = new ArrayList<>(loopSize);
        for (int i = 0; i < loopSize; i++) {
            Field sourceField = sourceDefinitions.get(i);
            String targetColumn = targetColumnNames.get(i);
            // Illegal data skips directly
            if (sourceField == null || StringUtil.isBlank(targetColumn)) {
                continue;
            }
            String col = buildWithQuotation(targetColumn);
            String type = formatPhysicalType(sourceField);
            clauses.add(String.format(Locale.ROOT, "MODIFY COLUMN %s %s", col, type));
        }
        if (clauses.isEmpty()) {
            return StringUtil.EMPTY;
        }
        return String.format(Locale.ROOT, "ALTER TABLE %s %s", qualifiedTable, StringUtil.join(clauses, ", "));
    }

    private String qualifyTable(DatabaseConnectorInstance targetInstance, ValidateSyncTask task,
                                String tableName) {
        String dbName = StringUtil.isNotBlank(targetInstance.getCatalog())
                ? targetInstance.getCatalog()
                : task.getTargetDatabase();
        if (StringUtil.isBlank(dbName)) {
            return buildWithQuotation(tableName);
        }
        return buildWithQuotation(dbName) + "." + buildWithQuotation(tableName);
    }

    @Override
    public String buildUpsertSql(DatabaseConnectorInstance connectorInstance, SqlBuilderConfig config) {
        Database database = config.getDatabase();
        List<Field> fields = config.getFields();
        List<String> fs = new ArrayList<>();
        List<String> vs = new ArrayList<>();
        List<String> dfs = new ArrayList<>();
        fields.forEach(f -> {
            String name = database.buildWithQuotation(f.getName());
            fs.add(name);
            vs.add("?");
            if (!f.isPk()) {
                dfs.add(String.format("%s = VALUES(%s)", name, name));
            }
        });

        String uniqueCode = database.generateUniqueCode();
        StringBuilder table = buildTableName(config);
        String fieldNames = StringUtil.join(fs, StringUtil.COMMA);
        String values = StringUtil.join(vs, StringUtil.COMMA);
        String dupNames = StringUtil.join(dfs, StringUtil.COMMA);
        // Based onPrimary keyOr the only.IndexUpdate on Conflict
        return String.format("%sINSERT INTO %s (%s) VALUES (%s) ON DUPLICATE KEY UPDATE %s;", uniqueCode, table, fieldNames, values, dupNames);
    }

    @Override
    public String buildInsertSql(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        List<Field> fields = config.getFields();

        List<String> fs = new ArrayList<>();
        List<String> vs = new ArrayList<>();
        fields.forEach(f -> {
            fs.add(database.buildWithQuotation(f.getName()));
            vs.add("?");
        });

        String uniqueCode = database.generateUniqueCode();
        StringBuilder table = buildTableName(config);
        String fieldNames = StringUtil.join(fs, StringUtil.COMMA);
        String values = StringUtil.join(vs, StringUtil.COMMA);

        // Rule number of conflictsActions
        return String.format("%sINSERT IGNORE INTO %s (%s) VALUES (%s)", uniqueCode, table, fieldNames, values);
    }

    private StringBuilder buildTableName(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        StringBuilder table = new StringBuilder();
        table.append(config.getSchema());
        table.append(database.buildWithQuotation(config.getTableName()));
        return table;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    @Override
    protected String getSchema(String schema, Connection connection) {
        return null;
    }

    @Override
    public String buildJdbcUrl(DatabaseConfig config, String database) {
        // jdbc:mysql://127.0.0.1:3306/test?rewriteBatchedStatements=true&useUnicode=true
        StringBuilder url = new StringBuilder();
        url.append("jdbc:mysql://").append(config.getHost()).append(":").append(config.getPort());
        if (database != null && !database.trim().isEmpty()) {
            url.append("/").append(database);
        }
        return url.toString();
    }

    @Override
    public String buildAlterCatalog(DatabaseConnectorInstance connectorInstance, Alter alter) {
        // Target databaseFirst Name
        String catalog = connectorInstance.getCatalog();
        catalog = buildWithQuotation(catalog);
        // 1. GenerateBasisSQL
        String sql = alter.toString();

        // IfTarget database nameNot empty and currentSQL Not includedLibraryFirst Name
        if (catalog != null && !sql.contains(catalog + ".")) {
            String tableName = alter.getTable().getName();

            // It explains:
            // (?i) : Ignore case
            // (ALTER\s+TABLE\s+) : accepts group1，Match"ALTER TABLE " Space Projects
            // (?:`[^`]+`\.)? : Non-capture group, match optional"Old library name." (For example:`test`.)
            // (?:`)? : Match optional initial inverted signs
            // \\Q...\\E : Match PureTable name
            // (?:`)? : Match optional end invertes
            String regex = "(?i)(ALTER\\s+TABLE\\s+)(?:`[^`]+`\\.)?(?:`)?" + java.util.regex.Pattern.quote(tableName) + "(?:`)?";

            // Replaceis: catch group1 + NewLibraryFirst Name+ . + Table name
            String replacement = "$1" + catalog + "." + tableName;
            return sql.replaceFirst(regex, replacement);
        }
        return sql;
    }

    @Override
    public String formatPhysicalType(Field sourceDefinition) {
        if (sourceDefinition == null || StringUtil.isBlank(sourceDefinition.getTypeName())) {
            return super.formatPhysicalType(sourceDefinition);
        }
        String t = sourceDefinition.getTypeName().trim().toUpperCase(Locale.ROOT);
        // MODIFY COLUMN DownENUM/SET If adoptNoneNumber of wordsListis illegal.VARCHAR
        if ("ENUM".equals(t) || "SET".equals(t)) {
            int len = sourceDefinition.getColumnSize() > 0 ? sourceDefinition.getColumnSize() : 255;
            return String.format(Locale.ROOT, "VARCHAR(%d)", len);
        }
        return super.formatPhysicalType(sourceDefinition);
    }

}