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
package org.realtimeetl.sdk.connector.database;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.config.DDLConfig;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.AbstractConnector;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.connector.FullPluginContext;
import org.realtimeetl.sdk.connector.database.ds.SimpleConnection;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.constant.DatabaseConstant;
import org.realtimeetl.sdk.enums.FilterEnum;
import org.realtimeetl.sdk.enums.OperationEnum;
import org.realtimeetl.sdk.enums.QuartzFilterEnum;
import org.realtimeetl.sdk.enums.SqlBuilderEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.filter.AbstractFilter;
import org.realtimeetl.sdk.filter.BooleanFilter;
import org.realtimeetl.sdk.filter.impl.InFilter;
import org.realtimeetl.sdk.model.CursorBound;
import org.realtimeetl.sdk.model.CursorBoundRequest;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Filter;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.PageSql;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.plugin.MetaContext;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.CustomData;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.realtimeetl.sdk.util.DatabaseUtil;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.support.rowset.ResultSetWrappingSqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.util.Assert;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Relationship DataLibraryConnectorAchieved
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020-01-08 15:17
 */
public abstract class AbstractDatabaseConnector extends AbstractConnector implements ConnectorService<DatabaseConnectorInstance, DatabaseConfig>, Database {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public abstract String buildJdbcUrl(DatabaseConfig connectorConfig, String database);

    @Override
    public TableTypeEnum getExtendedTableType() {
        return TableTypeEnum.SQL;
    }

    @Override
    public Class<DatabaseConfig> getConfigClass() {
        return DatabaseConfig.class;
    }

    @Override
    public ConnectorInstance connect(DatabaseConfig config, ConnectorServiceContext context) {
        return new DatabaseConnectorInstance(config, context.getCatalog(), context.getSchema());
    }

    @Override
    public void disconnect(DatabaseConnectorInstance connectorInstance) {
        connectorInstance.close();
    }

