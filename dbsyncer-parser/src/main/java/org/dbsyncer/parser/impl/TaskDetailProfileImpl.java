/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.impl;

import org.apache.lucene.index.IndexableField;
import org.dbsyncer.common.model.Paging;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.NumberUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.parser.TaskDetailProfile;
import org.dbsyncer.parser.model.TaskDetailQuery;
import org.dbsyncer.parser.util.SqlResultRowUtil;
import org.dbsyncer.parser.util.TaskDetailQuerySupport;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.dbsyncer.sdk.enums.DatabaseSyncDetailTypeEnum;
import org.dbsyncer.sdk.enums.StorageEnum;
import org.dbsyncer.sdk.filter.FieldResolver;
import org.dbsyncer.sdk.filter.Query;
import org.dbsyncer.sdk.storage.ExecuteRequest;
import org.dbsyncer.sdk.storage.StorageService;
import org.dbsyncer.sdk.util.TaskDetailUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link TaskDetailProfile} Achieved:task_detail JOIN meta / table_group，Storage SideminPage。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-20 15:00
 */
@Component
public final class TaskDetailProfileImpl implements TaskDetailProfile {

    private static final String SELECT_COLUMNS =
            "d.ID AS id, d.CREATE_TIME AS createTime, "
                    + "dm.START_TIME AS startTime, dm.UPDATE_TIME AS updateTime, "
                    + "d.TABLE_GROUP_ID AS tableGroupId, d.TYPE AS type, d.TARGET_TABLE AS targetTable, "
                    + "d.IS_SUCCESS AS isSuccess, d.ERROR AS error, d.DATA AS data, "
                    + "tg.SOURCE_TABLE AS sourceTable, tg.TARGET_TABLE AS targetTableName, "
                    + "tg.SOURCE_DATABASE AS sourceDatabase, tg.TARGET_DATABASE AS targetDatabase, "
                    + "tg.SOURCE_SCHEMA AS sourceSchema, tg.TARGET_SCHEMA AS targetSchema, "
                    + "tg.SOURCE_TOTAL AS sourceTotal, tg.TARGET_TOTAL AS targetTotal, "
                    + "tg.SORT_INDEX AS sortIndex, "
                    + "dm.TOTAL AS total, dm.SUCCESS AS success, dm.FAIL AS fail, "
                    + "dm.DIFF AS diff, dm.FIXED AS fixed, dm.STATE AS state";

    private static final String FROM_JOIN =
            " FROM %s d "
                    + "INNER JOIN dbsyncer_meta dm ON dm.TASK_ID = d.TABLE_GROUP_ID AND dm.IS_TASK_DETAIL = 1 "
                    + "INNER JOIN dbsyncer_table_group tg ON tg.ID = d.TABLE_GROUP_ID ";

    /**
     * and SELECT ... AS and a single name for the camels.H2 When it's possible to foldTYPE/UPDATETIME，
     * Read and write to fix these when matchingkey，Ensure FrontendJSON FieldSteady.
     */
    private static final String[] SELECT_ALIASES = {
            "id", "createTime", "startTime", "updateTime", "tableGroupId", "type", "targetTable",
            "isSuccess", "error", "data", "sourceTable", "targetTableName",
            "sourceDatabase", "targetDatabase", "sourceSchema", "targetSchema",
            "sourceTotal", "targetTotal", "sortIndex",
            "total", "success", "fail", "diff", "fixed", "state"
    };

    @Resource
    private StorageService storageService;

    @Override
    public Paging queryResults(TaskDetailQuery query) {
        Assert.notNull(query, "Query parameters cannot be empty");
        String taskId = query.getTaskId();
        TaskDetailQuerySupport.assertTaskId(taskId);
        int pageNum = query.getPageNum();
        int pageSize = query.getPageSize();
        String detailTable = detailTableName(taskId);

        List<Object> args = new ArrayList<>();
        String where = TaskDetailQuerySupport.buildWhere(query, args);
        String orderSql = TaskDetailQuerySupport.resolveOrderSql(query);

        long total = queryCount(detailTable, where, args);
        Paging paging = new Paging(pageNum, pageSize);
        paging.setTotal(total);
        if (total <= 0) {
            paging.setData(Collections.emptyList());
            return paging;
        }

        String sql = "SELECT " + SELECT_COLUMNS + String.format(FROM_JOIN, detailTable) + where + orderSql;
        List<Map<String, Object>> rows = storageService.queryList(
                ExecuteRequest.of(sql, args.toArray()).page(pageNum, pageSize));
        List<Map<String, Object>> data = new ArrayList<>(rows == null ? 0 : rows.size());
        if (!CollectionUtils.isEmpty(rows)) {
            for (Map<String, Object> row : rows) {
                data.add(toDisplayRow(row));
            }
        }
        paging.setData(data);
        return paging;
    }

