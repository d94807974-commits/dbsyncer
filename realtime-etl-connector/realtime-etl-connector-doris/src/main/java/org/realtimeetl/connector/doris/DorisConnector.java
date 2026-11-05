/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.doris;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.doris.constant.DorisConstant;
import org.realtimeetl.connector.doris.load.DorisStreamLoadWriter;
import org.realtimeetl.connector.doris.schema.DorisSchemaResolver;
import org.realtimeetl.connector.doris.validator.DorisConfigValidator;
import org.realtimeetl.connector.mysql.MySQLConnector;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Apache Doris Connector（CompatibilityMySQL )
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-06 21:00
 */
public final class DorisConnector extends MySQLConnector {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Set<String> SYSTEM_DATABASES = Stream.of(
            "information_schema", "mysql", "__internal_schema")
            .collect(Collectors.toSet());

    /**
     * Match the end of the column definitionMySQL StylePRIMARY KEY The sentence,Doris The table needs to be stripped before construction.
     */
    private static final Pattern PRIMARY_KEY_CLAUSE = Pattern.compile(
            ",?\\s*PRIMARY\\s+KEY\\s*\\(([^)]+)\\)\\s*$", Pattern.CASE_INSENSITIVE);

    private final DorisConfigValidator configValidator = new DorisConfigValidator();
    private final DorisSchemaResolver schemaResolver = new DorisSchemaResolver();
    private final DorisStreamLoadWriter streamLoadWriter = new DorisStreamLoadWriter();

    @Override
    public String getConnectorType() {
        return "Doris";
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
        DorisConstant.enrichJdbcProperties(config);
        return super.connect(config, context);
    }

    @Override
    public Result writer(DatabaseConnectorInstance connectorInstance, PluginContext context) {
        DatabaseConfig config = connectorInstance.getConfig();
        if (DorisConstant.isStreamLoadMode(config) && DorisConstant.isStreamLoadEvent(context.getEvent())) {
            return streamLoadWriter.write(connectorInstance, context);
        }
        return super.writer(connectorInstance, context);
    }

    /**
     * Doris Not supportedMySQL Yes.ON DUPLICATE KEY UPDATE；Unique/Primary Key Peoples under ModelINSERT Press keyOverwrite。
     */
    @Override
    public String buildUpsertSql(DatabaseConnectorInstance connectorInstance, SqlBuilderConfig config) {
        return buildPlainInsertSql(config);
    }

