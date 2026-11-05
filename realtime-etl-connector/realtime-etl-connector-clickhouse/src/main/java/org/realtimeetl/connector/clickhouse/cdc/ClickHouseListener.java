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
package org.realtimeetl.connector.clickhouse.cdc;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.clickhouse.ClickHouseException;
import org.realtimeetl.connector.clickhouse.constant.ClickHouseConfigConstant;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.listener.AbstractDatabaseListener;
import org.realtimeetl.sdk.listener.event.RowChangedEvent;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClickHouse IncrementalListener: based onPrimary keyTraveler 's round of inquiry (forMergeTree As soon as it is added.
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-05-29 23:50
 */
public final class ClickHouseListener extends AbstractDatabaseListener {

    private static final String SNAPSHOT_PREFIX = "cursor_";
    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 5;
    private static final int DEFAULT_BATCH_SIZE = 500;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private volatile boolean running;

    @Override
    public void start() {
        running = true;
        int interval = resolvePollIntervalSeconds();
        scheduledTaskService.start(interval * 1000L, this::poll);
        logger.info("ClickHouseListener started, poll interval={}s, batchSize={}", interval, resolveBatchSize());
    }

    @Override
    public void close() {
        running = false;
    }

    private void poll() {
        if (!running || CollectionUtils.isEmpty(sourceTable)) {
            return;
        }
        Database database = (Database) connectorService;
        DatabaseConnectorInstance instance = getConnectorInstance();
        for (Table table : sourceTable) {
            if (!CollectionUtils.isEmpty(filterTable) && !filterTable.contains(table.getName())) {
                continue;
            }
            try {
                pollTable(database, instance, table);
            } catch (Exception e) {
                logger.error("Crick House failed in the incremental round, table=}", table.getName(), e);
                errorEvent(e);
            }
        }
    }

    private void pollTable(Database database, DatabaseConnectorInstance instance, Table table) {
        List<Field> columns = table.getColumn();
        if (CollectionUtils.isEmpty(columns)) {
            return;
        }
        List<Field> pkFields = PrimaryKeyUtil.findPrimaryKeyFields(columns);
        if (CollectionUtils.isEmpty(pkFields) || pkFields.size() != 1) {
            throw new ClickHouseException(String.format("The Clickhouse log increment requires the primary value of a field, table = %s", table.getName()));
        }
        Field pkField = pkFields.get(0);
        if (!PrimaryKeyUtil.isSupportedCursor(columns)) {
            throw new ClickHouseException(String.format("ClickHouse Incremental Primary Key type does not support cursor, table = %s, field = %s", table.getName(), pkField.getName()));
        }

        String snapshotKey = SNAPSHOT_PREFIX + table.getName();
        Object lastCursor = parseCursor(snapshot.get(snapshotKey));
        String qualifiedTable = database.buildWithQuotation(table.getName());
        String pkName = database.buildWithQuotation(pkField.getName());
        String columnSql = buildColumnSql(database, columns);
        int batchSize = resolveBatchSize();

        StringBuilder sql = new StringBuilder("SELECT ").append(columnSql).append(" FROM ").append(qualifiedTable);
        List<Object> args = new ArrayList<>();
        if (lastCursor != null) {
            sql.append(" WHERE ").append(pkName).append(" > ?");
            args.add(lastCursor);
        }
        sql.append(" ORDER BY ").append(pkName).append(" ASC LIMIT ?");
        args.add(batchSize);

        List<Map<String, Object>> rows = instance.execute(databaseTemplate ->
                databaseTemplate.queryForList(sql.toString(), args.toArray()));
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }

        Object maxCursor = lastCursor;
        for (Map<String, Object> row : rows) {
            List<Object> rowData = new ArrayList<>(columns.size());
            for (Field column : columns) {
                rowData.add(row.get(column.getName()));
            }
            Object currentPk = row.get(pkField.getName());
            if (currentPk != null) {
                maxCursor = currentPk;
            }
            sendChangedEvent(new RowChangedEvent(table.getName(), ConnectorConstant.OPERTION_INSERT, rowData, null, null));
        }
        if (maxCursor != null && (lastCursor == null || compareCursor(maxCursor, lastCursor) > 0)) {
            snapshot.put(snapshotKey, String.valueOf(maxCursor));
            forceFlushEvent();
        }
    }

    private String buildColumnSql(Database database, List<Field> columns) {
        List<String> names = new ArrayList<>(columns.size());
        for (Field column : columns) {
            names.add(database.buildWithQuotation(column.getName()));
        }
        return StringUtil.join(names, StringUtil.COMMA);
    }

    private Object parseCursor(String cursor) {
        if (StringUtil.isBlank(cursor)) {
            return null;
        }
        if (NumberUtil.isCreatable(cursor)) {
            return cursor.contains(".") ? Double.valueOf(cursor) : NumberUtil.toLong(cursor, 0L);
        }
        return cursor;
    }

    private int compareCursor(Object current, Object last) {
        if (current instanceof Number && last instanceof Number) {
            return Double.compare(((Number) current).doubleValue(), ((Number) last).doubleValue());
        }
        return String.valueOf(current).compareTo(String.valueOf(last));
    }

    private int resolvePollIntervalSeconds() {
        if (connectorConfig == null || connectorConfig.getExtInfo() == null) {
            return DEFAULT_POLL_INTERVAL_SECONDS;
        }
        return NumberUtil.toInt(connectorConfig.getExtInfo().getProperty(ClickHouseConfigConstant.POLL_INTERVAL_SECONDS),
                DEFAULT_POLL_INTERVAL_SECONDS);
    }

    private int resolveBatchSize() {
        if (connectorConfig == null || connectorConfig.getExtInfo() == null) {
            return DEFAULT_BATCH_SIZE;
        }
        return NumberUtil.toInt(connectorConfig.getExtInfo().getProperty(ClickHouseConfigConstant.POLL_BATCH_SIZE),
                DEFAULT_BATCH_SIZE);
    }
}