    @Override
    public Map<String, Object> getDetail(TaskDetailQuery query) {
        Assert.notNull(query, "Query parameters cannot be empty");
        String taskId = query.getTaskId();
        TaskDetailQuerySupport.assertTaskId(taskId);
        if (StringUtil.isBlank(query.getDetailId())) {
            return null;
        }
        String detailTable = detailTableName(taskId);
        List<Object> args = new ArrayList<>();
        String where = TaskDetailQuerySupport.buildWhere(query, args);
        String sql = "SELECT " + SELECT_COLUMNS + String.format(FROM_JOIN, detailTable) + where;
        List<Map<String, Object>> rows = storageService.queryList(ExecuteRequest.of(sql, args.toArray()).page(1, 1));
        if (CollectionUtils.isEmpty(rows)) {
            return null;
        }
        return toDisplayRow(rows.get(0));
    }

    @Override
    public void delete(String taskId, String id) {
        storageService.remove(StorageEnum.TASK_DETAIL, taskId, id);
    }

    @Override
    public Map getData(String taskId, String id) {
        Query query = new Query();
        Map<String, FieldResolver> fieldResolvers = new HashMap<>();
        fieldResolvers.put(ConfigConstant.BINLOG_DATA, (FieldResolver<IndexableField>) field -> field.binaryValue().bytes);
        query.setFieldResolverMap(fieldResolvers);
        query.setTaskId(taskId);
        query.addFilter(ConfigConstant.CONFIG_MODEL_ID, id);
        query.setType(StorageEnum.TASK_DETAIL);
        return storageService.queryObject(query);
    }

    private long queryCount(String detailTable, String where, List<Object> args) {
        String sql = "SELECT COUNT(1) AS cnt " + String.format(FROM_JOIN, detailTable) + where;
        List<Map<String, Object>> rows = storageService.queryList(ExecuteRequest.of(sql, args.toArray()));
        if (CollectionUtils.isEmpty(rows)) {
            return 0L;
        }
        Object cnt = rows.get(0).get("cnt");
        if (cnt == null) {
            cnt = rows.get(0).values().iterator().next();
        }
        return NumberUtil.toLong(String.valueOf(cnt));
    }

