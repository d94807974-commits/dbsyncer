/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.connector.oracle.cdc;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import org.dbsyncer.common.QueueOverflowException;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.connector.oracle.OracleException;
import org.dbsyncer.connector.oracle.logminer.LogMiner;
import org.dbsyncer.connector.oracle.logminer.LogMinerHelper;
import org.dbsyncer.connector.oracle.logminer.RedoEvent;
import org.dbsyncer.connector.oracle.logminer.parser.OracleLobRedoHelper;
import org.dbsyncer.connector.oracle.logminer.parser.OracleUnchangedValue;
import org.dbsyncer.connector.oracle.logminer.parser.impl.DeleteSql;
import org.dbsyncer.connector.oracle.logminer.parser.impl.InsertSql;
import org.dbsyncer.connector.oracle.logminer.parser.impl.UpdateSql;
import org.dbsyncer.sdk.config.DatabaseConfig;
import org.dbsyncer.sdk.connector.database.Database;
import org.dbsyncer.sdk.constant.ConnectorConstant;
import org.dbsyncer.sdk.listener.AbstractDatabaseListener;
import org.dbsyncer.sdk.listener.ChangedEvent;
import org.dbsyncer.sdk.listener.event.DDLChangedEvent;
import org.dbsyncer.sdk.listener.event.RowChangedEvent;
import org.dbsyncer.sdk.model.ChangedOffset;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.util.PrimaryKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author AE86
 * @Version 1.0.0
 * @Date 2022-05-12 21:14
 */