    /**
     * Doris Not supportedINSERT IGNORE，Use undertakes normalINSERT。
     */
    @Override
    public String buildInsertSql(SqlBuilderConfig config) {
        return buildPlainInsertSql(config);
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isTiming(listenerType)) {
            return new DatabaseQuartzListener();
        }
        return null;
    }

    @Override
    public StorageService getStorageService() {
        return null;
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

    /**
     * ColumnsTypeKeep going.JDBC {@code getColumns}；KeysInfoUse{@code SHOW FULL COLUMNS}
     *（Avoidinformation_schema OriginalParameterYes.Doris Trigger UpMEM_ALLOC_FAILED）。
     */
    @Override
    public List<MetaInfo> getMetaInfo(DatabaseConnectorInstance connectorInstance, ConnectorServiceContext context) {
        if (CollectionUtils.isEmpty(context.getTablePatterns())) {
            return Collections.emptyList();
        }
        for (Table table : context.getTablePatterns()) {
            if (TableTypeEnum.getTableType(table.getType()) == getExtendedTableType()) {
                return super.getMetaInfo(connectorInstance, context);
            }
        }
        List<MetaInfo> metaInfos = super.getMetaInfo(connectorInstance, context);
        if (CollectionUtils.isEmpty(metaInfos)) {
            return metaInfos;
        }
        String database = StringUtil.getIfBlank(context.getCatalog(), context.getSchema());
        if (StringUtil.isBlank(database)) {
            return metaInfos;
        }
        return connectorInstance.execute(databaseTemplate -> {
            for (MetaInfo metaInfo : metaInfos) {
                if (metaInfo == null || StringUtil.isBlank(metaInfo.getTable())
                        || CollectionUtils.isEmpty(metaInfo.getColumn())) {
                    continue;
                }
                try {
                    String sql = "SHOW FULL COLUMNS FROM " + buildWithQuotation(database) + "."
                            + buildWithQuotation(metaInfo.getTable());
                    applyDorisKeyFlags(databaseTemplate.queryForList(sql), metaInfo);
                } catch (Exception e) {
                    // Key TagsFailedDo not block metadata loading, downstreamNonePrimary keyIt Thirties.
                    logger.warn("Failed to get the Doris Keybar, database={, table=}, err={",
                            database, metaInfo.getTable(), e.getMessage());
                }
            }
            return metaInfos;
        });
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

    /**
     * Doris Table must be specifiedKEY ModelandminA barrel. CrossLibraryMove Default Use Detailed ModelDUPLICATE KEY）：
     * Priority SourcePRIMARY KEY Columnsschema This post is part of our special coverage Global Voices 2011.No.
     */
    @Override
    public String getTargetTableDDL(DatabaseConnectorInstance targetInstance, String tableName, String sourceDDL) {
        if (StringUtil.isBlank(sourceDDL)) {
            return StringUtil.EMPTY;
        }
        String trimmed = sourceDDL.trim();
        List<String> keyColumns = extractPrimaryKeyColumns(trimmed);
        String columns = stripPrimaryKeyClause(trimmed);
        if (CollectionUtils.isEmpty(keyColumns)) {
            String firstColumn = extractFirstColumnName(columns);
            if (StringUtil.isBlank(firstColumn)) {
                return StringUtil.EMPTY;
            }
            keyColumns = Collections.singletonList(firstColumn);
        } else {
            columns = reorderColumnsForKeyPrefix(columns, keyColumns);
        }
        String qualifiedTable = qualifyTableName(targetInstance, tableName);
        String keyList = keyColumns.stream().map(this::buildWithQuotation).collect(Collectors.joining(StringUtil.COMMA));
        String hashCol = buildWithQuotation(keyColumns.get(0));
        return String.format(Locale.ROOT,
                "CREATE TABLE IF NOT EXISTS %s (%s) DUPLICATE KEY(%s) DISTRIBUTED BY HASH(%s) BUCKETS 10 "
                        + "PROPERTIES (\"replication_num\" = \"1\")",
                qualifiedTable, columns, keyList, hashCol);
    }

    private static void applyDorisKeyFlags(List<Map<String, Object>> keyRows, MetaInfo metaInfo) {
        if (CollectionUtils.isEmpty(keyRows)) {
            return;
        }
        List<String> keyColumns = new ArrayList<>();
        for (Map<String, Object> row : keyRows) {
            // SHOW FULL COLUMNS: Field / Key；Compatibilityinformation_schema Listing
            String columnName = getMapString(row, "Field", "FIELD", "field", "COLUMN_NAME", "column_name");
            if (StringUtil.isBlank(columnName)) {
                continue;
            }
            String columnKey = getMapString(row, "Key", "KEY", "key", "COLUMN_KEY", "column_key");
            if (isDorisKeyColumn(columnKey)) {
                keyColumns.add(columnName);
            }
        }
        if (CollectionUtils.isEmpty(keyColumns)) {
            return;
        }
        Set<String> keySet = keyColumns.stream()
                .map(name -> name.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(HashSet::new));
        for (Field field : metaInfo.getColumn()) {
            if (field == null || StringUtil.isBlank(field.getName())) {
                continue;
            }
            field.setPk(keySet.contains(field.getName().toUpperCase(Locale.ROOT)));
        }
    }

    /**
     * Doris {@code SHOW FULL COLUMNS} Yes.Key Always.YES/NO；DepartmentminVersion/information_schema YesPRI/UNI/AGG/DUP。
     */
    private static boolean isDorisKeyColumn(String columnKey) {
        if (StringUtil.isBlank(columnKey)) {
            return false;
        }
        String key = columnKey.trim().toUpperCase(Locale.ROOT);
        return "YES".equals(key) || "TRUE".equals(key) || "1".equals(key)
                || "PRI".equals(key) || "UNI".equals(key) || "AGG".equals(key) || "DUP".equals(key);
    }

    private static String getMapString(Map<String, Object> row, String... keys) {
        if (row == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object value = row.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return null;
    }

    private static String stripPrimaryKeyClause(String sourceDDL) {
        Matcher pkMatcher = PRIMARY_KEY_CLAUSE.matcher(sourceDDL);
        if (!pkMatcher.find()) {
            return sourceDDL;
        }
        String columns = pkMatcher.replaceFirst(StringUtil.EMPTY).trim();
        if (columns.endsWith(StringUtil.COMMA)) {
            columns = columns.substring(0, columns.length() - 1).trim();
        }
        return columns;
    }

    private static List<String> extractPrimaryKeyColumns(String sourceDDL) {
        Matcher pkMatcher = PRIMARY_KEY_CLAUSE.matcher(sourceDDL);
        if (!pkMatcher.find()) {
            return Collections.emptyList();
        }
        String inside = pkMatcher.group(1);
        if (StringUtil.isBlank(inside)) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>();
        for (String part : inside.split(StringUtil.COMMA)) {
            String name = unwrapIdentifier(part.trim());
            if (StringUtil.isNotBlank(name)) {
                keys.add(name);
            }
        }
        return keys;
    }

    /**
     * Doris RequestKEY Listedschema Orderd prefixes; CATPrimary keyColumn definition moved to the top of the column list.
     */
    private static String reorderColumnsForKeyPrefix(String columns, List<String> keyColumns) {
        List<String> defs = splitColumnDefinitions(columns);
        if (CollectionUtils.isEmpty(defs) || CollectionUtils.isEmpty(keyColumns)) {
            return columns;
        }
        Set<String> keySet = keyColumns.stream()
                .map(name -> name.toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<String> keyDefs = new ArrayList<>();
        List<String> otherDefs = new ArrayList<>();
        Set<String> foundKeys = new HashSet<>();
        for (String def : defs) {
            String colName = extractColumnNameFromDef(def);
            if (StringUtil.isNotBlank(colName) && keySet.contains(colName.toUpperCase(Locale.ROOT))) {
                keyDefs.add(def);
                foundKeys.add(colName.toUpperCase(Locale.ROOT));
            } else {
                otherDefs.add(def);
            }
        }
        if (foundKeys.size() != keySet.size()) {
            return columns;
        }
        List<String> ordered = new ArrayList<>(keyDefs.size() + otherDefs.size());
        // PressPRIMARY KEY Declares the sequence discharge column
        for (String key : keyColumns) {
            String upper = key.toUpperCase(Locale.ROOT);
            for (String def : keyDefs) {
                String colName = extractColumnNameFromDef(def);
                if (StringUtil.isNotBlank(colName) && upper.equals(colName.toUpperCase(Locale.ROOT))) {
                    ordered.add(def);
                    break;
                }
            }
        }
        ordered.addAll(otherDefs);
        return StringUtil.join(ordered, StringUtil.COMMA);
    }

    private static List<String> splitColumnDefinitions(String columns) {
        List<String> result = new ArrayList<>();
        if (StringUtil.isBlank(columns)) {
            return result;
        }
        StringBuilder current = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < columns.length(); i++) {
            char c = columns.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth = Math.max(0, depth - 1);
            } else if (c == ',' && depth == 0) {
                String part = current.toString().trim();
                if (StringUtil.isNotBlank(part)) {
                    result.add(part);
                }
                current.setLength(0);
                continue;
            }
            current.append(c);
        }
        String last = current.toString().trim();
        if (StringUtil.isNotBlank(last)) {
            result.add(last);
        }
        return result;
    }

    private static String extractColumnNameFromDef(String columnDef) {
        if (StringUtil.isBlank(columnDef)) {
            return StringUtil.EMPTY;
        }
        String[] tokens = columnDef.trim().split("\\s+");
        return unwrapIdentifier(tokens[0]);
    }

    @Override
    public String getSourceTableDDL(DatabaseConnectorInstance sourceInstance, String sourceTableName) {
        // Doris CompatibilityMySQL An agreement.SHOW CREATE TABLE BackFormatand MySQL Consistent
        return super.getSourceTableDDL(sourceInstance, sourceTableName);
    }

    @Override
    public String buildDropTableSql(DatabaseConnectorInstance targetInstance, String tableName) {
        if (StringUtil.isBlank(tableName)) {
            return StringUtil.EMPTY;
        }
        return "DROP TABLE IF EXISTS " + qualifyTableName(targetInstance, tableName);
    }

    private String qualifyTableName(DatabaseConnectorInstance targetInstance, String tableName) {
        String qualifiedTable = buildWithQuotation(tableName);
        if (targetInstance == null) {
            return qualifiedTable;
        }
        String database = StringUtil.isNotBlank(targetInstance.getCatalog())
                ? targetInstance.getCatalog()
                : targetInstance.getSchema();
        if (StringUtil.isNotBlank(database)) {
            return buildWithQuotation(database) + "." + qualifiedTable;
        }
        return qualifiedTable;
    }

    private static String extractFirstColumnName(String columns) {
        List<String> defs = splitColumnDefinitions(columns);
        if (CollectionUtils.isEmpty(defs)) {
            return StringUtil.EMPTY;
        }
        return extractColumnNameFromDef(defs.get(0));
    }

    private static String unwrapIdentifier(String identifier) {
        if (StringUtil.isBlank(identifier)) {
            return StringUtil.EMPTY;
        }
        String name = identifier.trim();
        if ((name.startsWith("`") && name.endsWith("`"))
                || (name.startsWith("\"") && name.endsWith("\""))
                || (name.startsWith("[") && name.endsWith("]"))) {
            return name.substring(1, name.length() - 1);
        }
        return name;
    }

    private String buildPlainInsertSql(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        List<Field> fields = config.getFields();
        List<String> fieldNames = new ArrayList<>(fields.size());
        List<String> placeholders = new ArrayList<>(fields.size());
        for (Field field : fields) {
            fieldNames.add(database.buildWithQuotation(field.getName()));
            placeholders.add("?");
        }
        StringBuilder table = new StringBuilder();
        table.append(config.getSchema());
        table.append(database.buildWithQuotation(config.getTableName()));
        return String.format("%sINSERT INTO %s (%s) VALUES (%s)",
                database.generateUniqueCode(),
                table,
                StringUtil.join(fieldNames, StringUtil.COMMA),
                StringUtil.join(placeholders, StringUtil.COMMA));
    }
}