    private Map<String, Object> toDisplayRow(Map<String, Object> sqlRow) {
        // Read first by IgnoreMappingResult，And write a fixed camel aliases.JSON Come on.TYPE/UPDATETIME）
        Map<String, Object> row = SqlResultRowUtil.toAliasRow(sqlRow, SELECT_ALIASES);
        TaskDetailUtil.mergeDetailRow(row);

        Object sourceTable = row.get("sourceTable");
        Object targetTableName = row.get("targetTableName");
        Object targetTable = row.get(ConfigConstant.DETAIL_TARGET_TABLE);
        Object sourceDatabase = row.get(ConfigConstant.DATABASE_SYNC_DETAIL_SOURCE_DATABASE);
        Object sourceSchema = row.get(ConfigConstant.DATABASE_SYNC_DETAIL_SOURCE_SCHEMA);
        Object targetDatabase = row.get(ConfigConstant.DATABASE_SYNC_DETAIL_TARGET_DATABASE);
        Object targetSchema = row.get(ConfigConstant.DATABASE_SYNC_DETAIL_TARGET_SCHEMA);
        Object sourceTotal = row.get(ConfigConstant.TASK_SOURCE_TOTAL);
        Object targetTotal = row.get(ConfigConstant.TASK_TARGET_TOTAL);
        Object sortIndex = row.get(ConfigConstant.TABLE_GROUP_SORT_INDEX);
        Object total = row.get(ConfigConstant.META_TOTAL);
        Object success = row.get(ConfigConstant.META_SUCCESS);
        Object fail = row.get(ConfigConstant.META_FAIL);
        Object diff = row.get(ConfigConstant.META_DIFF);
        Object fixed = row.get(ConfigConstant.META_FIXED);
        Object state = row.get(ConfigConstant.META_STATE);

        // Data Migration：Success/FailedBy TableMeta Whichever is applicable (cluster waves added);DATA Yeah.Overwrite sourceTotal
        Map<String, Object> detailContent = TaskDetailUtil.deserializeContent(row.get(ConfigConstant.BINLOG_DATA));
        if (detailContent.get(ConfigConstant.TASK_SOURCE_TOTAL) != null) {
            sourceTotal = detailContent.get(ConfigConstant.TASK_SOURCE_TOTAL);
        }
        boolean rowData = DatabaseSyncDetailTypeEnum.ROW_DATA.getCode()
                .equals(String.valueOf(row.get(ConfigConstant.CONFIG_MODEL_TYPE)));
        if (!rowData) {
            if (detailContent.get(ConfigConstant.DATABASE_SYNC_DETAIL_SUCCESS_TOTAL) != null) {
                success = detailContent.get(ConfigConstant.DATABASE_SYNC_DETAIL_SUCCESS_TOTAL);
            }
            if (detailContent.get(ConfigConstant.DATABASE_SYNC_DETAIL_FAIL_TOTAL) != null) {
                fail = detailContent.get(ConfigConstant.DATABASE_SYNC_DETAIL_FAIL_TOTAL);
            }
        }
        // TypeI'm the one that's the one that's the one of the one of the, the one of the one of the, the one of the, the one of the, the one of the, the... the one of the, the... the one of the, the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the- the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the... the the... the- the- the... the the... the the- the... the- the... the... the- the... the... the... the the the the- the the... the... the- the... the the the the- the the the the the the the- the the the the the the the the the the the the the the the the the the the the the the the theDATA.status；Form Backmeta.state
        Object detailStatus = detailContent.get(ConfigConstant.TASK_STATUS);
        if (detailStatus != null) {
            state = detailStatus;
        }

        putIfPresent(row, ConfigConstant.TASK_SOURCE_TABLE_NAME, sourceTable);
        putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_SOURCE_TABLE, sourceTable);
        Object displayTargetTable = targetTableName != null ? targetTableName : targetTable;
        putIfPresent(row, ConfigConstant.DATA_TARGET_TABLE_NAME, displayTargetTable);
        putIfPresent(row, ConfigConstant.DETAIL_TARGET_TABLE, displayTargetTable);
        putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_TARGET_TABLE, displayTargetTable);
        putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_SOURCE_DATABASE, sourceDatabase);
        putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_SOURCE_SCHEMA, sourceSchema);
        putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_TARGET_DATABASE, targetDatabase);
        putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_TARGET_SCHEMA, targetSchema);
        putIfPresent(row, ConfigConstant.TASK_SOURCE_TOTAL, sourceTotal);
        putIfPresent(row, ConfigConstant.TASK_TARGET_TOTAL, targetTotal);
        putIfPresent(row, ConfigConstant.TABLE_GROUP_SORT_INDEX, sortIndex);
        putIfPresent(row, ConfigConstant.TASK_DIFF_TOTAL, diff);
        putIfPresent(row, ConfigConstant.TASK_FIXED_TOTAL, fixed);
        putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_SUCCESS_TOTAL, success);
        putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_FAIL_TOTAL, fail);
        putIfPresent(row, ConfigConstant.META_TOTAL, total);
        putIfPresent(row, ConfigConstant.META_STATE, state);
        if (state != null) {
            // Presentationstatus：PriorityDATA.status，Nothenmeta.state
            row.put(ConfigConstant.TASK_STATUS, state);
        }
        return row;
    }

    private void putIfPresent(Map<String, Object> row, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String && StringUtil.isBlank((String) value)) {
            return;
        }
        row.put(key, value);
    }

    private String detailTableName(String taskId) {
        return "dbsyncer_task_detail_" + taskId;
    }
}