public class OracleListener extends AbstractDatabaseListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String REDO_POSITION = "position";
    private final Map<String, List<Field>> tableFiledMap = new ConcurrentHashMap<>();
    private LogMiner logMiner;

    @Override
    public void init() {
        super.init();
        sourceTable.forEach(table -> tableFiledMap.put(table.getName(), table.getColumn()));
    }

    @Override
    public void start() {
        try {
            final DatabaseConfig config = getConnectorInstance().getConfig();
            String driverClassName = config.getDriverClassName();
            String username = config.getUsername();
            String password = config.getPassword();
            String url = config.getUrl();
            boolean containsPos = snapshot.containsKey(REDO_POSITION);
            logMiner = new LogMiner(username, password, url, schema, driverClassName);
            logMiner.setStartScn(containsPos ? Long.parseLong(snapshot.get(REDO_POSITION)) : 0);
            logMiner.registerEventListener((event) -> {
                try {
                    parseEvent(event);
                } catch (JSQLParserException e) {
                    logger.warn("Other Organiser", event.getRedoSql(), e);
                } catch (Exception e) {
                    logger.error("Parsing sql anomaly: }", event.getRedoSql(), e);
                }
            });
            logMiner.start();
        } catch (Exception e) {
            logger.error("Start failed: {}", e.getMessage(), e);
            throw new OracleException(e);
        }
    }

    private void trySendEvent(ChangedEvent event) {
        try {
            // If you consumeEventFailed，Try again
            while (logMiner.isConnected()) {
                try {
                    sendChangedEvent(event);
                    break;
                } catch (QueueOverflowException e) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * ParsingEvent
     *
     * @param event
     * @throws Exception
     */
    private void parseEvent(RedoEvent event) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(event.getRedoSql());
        if (statement instanceof Update) {
            Update update = (Update) statement;
            String tableName = getTableName(update.getTable());
            if (tableFiledMap.containsKey(tableName)) {
                List<Field> fields = tableFiledMap.get(tableName);
                UpdateSql parser = new UpdateSql(update, fields);
                List<Object> data = parser.parseColumns();
                // We're short of guards.+ LargeLOB Yes.EMPTY_BLOB/EMPTY_CLOB Placed, all required ApproachPrimary keyCheck back.Value
                List<Integer> indexes = findUpdateColumnsNeedFill(fields, data);
                if (!fillColumnsByPrimaryKey(tableName, fields, data, indexes)) {
                    logger.error("UPDATE has failed to recheck the source list, discarding events to avoid writing empty LOB. table=}, scn={}, sql=}",
                            tableName, event.getScn(), event.getRedoSql());
                    return;
                }
                trySendEvent(new RowChangedEvent(tableName, ConnectorConstant.OPERTION_UPDATE, data, null, event.getScn()));
            }
            return;
        }

        if (statement instanceof Insert) {
            Insert insert = (Insert) statement;
            String tableName = getTableName(insert.getTable());
            if (tableFiledMap.containsKey(tableName)) {
                List<Field> fields = tableFiledMap.get(tableName);
                InsertSql parser = new InsertSql(insert, fields);
                List<Object> data = parser.parseColumns();
                List<Integer> lobIndexes = OracleLobRedoHelper.findEmptyLobPlaceholderIndexes(fields, data);
                if (!fillColumnsByPrimaryKey(tableName, fields, data, lobIndexes)) {
                    logger.error("INSERT failed to recheck source list LOB, discarding events to avoid writing empty LOB. table=}, scn=}, sql=}",
                            tableName, event.getScn(), event.getRedoSql());
                    return;
                }
                trySendEvent(new RowChangedEvent(tableName, ConnectorConstant.OPERTION_INSERT, data, null, event.getScn()));
            }
            return;
        }

        if (statement instanceof Delete) {
            Delete delete = (Delete) statement;
            String tableName = getTableName(delete.getTable());
            if (tableFiledMap.containsKey(tableName)) {
                DeleteSql parser = new DeleteSql(delete, tableFiledMap.get(tableName));
                trySendEvent(new RowChangedEvent(tableName, ConnectorConstant.OPERTION_DELETE, parser.parseColumns(), null, event.getScn()));
            }
        }

        if (statement instanceof Alter) {
            Alter alter = (Alter) statement;
            String tableName = getTableName(alter.getTable());
            if (tableFiledMap.containsKey(tableName)) {
                logger.info("sql:{}", event.getRedoSql());
                trySendEvent(new DDLChangedEvent(tableName, ConnectorConstant.OPERTION_ALTER, event.getRedoSql(), null, event.getScn()));
            }
        }
    }

    /**
     * Find out.UPDATE redo Column subscripts that do not appear in{@link OracleUnchangedValue} Sentry.
     * <p>Missingnull Writing, AllField MERGE Yes.Objective CombinedSide unchangedLOB Clear。
     */
    private List<Integer> findUnchangedIndexes(List<Object> data) {
        List<Integer> indexes = new ArrayList<>();
        if (CollectionUtils.isEmpty(data)) {
            return indexes;
        }
        for (int i = 0; i < data.size(); i++) {
            if (OracleUnchangedValue.isUnchanged(data.get(i))) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    /**
     * UPDATE Reconnaissance: missing sentry+ LogMiner Yeah.LOB CommonEMPTY_BLOB()/EMPTY_CLOB() Place.
     * <p>Change onlyOtherTypeYes TimeLOB For the sentry; to grow up directlyLOB TimeSET Most sidesEMPTY_*()，I want both to be heard.Value。
     */
    private List<Integer> findUpdateColumnsNeedFill(List<Field> fields, List<Object> data) {
        List<Integer> indexes = findUnchangedIndexes(data);
        for (Integer idx : OracleLobRedoHelper.findEmptyLobPlaceholderIndexes(fields, data)) {
            if (!indexes.contains(idx)) {
                indexes.add(idx);
            }
        }
        return indexes;
    }

    /**
     * PressPrimary keyCheck back.Source table，Use it.ValueOverwriteSpecifies the column subscript (UPDATE No sentry posts /INSERT LOB I'm not Achievements.
     *
     * @param indexes Column subscripts to fill back; empty directSuccess
     * @return false It means we need to look back.Failed，Caller should be discardedEvent
     */
    private boolean fillColumnsByPrimaryKey(String tableName, List<Field> fields, List<Object> data, List<Integer> indexes) {
        // NoneRecoverable (normal small)UPDATE / None LOB I'm in position.INSERT）
        if (CollectionUtils.isEmpty(indexes)) {
            return true;
        }
        if (CollectionUtils.isEmpty(data) || CollectionUtils.isEmpty(fields) || data.size() != fields.size()) {
            return false;
        }

        List<Field> pkFields = PrimaryKeyUtil.findPrimaryKeyFields(fields);
        if (CollectionUtils.isEmpty(pkFields)) {
            logger.error("Table {} No master key, cannot check back", tableName);
            return false;
        }

        // Primary keyMust be there.redo Itчs a lot.NothenNoneLegal source line
        Object[] pkArgs = buildPrimaryKeyArgs(tableName, fields, data, pkFields);
        if (pkArgs == null) {
            return false;
        }

        Database database = (Database) connectorService;
        // Only IncludesSELECT To be filled back to avoid width/overLOB Line Rip
        String sql = buildSelectSqlByPrimaryKey(database, tableName, fields, indexes, pkFields);
        if (sql == null) {
            return false;
        }

        try {
            Map<String, Object> row = getConnectorInstance().execute(databaseTemplate -> {
                try {
                    return databaseTemplate.queryForMap(sql, pkArgs);
                } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                    // Line deleted or non-existent: supplementsandQuestionErrorSector Entitiesmin，Upstream uniform emptied and discardedEvent
                    return null;
                }
            });
            if (CollectionUtils.isEmpty(row)) {
                logger.error("Table {} Turn back no data by primary key, args=}", tableName, pkArgs);
                return false;
            }
            // PointOverwrite：Reservationsredo Reassembly.Value（VisibleNULL），Sentry only/ranking
            for (Integer idx : indexes) {
                Field field = fields.get(idx);
                data.set(idx, row.get(field.getName()));
            }
            return true;
        } catch (Exception e) {
            logger.error("Table {} Failed to check back: }", tableName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Fromredo Row dataassemblyPrimary keyTieParameter；Primary keyWhile missing or still a sentryBack null。
     */
    private Object[] buildPrimaryKeyArgs(String tableName, List<Field> fields, List<Object> data, List<Field> pkFields) {
        Object[] args = new Object[pkFields.size()];
        for (int i = 0; i < pkFields.size(); i++) {
            Field pk = pkFields.get(i);
            int idx = indexOfField(fields, pk.getName());
            if (idx < 0 || OracleUnchangedValue.isUnchanged(data.get(idx))) {
                logger.error("Table/2001/8", tableName, pk.getName());
                return null;
            }
            args[i] = data.get(idx);
        }
        return args;
    }

    /**
     * Collapse PressPrimary keyCheck back.SQL：Include onlyindexes Corresponding column.
     *
     * @return Other OrganiserBack null
     */
    private String buildSelectSqlByPrimaryKey(Database database, String tableName, List<Field> fields,
                                              List<Integer> indexes, List<Field> pkFields) {
        List<String> selectCols = new ArrayList<>(indexes.size());
        for (Integer idx : indexes) {
            if (idx == null || idx < 0 || idx >= fields.size()) {
                logger.error("Table {} Filling back the subscript illegal: }", tableName, idx);
                return null;
            }
            selectCols.add(database.buildWithQuotation(fields.get(idx).getName()));
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(StringUtil.join(selectCols, StringUtil.COMMA));
        sql.append(" FROM ");
        if (StringUtil.isNotBlank(schema)) {
            sql.append(database.buildWithQuotation(schema)).append(StringUtil.POINT);
        }
        sql.append(database.buildWithQuotation(tableName)).append(" WHERE ");
        List<String> pkNames = pkFields.stream().map(Field::getName).collect(Collectors.toList());
        database.appendPrimaryKeys(sql, pkNames);
        return sql.toString();
    }

    private int indexOfField(List<Field> fields, String name) {
        for (int i = 0; i < fields.size(); i++) {
            if (StringUtil.equals(fields.get(i).getName(), name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Map<String, String> captureSnapshot() {
        try {
            final DatabaseConfig config = getConnectorInstance().getConfig();
            try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword())) {
                long scn = LogMinerHelper.getCurrentScn(connection);
                snapshot.put(REDO_POSITION, String.valueOf(scn));
                Map<String, String> captured = new HashMap<>(1);
                captured.put(REDO_POSITION, String.valueOf(scn));
                return captured;
            }
        } catch (Exception e) {
            logger.error("Failed to capture Oracle SCN location: }", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    public void close() {
        if (logMiner != null) {
            logMiner.close();
        }
    }

    @Override
    public void refreshEvent(ChangedOffset offset) {
        snapshot.put(REDO_POSITION, String.valueOf(offset.getPosition()));
    }

    private String getTableName(Table table) {
        return table == null ? StringUtil.EMPTY : StringUtil.replace(table.getName(), StringUtil.DOUBLE_QUOTATION, StringUtil.EMPTY);
    }

}