    @Override
    public boolean isAlive(DatabaseConnectorInstance connectorInstance) {
        Integer count = connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForObject(getValidationQuery(), Integer.class));
        return null != count && count > 0;
    }

    @Override
    public List<Table> getTable(DatabaseConnectorInstance connectorInstance, ConnectorServiceContext context) {
        return connectorInstance.execute(databaseTemplate -> {
            SimpleConnection connection = databaseTemplate.getSimpleConnection();
            Connection conn = connection.getConnection();
            // Compatible processingschema andcatalog
            String effectiveCatalog = getCatalog(context.getCatalog(), conn);
            String effectiveSchema = getSchema(context.getSchema(), conn);

            String[] types = {TableTypeEnum.TABLE.getCode(), TableTypeEnum.VIEW.getCode(), TableTypeEnum.MATERIALIZED_VIEW.getCode()};
            final ResultSet rs = conn.getMetaData().getTables(effectiveCatalog, effectiveSchema, null, types);
            logger.info("Using connector type: {}, catalog: {}, schema: {}", getConnectorType(), effectiveCatalog, effectiveSchema);
            List<Table> tables = new ArrayList<>();
            while (rs.next()) {
                final String tableName = rs.getString("TABLE_NAME");
                final String tableType = rs.getString("TABLE_TYPE");
                Table table = new Table();
                table.setName(tableName);
                table.setType(tableType);
                tables.add(table);
            }
            return tables;
        });
    }

    @Override
    public List<MetaInfo> getMetaInfo(DatabaseConnectorInstance connectorInstance, ConnectorServiceContext context) {
        return connectorInstance.execute(databaseTemplate -> {
            SimpleConnection connection = databaseTemplate.getSimpleConnection();
            Connection conn = connection.getConnection();
            final String catalog = getCatalog(context.getCatalog(), conn);
            final String schema = getSchema(context.getSchema(), conn);
            DatabaseMetaData metaData = conn.getMetaData();
            List<MetaInfo> metaInfos = new ArrayList<>();
            for (Table table : context.getTablePatterns()) {
                // Not specifiedTypeQuite normal, avoidTableTypeEnum type "null"
                if (StringUtil.isBlank(table.getType())) {
                    table.setType(TableTypeEnum.TABLE.getCode());
                }
                // CustomSQL
                if (TableTypeEnum.getTableType(table.getType()) == getExtendedTableType()) {
                    getMetaInfoWithSQL(databaseTemplate, metaInfos, catalog, schema, table);
                    continue;
                }

                String tableName = table.getName();
                List<Field> fields = new ArrayList<>();
                List<String> primaryKeys = findTablePrimaryKeys(metaData, catalog, schema, tableName);
                try (ResultSet columnMetadata = metaData.getColumns(catalog, schema, tableName, null)) {
                    while (columnMetadata.next()) {
                        String columnName = columnMetadata.getString("COLUMN_NAME");
                        int columnType = columnMetadata.getInt("DATA_TYPE");
                        String typeName = columnMetadata.getString("TYPE_NAME");
                        int columnSize = Math.max(0, columnMetadata.getInt("COLUMN_SIZE"));
                        int ratio = Math.max(0, columnMetadata.getInt("DECIMAL_DIGITS"));
                        fields.add(new Field(columnName, typeName, columnType, primaryKeys.contains(columnName), columnSize, ratio));
                    }
                }
                MetaInfo metaInfo = new MetaInfo();
                metaInfo.setTable(tableName);
                metaInfo.setTableType(TableTypeEnum.TABLE.getCode());
                metaInfo.setColumn(fields);
                metaInfos.add(metaInfo);
            }
            return metaInfos;
        });
    }

    @Override
    public Result writerDDL(DatabaseConnectorInstance connectorInstance, DDLConfig config) {
        Result result = new Result();
        try {
            Assert.hasText(config.getSql(), "Executing SQL cannot be empty.");
            connectorInstance.execute(databaseTemplate -> {
                // ImplementationddlTime, Take it.dbsUnique identification code to prevent a two-way sync leading to a dead cycle
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

    private void getMetaInfoWithSQL(DatabaseTemplate databaseTemplate, List<MetaInfo> metaInfos, String catalog, String schema, Table t) throws SQLException {
        Object val = t.getExtInfo().get(ConnectorConstant.CUSTOM_TABLE_SQL);
        if (val == null) {
            return;
        }
        String originalSql = String.valueOf(val);
        String sql = originalSql.toUpperCase();
        sql = sql.replace("\t", " ");
        sql = sql.replace("\r", " ");
        sql = sql.replace("\n", " ");
        String metaSql = StringUtil.contains(sql, " WHERE ") ? originalSql + " AND 1!=1 " : originalSql + " WHERE 1!=1 ";
        String tableName = t.getName();

        SqlRowSet sqlRowSet = databaseTemplate.queryForRowSet(metaSql);
        ResultSetWrappingSqlRowSet rowSet = (ResultSetWrappingSqlRowSet) sqlRowSet;
        SqlRowSetMetaData metaData = rowSet.getMetaData();

        // 165. Query TableFieldInfo
        int columnCount = metaData.getColumnCount();
        if (1 > columnCount) {
            throw new SdkException("Query table fields cannot be empty.");
        }
        List<Field> fields = new ArrayList<>(columnCount);
        Map<String, List<String>> tables = new HashMap<>();
        try {
            SimpleConnection connection = databaseTemplate.getSimpleConnection();
            DatabaseMetaData md = connection.getMetaData();
            Connection conn = connection.getConnection();
            String name = null;
            String label = null;
            String typeName = null;
            String table = null;
            int columnType;
            boolean pk;
            for (int i = 1; i <= columnCount; i++) {
                table = StringUtil.isNotBlank(tableName) ? tableName : metaData.getTableName(i);
                if (null == tables.get(table)) {
                    tables.putIfAbsent(table, findTablePrimaryKeys(md, catalog, schema, table));
                }
                name = metaData.getColumnName(i);
                label = metaData.getColumnLabel(i);
                typeName = metaData.getColumnTypeName(i);
                columnType = metaData.getColumnType(i);
                pk = isPk(tables, table, name);
                fields.add(new Field(label, typeName, columnType, pk));
            }
        } finally {
            tables.clear();
        }
        MetaInfo metaInfo = new MetaInfo();
        metaInfo.setTable(tableName);
        metaInfo.setTableType(getExtendedTableType().getCode());
        metaInfo.setColumn(fields);
        metaInfos.add(metaInfo);
    }

    private boolean isPk(Map<String, List<String>> tables, String tableName, String name) {
        List<String> pk = tables.get(tableName);
        if (CollectionUtils.isEmpty(pk)) {
            return false;
        }
        return pk.stream().anyMatch(key -> key.equalsIgnoreCase(name));
    }

    @Override
    public long getCount(DatabaseConnectorInstance connectorInstance, MetaContext metaContext) {
        Map<String, String> command = metaContext.getCommand();
        if (CollectionUtils.isEmpty(command)) {
            return 0L;
        }
        // 2. Based onYesNoTarget ConnectionOrganisationSQL
        String queryCountSql = metaContext.isTargetConnector() ? command.get(ConnectorConstant.TARGET_QUERY_COUNT) : command.get(ConnectorConstant.OPERTION_QUERY_COUNT);

        // 3. SQLDirect as emptyBack
        if (StringUtil.isBlank(queryCountSql)) {
            return 0L;
        }

        // 2、BackResultSet
        try {
            return connectorInstance.execute(databaseTemplate -> {
                Long count = databaseTemplate.queryForObject(queryCountSql, Long.class);
                return count == null ? 0 : count;
            });
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }

    @Override
    public Result reader(DatabaseConnectorInstance connectorInstance, ReaderContext context) {
        String querySql;
        String queryKey = context.getCommandKey();
        if (StringUtil.isBlank(queryKey)) {
            boolean supportedCursor = context.isSupportedCursor() && context.getCursors() != null && context.getCursors().length > 0;
            if (context.isTargetConnector()) {
                queryKey = supportedCursor ? ConnectorConstant.OPERTION_QUERY_TARGET_CURSOR : ConnectorConstant.OPERTION_QUERY_TARGET;
            } else {
                queryKey = supportedCursor ? ConnectorConstant.OPERTION_QUERY_CURSOR : ConnectorConstant.OPERTION_QUERY;
            }
            querySql = context.getCommand().get(queryKey);
            Assert.hasText(querySql, "Query statement cannot be empty.");
            Collections.addAll(context.getArgs(), supportedCursor ? getPageCursorArgs(context) : getPageArgs(context));
        } else {
            FullPluginContext full = (FullPluginContext) context;
            String condition = buildQueryCondition(full.getFilter(), context.getArgs());
            querySql = context.getCommand().get(queryKey);
            Assert.hasText(querySql, "Query statement cannot be empty.");
            querySql = buildTargetReaderSql(querySql, condition);
        }
        final String finalQuerySql = querySql;
        // 3、ImplementationSQL
        List<Map<String, Object>> list = connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(finalQuerySql, context.getArgs().toArray()));
        // 4、BackResultSet
        return new Result(list);
    }

    @Override
    public CursorBound resolveCursorBound(DatabaseConnectorInstance connectorInstance, CursorBoundRequest request) {
        if (request == null || connectorInstance == null || request.getBudget() <= 0 || request.getSourceTable() == null) {
            return CursorBound.unsupported(request == null ? StringUtil.EMPTY : request.getStartCursor());
        }
        String startCursor = StringUtil.getIfBlank(request.getStartCursor(), StringUtil.EMPTY);
        Table table = request.getSourceTable();
        if (StringUtil.isBlank(table.getName()) || CollectionUtils.isEmpty(table.getColumn())) {
            return CursorBound.unsupported(startCursor);
        }
        if (!PrimaryKeyUtil.isSupportedCursor(table.getColumn())) {
            return CursorBound.unsupported(startCursor);
        }

        List<String> primaryKeys = resolveCursorPrimaryKeys(request.getCommand(), table);
        if (CollectionUtils.isEmpty(primaryKeys)) {
            return CursorBound.unsupported(startCursor);
        }
        List<Field> pkFields = resolvePrimaryKeyFields(table.getColumn(), primaryKeys);
        if (CollectionUtils.isEmpty(pkFields) || !PrimaryKeyUtil.isSupportedCursor(pkFields)) {
            return CursorBound.unsupported(startCursor);
        }

        String queryFilter = extractQueryFilter(request.getCommand());
        String schema = buildSchemaWithQuotation(connectorInstance.getSchema());
        SqlBuilderConfig buildSqlConfig = new SqlBuilderConfig(this, schema, table.getName(), primaryKeys, pkFields, queryFilter);
        String baseQuerySql = SqlBuilderEnum.QUERY.getSqlBuilder().buildQuerySql(buildSqlConfig);
        PageSql pageSql = new PageSql(baseQuerySql, queryFilter, primaryKeys, pkFields);

        Object[] cursors = PrimaryKeyUtil.getLastCursors(startCursor);
        boolean fromHead = cursors == null || cursors.length == 0;
        int budget = request.getBudget();

        String sql;
        if (fromHead) {
            sql = getPageSql(pageSql);
        } else {
            sql = getPageCursorSql(pageSql);
            if (StringUtil.isBlank(sql)) {
                return CursorBound.unsupported(startCursor);
            }
        }
        if (StringUtil.isBlank(sql)) {
            return CursorBound.unsupported(startCursor);
        }
        //That's it.budget SoldierOFFSET=budget-1, LIMIT=1），Hit.O(1) Memory
        Object[] probeArgs = buildBoundProbeArgs(fromHead, cursors, budget);
        if (probeArgs != null) {
            List<Map<String, Object>> probeRows = queryBoundRows(connectorInstance, sql, probeArgs);
            if (!CollectionUtils.isEmpty(probeRows)) {
                String endCursor = toEndCursor(probeRows, primaryKeys);
                return CursorBound.of(startCursor, endCursor, false, budget);
            }
            // Uncut: remaining rows< budget，Back Four Windows (usually far smaller thanbudget）
        }

        return resolveCursorBoundByFullWindow(connectorInstance, sql, fromHead, cursors, budget, startCursor, primaryKeys);
    }

    /**
     * 划界探点参数：取排序后第 {@code budget} 行（OFFSET=budget-1, LIMIT=1）。
     * <p>General in the cursor window.keyset Parameterand {@link #getPageArgs} minPagepatches; if cursorSQL minPagePosition.and
     * {@link #getPageArgs} Inconsistencies (e.g., department)min Oracle Cursor OnlyROWNUM），Back null It means the window is changed.
     *
     * @param fromHead YesNoFrom FrontNone START）
     * @param cursors  START Positioning keys after resolution;fromHead Time availablenull
     * @param budget   Block Size
     * @return Explore.Parameter；NoneIt's a French security stakeout.Back null
     */
    protected Object[] buildBoundProbeArgs(boolean fromHead, Object[] cursors, int budget) {
        int safeBudget = Math.max(budget, 1);
        FullPluginContext pageContext = newBoundPageContext(1, safeBudget, fromHead ? null : cursors);
        if (fromHead) {
            return getPageArgs(pageContext);
        }
        Object[] cursorPart = buildCursorArgs(cursors);
        if (cursorPart == null) {
            cursorPart = new Object[0];
        }
        FullPluginContext sizeOne = newBoundPageContext(1, 1, cursors);
        Object[] cursorArgsWithPage = getPageCursorArgs(sizeOne);
        if (cursorArgsWithPage == null) {
            return null;
        }
        int pageArgSlots = cursorArgsWithPage.length - cursorPart.length;
        if (pageArgSlots < 0) {
            return null;
        }
        Object[] pagePart = getPageArgs(pageContext);
        if (pagePart == null || pagePart.length != pageArgSlots) {
            // CursorminPagePosition.andNormalminPageIt's not consistent.NoneFrench security rewritingOFFSET
            return null;
        }
        Object[] merged = new Object[cursorPart.length + pagePart.length];
        System.arraycopy(cursorPart, 0, merged, 0, cursorPart.length);
        System.arraycopy(pagePart, 0, merged, cursorPart.length, pagePart.length);
        return merged;
    }

    /**
     * Full window demarcation:LIMIT=budget，Take the end Advise SOLASEND（Last pageorNoneUse in detection).
     */
    private CursorBound resolveCursorBoundByFullWindow(DatabaseConnectorInstance connectorInstance, String sql,
                                                       boolean fromHead, Object[] cursors, int budget,
                                                       String startCursor, List<String> primaryKeys) {
        FullPluginContext pageContext = newBoundPageContext(budget, 1, fromHead ? null : cursors);
        Object[] args = fromHead ? getPageArgs(pageContext) : getPageCursorArgs(pageContext);
        if (args == null) {
            args = new Object[0];
        }
        List<Map<String, Object>> rows = queryBoundRows(connectorInstance, sql, args);
        int actualCount = CollectionUtils.isEmpty(rows) ? 0 : rows.size();
        if (actualCount == 0) {
            return CursorBound.of(startCursor, StringUtil.EMPTY, true, 0);
        }
        String endCursor = toEndCursor(rows, primaryKeys);
        boolean lastPage = actualCount < budget;
        return CursorBound.of(startCursor, endCursor, lastPage, actualCount);
    }

    private FullPluginContext newBoundPageContext(int pageSize, int pageIndex, Object[] cursors) {
        FullPluginContext pageContext = new FullPluginContext();
        pageContext.setPageIndex(pageIndex);
        pageContext.setPageSize(pageSize);
        pageContext.setCursors(cursors);
        pageContext.setArgs(new ArrayList<>());
        return pageContext;
    }

    private List<Map<String, Object>> queryBoundRows(DatabaseConnectorInstance connectorInstance,
                                                     String sql, Object[] args) {
        final String finalSql = sql;
        final Object[] finalArgs = args == null ? new Object[0] : args;
        return connectorInstance.execute(
                databaseTemplate -> databaseTemplate.queryForList(finalSql, finalArgs));
    }

    private String toEndCursor(List<Map<String, Object>> rows, List<String> primaryKeys) {
        Object[] lastCursors = PrimaryKeyUtil.getLastCursors((List) rows, primaryKeys);
        if (lastCursors == null || lastCursors.length == 0) {
            return StringUtil.EMPTY;
        }
        return StringUtil.join(lastCursors, StringUtil.COMMA);
    }

    /**
     * Batch writingTargetData Fact
     */
    @Override
    public Result writer(DatabaseConnectorInstance connectorInstance, PluginContext context) {
        String event = context.getEvent();
        List<Map> data = context.getTargetList();
        List<Field> targetFields = context.getTargetFields();

        if (CollectionUtils.isEmpty(targetFields)) {
            logger.error("writer fields can not be empty.");
            throw new SdkException("writer fields can not be empty.");
        }
        if (CollectionUtils.isEmpty(data)) {
            logger.error("writer data can not be empty.");
            throw new SdkException("writer data can not be empty.");
        }
        // 1、AccessSQL
        List<Field> fields = new ArrayList<>(targetFields);
        String executeSql;
        if (isDelete(event)) {
            executeSql = context.getCommand().get(event);
            fields.clear();
            fields.addAll(PrimaryKeyUtil.findExistPrimaryKeyFields(targetFields));
        } else if (context.isForceUpdate()) {
            // OnOverwrite
            executeSql = context.getCommand().get(ConnectorConstant.OPERTION_UPSERT);
        } else if (isUpdate(event)) {
            // EditActions
            fields.addAll(PrimaryKeyUtil.findPrimaryKeyFields(fields));
            executeSql = context.getCommand().get(event);
        } else {
            // CreateActions
            executeSql = context.getCommand().get(event);
        }
        if (StringUtil.isBlank(executeSql)) {
            logger.error("Event: }, executing SQL cannot be empty", event);
            throw new SdkException("Implementing SQL cannot be empty");
        }

        Result result = new Result();
        int[] execute = null;
        try {
            // 2、SettingsParameter
            execute = connectorInstance.execute(databaseTemplate -> batchUpdate(databaseTemplate, executeSql, fields, data));
        } catch (Exception e) {
            // Come on.Failed, the service is downgraded to article by article
            forceUpdate(connectorInstance, context, executeSql, fields, event, data, result);
        }

        if (null != execute) {
            int batchSize = execute.length;
            for (int i = 0; i < batchSize; i++) {
                /**
                 * MySQLBackResult：
                 * With ON DUPLICATE KEY UPDATE, the affected-rows value per row is 1 if the row is inserted as a new row, 2 if an existing row is updated, and 0 if an existing row is set to its current values.
                 */
                if (execute[i] == 1 || execute[i] == 2 || execute[i] == -2
                        || (context.isForceUpdate() && execute[i] == 0)) {
                    result.getSuccessData().add(data.get(i));
                    continue;
                }
                if (isInsert(event)) {
                    result.getFailData().add(data.get(i));
                    if (StringUtil.isBlank(result.getError())) {
                        result.getError().append("Possible 8.6 Sheet Data");
                    }
                }

            }
        }
        return result;
    }

    /**
     * YesNoUseJDBC Service packing batch written.ClickHouse The engine that doesn't support the service shouldBack false。
     */
    protected boolean useJdbcTransaction() {
        return true;
    }

    protected int[] batchUpdate(DatabaseTemplate databaseTemplate, String executeSql, List<Field> fields, List<Map> data) throws Exception {
        if (!useJdbcTransaction()) {
            return databaseTemplate.batchUpdate(executeSql, batchRows(fields, data));
        }
        SimpleConnection connection = databaseTemplate.getSimpleConnection();
        try {
            connection.setAutoCommit(false);
            int[] result = databaseTemplate.batchUpdate(executeSql, batchRows(fields, data));
            connection.commit();
            return result;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void forceUpdate(DatabaseConnectorInstance connectorInstance, PluginContext context, String executeSql, List<Field> fields, String event, List<Map> data, Result result) {
        data.forEach(row -> {
            try {
                int execute = connectorInstance.execute(databaseTemplate -> databaseTemplate.update(executeSql, batchRow(fields, row)));
                if (execute == 0) {
                    throw new SdkException("Data non-existent or performance abnormal");
                }
                result.getSuccessData().add(row);
                printTraceLog(context, event, row, Boolean.TRUE, null);
            } catch (Exception e) {
                result.getFailData().add(row);
                result.getError().append(context.getTraceId()).append(" SQL:").append(executeSql).append(System.lineSeparator()).append("ERROR:").append(e.getMessage()).append(System.lineSeparator());
                printTraceLog(context, event, row, Boolean.FALSE, e.getMessage());
            }
        });
    }

    @Override
    public Map<String, String> getSourceCommand(CommandConfig commandConfig) {
        Table table = commandConfig.getTable();
        if (StringUtil.isBlank(table.getType())) {
            table.setType(TableTypeEnum.TABLE.getCode());
        }
        TableTypeEnum tableType = TableTypeEnum.getTableType(table.getType());
        if (tableType == getExtendedTableType()) {
            return getSourceCommandWithSQL(commandConfig);
        }

        List<String> primaryKeys = PrimaryKeyUtil.findTablePrimaryKeys(table);
        if (CollectionUtils.isEmpty(primaryKeys)) {
            return new HashMap<>();
        }

        String tableName = table.getName();
        if (StringUtil.isBlank(tableName)) {
            logger.error("Data source table cannot be empty.");
            throw new SdkException("Data source table cannot be empty.");
        }

        // Structure Name
        String schema = buildSchemaWithQuotation(resolveSchemaOrCatalog(commandConfig));
        // SyncField
        List<Field> columns = filterColumn(table.getColumn());
        // Fetch FilterSQL
        final String queryFilterSql = getQueryFilterSql(commandConfig);

        // AccessminPageSQL
        Map<String, String> map = new HashMap<>();
        SqlBuilderConfig buildSqlConfig = new SqlBuilderConfig(this, schema, tableName, primaryKeys, columns, queryFilterSql);
        map.put(ConnectorConstant.OPERTION_QUERY_SOURCE_IN, SqlBuilderEnum.QUERY.getSqlBuilder().buildQuerySql(buildSqlConfig));
        // Business filtering in the original language: demarcation reconstructionSQL Read it from time to time.minPage QUERY Invert
        map.put(ConnectorConstant.OPERTION_QUERY_FILTER, queryFilterSql);
        buildSql(map, SqlBuilderEnum.QUERY, buildSqlConfig);

        // Build CursorminPageSQL
        buildSql(map, SqlBuilderEnum.QUERY_CURSOR, buildSqlConfig);
        // Actual references to recordsandThe cursor.Primary keyList，ForFull syncRetrieving cursor placeholders
        map.put(ConnectorConstant.CURSOR_PK_NAMES, StringUtil.join(primaryKeys, StringUtil.COMMA));
        // Get queriesTotalSQL
        map.put(SqlBuilderEnum.QUERY_COUNT.getName(), getQueryCountSql(buildSqlConfig));
        return map;
    }

    private Map<String, String> getSourceCommandWithSQL(CommandConfig commandConfig) {
        // Fetch FilterSQL
        String queryFilterSql = getQueryFilterSql(commandConfig);
        Table table = commandConfig.getTable();
        Map<String, String> map = new HashMap<>();
        List<String> primaryKeys = PrimaryKeyUtil.findTablePrimaryKeys(table);
        if (CollectionUtils.isEmpty(primaryKeys)) {
            return map;
        }

        // Get queriesSQL
        String querySql = String.valueOf(table.getExtInfo().get(ConnectorConstant.CUSTOM_TABLE_SQL));

        // ExistenceCondition
        if (StringUtil.isNotBlank(queryFilterSql)) {
            querySql += queryFilterSql;
        }
        map.put(ConnectorConstant.OPERTION_QUERY_SOURCE_IN, querySql);
        map.put(ConnectorConstant.OPERTION_QUERY_FILTER, queryFilterSql);
        PageSql pageSql = new PageSql(querySql, StringUtil.EMPTY, primaryKeys, table.getColumn());
        map.put(SqlBuilderEnum.QUERY.getName(), getPageSql(pageSql));
        // Get queriesTotalSQL
        map.put(SqlBuilderEnum.QUERY_COUNT.getName(), "SELECT COUNT(*) FROM (" + querySql + ") DBS_T");
        return map;
    }

    @Override
    public Map<String, String> getTargetCommand(CommandConfig commandConfig) {
        Table table = commandConfig.getTable();
        List<String> primaryKeys = PrimaryKeyUtil.findTablePrimaryKeys(table);
        if (CollectionUtils.isEmpty(primaryKeys)) {
            return new HashMap<>();
        }

        String tableName = table.getName();
        if (StringUtil.isBlank(tableName)) {
            logger.error("Target source table cannot be empty.");
            throw new SdkException("Target source table cannot be empty.");
        }

        // Structure Name
        String schema = buildSchemaWithQuotation(resolveSchemaOrCatalog(commandConfig));
        // SyncField
        List<Field> column = filterColumn(table.getColumn());
        SqlBuilderConfig config = new SqlBuilderConfig(this, schema, tableName, primaryKeys, column, "");

        // Get Add & DeleteSQL
        Map<String, String> map = new HashMap<>();
        map.put(SqlBuilderEnum.INSERT.getName(), buildInsertSql(config));
        if (commandConfig.isForceUpdate()) {
            DatabaseConnectorInstance instance = (DatabaseConnectorInstance) commandConfig.getConnectorInstance();
            map.put(ConnectorConstant.OPERTION_UPSERT, buildUpsertSql(instance, config));
        } else {
            buildSql(map, SqlBuilderEnum.UPDATE, config);
        }
        buildSql(map, SqlBuilderEnum.DELETE, config);

        map.put(ConnectorConstant.OPERTION_QUERY_TARGET_IN, SqlBuilderEnum.QUERY.getSqlBuilder().buildQuerySql(config));
        buildSql(map, SqlBuilderEnum.QUERY_TARGET, config);
        buildSql(map, SqlBuilderEnum.QUERY_TARGET_CURSOR, config);

        //QuestionObjective CombinedTotalSQL
        final String queryFilterSql = getQueryFilterSql(commandConfig);
        SqlBuilderConfig buildSqlConfig = new SqlBuilderConfig(this, schema, tableName, primaryKeys, column, queryFilterSql);
        map.put(SqlBuilderEnum.TARGET_QUERY_COUNT.getName(), getQueryTargetCountSql(buildSqlConfig));
        return map;
    }

    /**
     * AccessDatabase name
     *
     * @param database
     * @param connection
     * @return
     */
    protected String getCatalog(String database, Connection connection) throws SQLException {
        return StringUtil.isNotBlank(database) ? database : connection.getCatalog();
    }

    /**
     * Get Structure Name
     *
     * @param schema
     * @param connection
     * @return
     */
    protected String getSchema(String schema, Connection connection) throws SQLException {
        return StringUtil.isNotBlank(schema) ? schema : connection.getSchema();
    }

    /**
     * Retrieving frame name prefixes (included dots), emptyBackEmpty string.
     */
    protected String buildSchemaWithQuotation(String schema) {
        StringBuilder s = new StringBuilder();
        if (StringUtil.isNotBlank(schema)) {
            s.append(buildWithQuotation(schema)).append(".");
        }
        return s.toString();
    }

    /**
     * SpellTable namePrefix: Priorityschema（Oracle/PG）；schema Back in time.catalog（MySQL LibraryName.
     */
    protected String resolveSchemaOrCatalog(CommandConfig commandConfig) {
        if (commandConfig == null) {
            return StringUtil.EMPTY;
        }
        if (StringUtil.isNotBlank(commandConfig.getSchema())) {
            return commandConfig.getSchema();
        }
        return StringUtil.getIfBlank(commandConfig.getCatalog(), StringUtil.EMPTY);
    }

    /**
     * Parsing the use of cursor delimitationPrimary keyName (priority)command MediumCURSOR_PK_NAMES）。
     */
    private List<String> resolveCursorPrimaryKeys(Map<String, String> command, Table table) {
        if (command != null) {
            String cursorPkNames = command.get(ConnectorConstant.CURSOR_PK_NAMES);
            if (StringUtil.isNotBlank(cursorPkNames)) {
                return Arrays.stream(StringUtil.split(cursorPkNames, StringUtil.COMMA))
                        .map(String::trim)
                        .filter(StringUtil::isNotBlank)
                        .collect(Collectors.toList());
            }
        }
        List<Field> pkFields = PrimaryKeyUtil.findPrimaryKeyFields(table.getColumn());
        if (CollectionUtils.isEmpty(pkFields)) {
            return Collections.emptyList();
        }
        return pkFields.stream().map(Field::getName).collect(Collectors.toList());
    }

    /**
     * PressPrimary keyName orderFieldDefinitions.
     */
    private List<Field> resolvePrimaryKeyFields(List<Field> columns, List<String> primaryKeys) {
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : columns) {
            if (field != null && StringUtil.isNotBlank(field.getName())) {
                fieldMap.putIfAbsent(field.getName(), field);
            }
        }
        List<Field> pkFields = new ArrayList<>(primaryKeys.size());
        for (String pk : primaryKeys) {
            Field field = fieldMap.get(pk);
            if (field == null) {
                for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                    if (StringUtil.equalsIgnoreCase(entry.getKey(), pk)) {
                        field = entry.getValue();
                        break;
                    }
                }
            }
            if (field == null) {
                return Collections.emptyList();
            }
            pkFields.add(field);
        }
        return pkFields;
    }

    /**
     * FromSavedYes.QUERY SQL InterceptUserFilter Conditions（HamWHERE，does not containORDER BY/LIMIT）。
     * <p>OnlyQUERY，No need.QUERY_CURSOR（The latter may contain a cursor placeholder).
     */
    private String extractQueryFilter(Map<String, String> command) {
        if (CollectionUtils.isEmpty(command)) {
            return StringUtil.EMPTY;
        }
        String filter = command.get(ConnectorConstant.OPERTION_QUERY_FILTER);
        return filter == null ? StringUtil.EMPTY : filter;
    }

    /**
     * Cursor for Build Composite KeysminPageCondition（Equity SupportedPrimary key）
     *
     * <p>For SinglesFieldPrimary key (pk1)：GenerateCondition：pk1 > ?</p>
     * <p>Yeah.2FieldPrimary key (pk1, pk2)：GenerateCondition：(pk1 > ?) OR (pk1 = ? AND pk2 > ?)</p>
     * <p>Yeah.3FieldPrimary key (pk1, pk2, pk3)：GenerateCondition：(pk1 > ?) OR (pk1 = ? AND pk2 > ?) OR (pk1 = ? AND pk2 = ? AND pk3 > ?)</p>
     * <p>Yeah.nFieldPrimary key：And so on....</p>
     *
     * <p>原理：对于复合键 (pk1, pk2, ..., pkn)，要查询所有大于 (v1, v2, ..., vn) 的记录，
     * Need to meet:(pk1 > v1) OR (pk1 = v1 AND pk2 > v2) OR ... OR (pk1 = v1 AND pk2 = v2 AND ... AND pkn > vn)</p>
     *
     * @param sql         SQLBuilder
     * @param primaryKeys Primary keyList（Support for AnyLength）
     * @param noCondition NoneFilter Conditions
     */
    private void buildCursorCondition(StringBuilder sql, List<String> primaryKeys, boolean noCondition) {
        if (primaryKeys.isEmpty()) {
            return;
        }

        // SingleFieldPrimary key：Direct Usepk > ?
        if (primaryKeys.size() == 1) {
            if (!noCondition) {
                sql.append(" AND ");
            }
            sql.append(primaryKeys.get(0)).append(" > ? ");
            return;
        }

        // Finance(pk1 > ?) OR (pk1 = ? AND pk2 > ?) OR ...
        if (!noCondition) {
            sql.append(" AND ");
        }
        sql.append("(");

        for (int i = 0; i < primaryKeys.size(); i++) {
            if (i > 0) {
                sql.append(" OR ");
            }
            sql.append("(");

            // Front.FieldBothYesEqual
            for (int j = 0; j < i; j++) {
                if (j > 0) {
                    sql.append(" AND ");
                }
                sql.append(primaryKeys.get(j)).append(" = ? ");
            }

            // Last one.FieldUse greater than
            if (i > 0) {
                sql.append(" AND ");
            }
            sql.append(primaryKeys.get(i)).append(" > ? ");
            sql.append(")");
        }

        sql.append(") ");
    }

    /**
     * Build CursorminPageYes.WHEREConditionandORDER BYSub sentences (do not include)LIMIT/ROWNUMWait.minPage(Restricted)
     *
     * <p>For SubcategorygetPageCursorSqlBuild cursor in MethodminPageSQLThe man.TotalDepartmentmin</p>
     *
     * @param sql    SQLBuilder
     * @param config PageSqlConfigure
     */
    protected void buildCursorConditionAndOrderBy(StringBuilder sql, PageSql config) {
        boolean noCondition = StringUtil.isBlank(config.getQueryFilter());
        // Auditor apologizedless.Filter Conditions
        if (noCondition) {
            sql.append(" WHERE ");
        }

        // UsebuildPrimaryKeysProcessingPrimary key
        List<String> primaryKeys = buildPrimaryKeys(config.getPrimaryKeys());

        // Cursor for Build Composite KeysminPageCondition: (pk1 > ?) OR (pk1 = ? AND pk2 > ?) OR ...
        buildCursorCondition(sql, primaryKeys, noCondition);

        // Add ORDER BY - You have to.Primary keySort to ensure cursorminPagewith
        sql.append(" ORDER BY ").append(StringUtil.join(primaryKeys, StringUtil.COMMA));
    }

    /**
     * 仅构建游标分页的WHERE条件（不包含ORDER BY）
     *
     * <p>ForSQL ServerWait for scenes that need to be sorted from the outer layer to avoid repetition from the internal and external layers.ORDER BY</p>
     *
     * @param sql    SQLBuilder
     * @param config PageSqlConfigure
     */
    protected void buildCursorConditionOnly(StringBuilder sql, PageSql config) {
        boolean noCondition = StringUtil.isBlank(config.getQueryFilter());
        // Auditor apologizedless.Filter Conditions
        if (noCondition) {
            sql.append(" WHERE ");
        }

        // UsebuildPrimaryKeysProcessingPrimary key
        List<String> primaryKeys = buildPrimaryKeys(config.getPrimaryKeys());

        // Cursor for Build Composite KeysminPageCondition: (pk1 > ?) OR (pk1 = ? AND pk2 > ?) OR ...
        buildCursorCondition(sql, primaryKeys, noCondition);
    }

    /**
     * YesminPageQuestionAddORDER BYSub sentences (press)Primary keySort)
     *
     * <p>Used ingetPageSqlThe method is non-goatminPageQuestionAddORDER BYSubword, make sure.minPageResultCoherence</p>
     * <p>Note: Non-kidsminPageThe scene must also.AddORDER BY，NoIt leads to data duplication or omission.</p>
     *
     * @param sql    SQLBuilder
     * @param config PageSqlConfigure
     */
    protected void appendOrderByPrimaryKeys(StringBuilder sql, PageSql config) {
        // UsebuildPrimaryKeysProcessingPrimary key
        final List<String> primaryKeys = buildPrimaryKeys(config.getPrimaryKeys());
        // Add ORDER BY - You have to.Primary keySort to ensure cursorminPagewith
        sql.append(" ORDER BY ").append(StringUtil.join(primaryKeys, StringUtil.COMMA));
    }

    /**
     * Build CursorminPageYes.ParameterCore logic of arrays (excluding data)LibrarySpecificOFFSETWait.Parameter）
     *
     * <p>返回游标条件参数，不包含pageSize等分页参数</p>
     *
     * @param cursors Current cursorValueArray
     * @return CursorConditionParameterarray, ifNoneCurseBacknull
     */
    protected Object[] buildCursorArgs(Object[] cursors) {
        if (null == cursors || cursors.length == 0) {
            return null;
        }
        // SingleFieldPrimary key：Just...pk > ?，ParameterYes[last_pk]
        int pkCount = cursors.length;
        if (pkCount == 1) {
            return new Object[]{cursors[0]};
        }

        // Composite Keys (Accountable Number Supported)Primary key）
        // WHERE ConditionYes(pk1 > ?) OR (pk1 = ? AND pk2 > ?) OR (pk1 = ? AND pk2 = ? AND pk3 > ?) OR ...
        //
        // ParameterVolume Represents: Sum of equal number columns
        // - 1individualPrimary key：1individualParameter (pk1 > ?)
        // - 2individualPrimary key：1+2=3individualParameter (pk1 > ?) OR (pk1 = ? AND pk2 > ?)
        // - 3individualPrimary key：1+2+3=6individualParameter (pk1 > ?) OR (pk1 = ? AND pk2 > ?) OR (pk1 = ? AND pk2 = ? AND pk3 > ?)
        // - nindividualPrimary key：1+2+...+n = n*(n+1)/2 individualParameter
        //
        // Parametertried2individualPrimary key，cursors=[v1, v2]）：
        // [v1, v1, v2] CorrespondCondition (pk1 > v1) OR (pk1 = v1 AND pk2 > v2)
        // Parametertried3individualPrimary key，cursors=[v1, v2, v3]）：
        // [v1, v1, v2, v1, v2, v3] CorrespondCondition (pk1 > v1) OR (pk1 = v1 AND pk2 > v2) OR (pk1 = v1 AND pk2 = v2 AND pk3 > v3)
        int paramCount = pkCount * (pkCount + 1) / 2;
        Object[] cursorArgs = new Object[paramCount];

        int index = 0;
        // Every timePrimary keyField，GenerateCorrespondingParameter
        for (int i = 0; i < pkCount; i++) {
            // For the firsti individualPrimary key，All ahead.Primary keyWe all need the equivalent.Condition
            for (int j = 0; j < i; j++) {
                cursorArgs[index++] = cursors[j];
            }
            // The 180thi individualPrimary keyUse greater thanCondition
            cursorArgs[index++] = cursors[i];
        }

        return cursorArgs;
    }

    /**
     * Based on currently runningParameter，Dynamic  PlanningWHERE QuestionCondition
     */
    private String buildQueryCondition(BooleanFilter baseQuery, List<Object> args) {
        if (baseQuery == null) {
            return null;
        }
        List<BooleanFilter> clauses = baseQuery.getClauses();
        List<AbstractFilter> rootFilters = baseQuery.getFilters();
        if (CollectionUtils.isEmpty(clauses) && CollectionUtils.isEmpty(rootFilters)) {
            return null;
        }
        StringBuilder sql = new StringBuilder();
        if (!CollectionUtils.isEmpty(rootFilters)) {
            createFilterSql(rootFilters, args, sql);
            return sql.length() > 0 ? sql.toString() : null;
        }
        appendConditionSql(clauses, args, sql);
        return sql.length() > 0 ? sql.toString() : null;
    }

    /**
     * Parsing/processing [T]Objective CombinedQuery for End Reader]SQL，Dynamic puzzle queryCondition
     */
    private String buildTargetReaderSql(String querySql, String condition) {
        // NoneQuestionCondition，DirectBackOriginalSQL
        if (StringUtil.isBlank(condition)) {
            return trimTrailingAndOrWhere(querySql);
        }
        // Autojudge AddWHERE orAND
        String keyword = querySql.toUpperCase(Locale.ROOT).contains(" WHERE ") ? " AND " : " WHERE ";
        return querySql + keyword + condition;
    }

    /**
     * 3, 2, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 3, 3, 4, 3, 3, 3, 4, 4, favoured, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 10, 3SQLIt's redundant.WHERE / AND Keyword
     */
    private String trimTrailingAndOrWhere(String sql) {
        String result = sql.replaceAll("(?i)\\s+AND\\s*$", "");
        result = result.replaceAll("(?i)\\s+WHERE\\s*$", "");
        return result;
    }

    /**
     * Based onCondition Dynamic assemblysql andParameter
     */
    private void createFilterSql(List<AbstractFilter> filters, List<Object> args, StringBuilder sql) {
        int size = filters.size();
        for (int i = 0; i < size; i++) {
            AbstractFilter abstractFilter = filters.get(i);
            if (i > 0) {
                sql.append(" ").append(abstractFilter.getOperation().toUpperCase()).append(" ");
            }
            if (abstractFilter instanceof InFilter) {
                InFilter inList = (InFilter) abstractFilter;
                List<Object> binds = inList.getBindValues();
                if (CollectionUtils.isEmpty(binds)) {
                    throw new SdkException("InListFilter bindValues can not be empty.");
                }
                sql.append(buildWithQuotation(inList.getName())).append(" IN (");
                for (int j = 0; j < binds.size(); j++) {
                    if (j > 0) {
                        sql.append(StringUtil.COMMA);
                    }
                    sql.append("?");
                }
                sql.append(")");
                args.addAll(binds);
                continue;
            }
            FilterEnum filterEnum = FilterEnum.getFilterEnum(abstractFilter.getFilter());
            sql.append(abstractFilter.getName());
            if (filterEnum == FilterEnum.IS_NULL || filterEnum == FilterEnum.IS_NOT_NULL) {
                sql.append(" ").append(filterEnum.getName().toUpperCase());
                continue;
            }
            sql.append(String.format(" %s ?", filterEnum.getName()));
            switch (filterEnum) {
                case EQUAL:
                case NOT_EQUAL:
                case LT:
                case LT_AND_EQUAL:
                case GT:
                case GT_AND_EQUAL:
                    args.add(abstractFilter.getValue());
                    break;
                default:
                    throw new SdkException("Unsupported filter type: " + filterEnum.getName());
            }
        }
    }

    /**
     * Embedded{@link BooleanFilter} S spellParameterChemicalCondition（In between the lines.{@link OperationEnum} Connect）。
     *
     * @param clauses Sub sentencesList
     * @param args    ParameterList
     * @param sql     OutputSQL Snippet
     */
    private void appendConditionSql(List<BooleanFilter> clauses, List<Object> args, StringBuilder sql) {
        int size = clauses.size();
        for (int i = 0; i < size; i++) {
            BooleanFilter booleanFilter = clauses.get(i);
            List<AbstractFilter> filters = booleanFilter.getFilters();
            if (CollectionUtils.isEmpty(filters)) {
                continue;
            }
            if (i > 0) {
                sql.append(" ").append(booleanFilter.getOperationEnum().name().toUpperCase()).append(" ");
            }
            sql.append("(");
            createFilterSql(filters, args, sql);
            sql.append(")");
        }
    }

    /**
     * Get queriesConditionSQL
     *
     * @param commandConfig
     * @return
     */
    private String getQueryFilterSql(CommandConfig commandConfig) {
        List<Filter> filter = commandConfig.getFilter();
        if (CollectionUtils.isEmpty(filter)) {
            return "";
        }
        Table table = commandConfig.getTable();
        Map<String, Field> fieldMap = new HashMap<>();
        table.getColumn().forEach(field -> fieldMap.put(field.getName(), field));

        // Filter ConditionsSQL
        StringBuilder sql = new StringBuilder();

        // CollapseANDSQL
        String addSql = buildFilterSql(fieldMap, OperationEnum.AND.getName(), filter);
        // IfAddConditionExistence
        if (StringUtil.isNotBlank(addSql)) {
            sql.append(addSql);
        }

        // CollapseORSQL
        String orSql = buildFilterSql(fieldMap, OperationEnum.OR.getName(), filter);
        // IfOrConditionandAddConditionBoth exist.
        if (StringUtil.isNotBlank(orSql) && StringUtil.isNotBlank(addSql)) {
            sql.append(" OR ");
        }
        sql.append(orSql);

        // CustomSQL
        Optional<Filter> sqlFilter = filter.stream().filter(f -> StringUtil.equals(f.getOperation(), OperationEnum.SQL.getName())).findFirst();
        sqlFilter.ifPresent(f -> sql.append(f.getValue()));

        // If there is.ConditionAddWHERE
        if (StringUtil.isNotBlank(sql.toString())) {
            // WHERE (USER.USERNAME = 'zhangsan' AND USER.AGE='20') OR (USER.TEL='18299996666')
            sql.insert(0, " WHERE ");
        }
        return sql.toString();
    }

    /**
     * To re-list.
     *
     * @param column
     * @return
     */
    private List<Field> filterColumn(List<Field> column) {
        Set<String> mark = new HashSet<>();
        List<Field> fields = new ArrayList<>();
        for (Field c : column) {
            String name = c.getName();
            if (StringUtil.isBlank(name)) {
                throw new SdkException("The field name can not be empty.");
            }
            if (!mark.contains(name)) {
                fields.add(c);
                mark.add(name);
            }
        }
        return fields;
    }

    /**
     * Based onFilter ConditionsGet queriesSQL
     *
     * @param fieldMap
     * @param operator {@link OperationEnum}
     * @param filter
     * @return
     */
    private String buildFilterSql(Map<String, Field> fieldMap, String operator, List<Filter> filter) {
        List<Filter> list = filter.stream().filter(f -> StringUtil.equals(f.getOperation(), operator)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }

        int size = list.size();
        int end = size - 1;
        StringBuilder sql = new StringBuilder();
        sql.append("(");
        Filter c = null;
        for (int i = 0; i < size; i++) {
            c = list.get(i);
            Field field = fieldMap.get(c.getName());
            Assert.notNull(field, "Condition field is invalid.");
            // "USER" = 'zhangsan'
            sql.append(buildWithQuotation(field.getName()));
            // If usedFunctionsNo repay marks.
            FilterEnum filterEnum = FilterEnum.getFilterEnum(c.getFilter());
            switch (filterEnum) {
                case IN:
                    sql.append(StringUtil.SPACE).append(FilterEnum.IN.getName()).append(StringUtil.SPACE).append("(");
                    sql.append(c.getValue());
                    sql.append(")");
                    break;
                case IS_NULL:
                case IS_NOT_NULL:
                    sql.append(StringUtil.SPACE).append(filterEnum.getName().toUpperCase()).append(StringUtil.SPACE);
                    break;
                default:
                    // > 10
                    sql.append(StringUtil.SPACE).append(c.getFilter()).append(StringUtil.SPACE);
                    sql.append(buildFilterValue(c.getValue()));
                    break;
            }
            if (i < end) {
                sql.append(StringUtil.SPACE).append(operator).append(StringUtil.SPACE);
            }
        }
        sql.append(")");
        return sql.toString();
    }

    /**
     * AccessConditionValueQuotes (excludes quotation marks if Rates are included)
     *
     * @param value
     * @return
     */
    private String buildFilterValue(String value) {
        if (StringUtil.isNotBlank(value)) {
            // ExcludeScheduledExpression
            if (QuartzFilterEnum.getQuartzFilterEnum(value) == null) {
                // SystemFunctionsExpression$select max(update_time)$
                Matcher matcher = Pattern.compile("^[$].*[$]$").matcher(value);
                if (matcher.find()) {
                    return StringUtil.substring(value, 1, value.length() - 1);
                }
            }
        }
        return StringUtil.SINGLE_QUOTATION + value + StringUtil.SINGLE_QUOTATION;
    }

    /**
     * GenerateSQL
     *
     * @param map
     * @param builderType
     * @param config
     */
    private void buildSql(Map<String, String> map, SqlBuilderEnum builderType, SqlBuilderConfig config) {
        map.put(builderType.getName(), builderType.getSqlBuilder().buildSql(config));
    }

    /**
     * BackTablePrimary key
     *
     * @param md
     * @param catalog
     * @param schema
     * @param tableName
     * @return
     * @throws SQLException
     */
    private List<String> findTablePrimaryKeys(DatabaseMetaData md, String catalog, String schema, String tableName) throws SQLException {
        // Based onTable nameAccessPrimary keyResultSet
        ResultSet rs = null;
        List<String> primaryKeys = new ArrayList<>();
        try {
            rs = md.getPrimaryKeys(catalog, schema, tableName);
            while (rs.next()) {
                primaryKeys.add(rs.getString("COLUMN_NAME"));
            }
        } finally {
            DatabaseUtil.close(rs);
        }
        return primaryKeys;
    }

    protected List<Object[]> batchRows(List<Field> fields, List<Map> data) {
        return data.stream().map(row -> batchRow(fields, row)).collect(Collectors.toList());
    }

    private Object[] batchRow(List<Field> fields, Map row) {
        List<Object> args = new ArrayList<>();
        for (Field f : fields) {
            Object val = row.get(f.getName());
            if (val instanceof CustomData) {
                CustomData cd = (CustomData) val;
                args.addAll(cd.apply());
                continue;
            }
            args.add(wrapBindParameter(f, val));
        }
        return args.toArray();
    }

    /**
     * OceanBase Wait.UPSERT Medium binary column bindings are sensitive, visible statementsJDBC TypeAvoidsetObject InferenceFailed。
     */
    protected Object wrapBindParameter(Field field, Object val) {
        if (!(val instanceof byte[])) {
            return val;
        }
        return new SqlParameterValue(resolveBinarySqlType(field), val);
    }

    private static int resolveBinarySqlType(Field field) {
        if (field == null || StringUtil.isBlank(field.getTypeName())) {
            return Types.VARBINARY;
        }
        String type = field.getTypeName().trim().toUpperCase(Locale.ROOT);
        if (type.endsWith("BLOB") || "BLOB".equals(type)) {
            return Types.BLOB;
        }
        return Types.VARBINARY;
    }

    private void printTraceLog(PluginContext context, String event, Map row, boolean success, String message) {
        if (success) {
            // OnlyOntraceIdTimeoutLog
            if (context.isEnablePrintTraceInfo()) {
                logger.info("{}Table Event}, Execute {} Success,}", context.getTraceId(), context.getTargetTable().getName(), context.getEvent(), event, row);
            }
            return;
        }
        logger.error("{} Table Event}, Execution } Failed: }, DATA:}", context.getTraceId(), context.getTargetTable().getName(), context.getEvent(), event, message, row);
    }

    /**
     * GenerateColumn definitionTypeSnippets (excluding listings) for the same componentsRevisedWait.DDL。
     */
    public String formatPhysicalType(Field sourceDefinition) {
        String raw = sourceDefinition.getTypeName();
        // EmptyTypePressVARCHAR Bottom of the road, Responses to the illegal downstream fusion.DDL
        if (StringUtil.isBlank(raw)) {
            return "VARCHAR(255)";
        }
        String t = raw.trim().toUpperCase(Locale.ROOT);
        int size = Math.max(0, sourceDefinition.getColumnSize());
        int ratio = Math.max(0, sourceDefinition.getRatio());
        //Yes.SettingsOutlook saluteYes.Type
        if ("DECIMAL".equals(t) || "NUMERIC".equals(t) || "NUMBER".equals(t)) {
            return formatDecimalTypeFragment(t, size, ratio);
        }
        //CHAR / VARCHAR / NCHAR / NVARCHAR / Binary(BINARY/VARBINARY/RAW)
        if (t.contains("CHAR") || t.contains("BINARY") || t.contains("RAW") || t.contains("BYTEA")) {
            return formatCharOrBinaryTypeFragment(t, size);
        }
        return t;
    }

    private static String formatDecimalTypeFragment(String t, int size, int ratio) {
        if (size > 0 && ratio > 0) {
            return String.format(Locale.ROOT, "%s(%d,%d)", t, size, ratio);
        } else if (size > 0 && ratio == 0) {
            return String.format(Locale.ROOT, "%s(%d)", t, size);
        }
        return t;
    }

    private String formatCharOrBinaryTypeFragment(String t, int size) {
        int len = size > 0 ? size : defaultLengthForCharFamily(t);
        return String.format(Locale.ROOT, "%s(%d)", t, len);
    }

    private int defaultLengthForCharFamily(String t) {
        if (t.contains("TEXT") || "CLOB".equals(t) || "NCLOB".equals(t)) {
            return 65535;
        }
        return 255;
    }

}