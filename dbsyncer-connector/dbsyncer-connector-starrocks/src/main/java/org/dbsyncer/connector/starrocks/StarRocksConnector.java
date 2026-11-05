/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.starrocks;

import org.dbsyncer.common.model.Result;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.connector.mysql.MySQLConnector;
import org.dbsyncer.connector.starrocks.constant.StarRocksConstant;
import org.dbsyncer.connector.starrocks.load.StarRocksStreamLoadWriter;
import org.dbsyncer.connector.starrocks.schema.StarRocksSchemaResolver;
import org.dbsyncer.connector.starrocks.validator.StarRocksConfigValidator;
import org.dbsyncer.sdk.config.DatabaseConfig;
import org.dbsyncer.sdk.config.SqlBuilderConfig;
import org.dbsyncer.sdk.connector.ConfigValidator;
import org.dbsyncer.sdk.connector.ConnectorInstance;
import org.dbsyncer.sdk.connector.ConnectorServiceContext;
import org.dbsyncer.sdk.connector.database.Database;
import org.dbsyncer.sdk.connector.database.DatabaseConnectorInstance;
import org.dbsyncer.sdk.enums.ListenerTypeEnum;
import org.dbsyncer.sdk.enums.TableTypeEnum;
import org.dbsyncer.sdk.listener.DatabaseQuartzListener;
import org.dbsyncer.sdk.listener.Listener;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.model.MetaInfo;
import org.dbsyncer.sdk.model.Table;
import org.dbsyncer.sdk.plugin.PluginContext;
import org.dbsyncer.sdk.schema.SchemaResolver;
import org.dbsyncer.sdk.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * StarRocks Connector（CompatibilityMySQL )
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-07 03:00
 */
public final class StarRocksConnector extends MySQLConnector {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final Set<String> SYSTEM_DATABASES = Stream.of(
            "information_schema", "sys", "_statistics_")
            .collect(Collectors.toSet());

    private final StarRocksConfigValidator configValidator = new StarRocksConfigValidator();
    private final StarRocksSchemaResolver schemaResolver = new StarRocksSchemaResolver();
    private final StarRocksStreamLoadWriter streamLoadWriter = new StarRocksStreamLoadWriter();

    @Override
    public String getConnectorType() {
        return "StarRocks";
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
        StarRocksConstant.enrichJdbcProperties(config);
        return super.connect(config, context);
    }

    @Override
    public Result writer(DatabaseConnectorInstance connectorInstance, PluginContext context) {
        DatabaseConfig config = connectorInstance.getConfig();
        if (StarRocksConstant.isStreamLoadMode(config) && StarRocksConstant.isStreamLoadEvent(context.getEvent())) {
            return streamLoadWriter.write(connectorInstance, context);
        }
        return super.writer(connectorInstance, context);
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isTiming(listenerType)) {
            return new DatabaseQuartzListener();
        }
        return null;
    }

    /**
     * StarRocks Not supportedMySQL Yes.{@code INSERT IGNORE} Syntax:Primary keyModelPrimary Key / Unique Key）Table
     * Pass.{@code INSERT INTO} You opted.Primary keyAutomatically heavyOverwrite，So here it's pure.{@code INSERT INTO}。
     */
    @Override
    public String buildInsertSql(SqlBuilderConfig config) {
        return buildInsertIntoSql(config);
    }

    /**
     * StarRocks Not supportedMySQL Yes.{@code ON DUPLICATE KEY UPDATE} Syntax:Primary keyModel Table{@code INSERT INTO}
     * That's what it is.UPSERT（PressPrimary keyOverwrite），And so...upsert and insert Use same{@code INSERT INTO} statement.
     */
    @Override
    public String buildUpsertSql(DatabaseConnectorInstance connectorInstance, SqlBuilderConfig config) {
        return buildInsertIntoSql(config);
    }

    private String buildInsertIntoSql(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        List<Field> fields = config.getFields();
        List<String> fs = new ArrayList<>();
        List<String> vs = new ArrayList<>();
        fields.forEach(f -> {
            fs.add(database.buildWithQuotation(f.getName()));
            vs.add("?");
        });

        String uniqueCode = database.generateUniqueCode();
        StringBuilder table = new StringBuilder();
        table.append(config.getSchema());
        table.append(database.buildWithQuotation(config.getTableName()));
        String fieldNames = StringUtil.join(fs, StringUtil.COMMA);
        String values = StringUtil.join(vs, StringUtil.COMMA);
        return String.format("%sINSERT INTO %s (%s) VALUES (%s)", uniqueCode, table, fieldNames, values);
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
     * StarRocks Yes.JDBC {@code getPrimaryKeys} Yeah.key Model tables usuallyBackEmpty, resulting in column metadata missingPrimary keyMark,
     * Database migrationIt's time.「MissingPrimary key」Failed。HomeTypeStill FatherJDBC，Primary keyChange{@code DESC table} Yes.Key Columns
     *（key Model Sort Keytrue）Completion.
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
        return connectorInstance.execute(databaseTemplate -> {
            for (MetaInfo metaInfo : metaInfos) {
                if (metaInfo == null || StringUtil.isBlank(metaInfo.getTable()) || CollectionUtils.isEmpty(metaInfo.getColumn())) {
                    continue;
                }
                try {
                    String table = buildWithQuotation(metaInfo.getTable());
                    if (StringUtil.isNotBlank(database)) {
                        table = buildWithQuotation(database) + "." + table;
                    }
                    // queryForList BackCase insensitiveLinkedCaseInsensitiveMap，Directly pressableField/Key RemoveValue
                    Set<String> keys = new HashSet<>();
                    for (Map<String, Object> row : databaseTemplate.queryForList("DESC " + table)) {
                        if (isKeyColumn(row.get("Key"))) {
                            keys.add(String.valueOf(row.get("Field")).toUpperCase(Locale.ROOT));
                        }
                    }
                    metaInfo.getColumn().forEach(f -> f.setPk(keys.contains(f.getName().toUpperCase(Locale.ROOT))));
                } catch (Exception e) {
                    // Key TagsFailedDo not block metadata loading, downstreamNonePrimary keyIt Thirties.
                    logger.warn("Failed to get StarRocks Table =}, table =}, err ={", database, metaInfo.getTable(), e.getMessage());
                }
            }
            return metaInfos;
        });
    }

    /**
     * StarRocks {@code DESC} Yes.Key Listedkey Model Sort Keytrue；CompatibilityPRI/UNI/DUP（DepartmentminVersion/Detailed Model Sorting Key.
     */
    private static boolean isKeyColumn(Object columnKey) {
        if (columnKey == null) {
            return false;
        }
        String key = columnKey.toString().trim();
        return "true".equalsIgnoreCase(key) || "PRI".equalsIgnoreCase(key)
                || "UNI".equalsIgnoreCase(key) || "DUP".equalsIgnoreCase(key);
    }
}
