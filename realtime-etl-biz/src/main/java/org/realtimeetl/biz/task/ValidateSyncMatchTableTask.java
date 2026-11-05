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
package org.realtimeetl.biz.task;

import org.realtimeetl.biz.ValidateSyncService;
import org.realtimeetl.common.dispatch.AbstractDispatchTask;
import org.realtimeetl.common.enums.DispatchTaskEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.ValidateSyncProfile;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Revised verification tasksA walk.Match similar tables
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026/4/8
 */
@Service
public final class ValidateSyncMatchTableTask extends AbstractDispatchTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ValidateSyncService validateSyncService;

    @Resource
    private ValidateSyncProfile validateSyncProfile;

    private String taskId;

    @Override
    public DispatchTaskEnum getType() {
        return DispatchTaskEnum.VALIDATE_SYNC_MATCH_TABLE;
    }

    @Override
    public String getUniqueId() {
        return taskId;
    }

    @Override
    public void execute() {
        ValidateSyncTask task = validateSyncProfile.get(taskId);
        if (task == null) {
            logger.warn("ValidateSyncTask not found, skip match table, taskId={}", taskId);
            return;
        }
        matchSimilarTableGroups(task);
    }

    /**
     * Batch writing of similar map byaddTableGroupBatch Just once.
     */
    private void matchSimilarTableGroups(ValidateSyncTask validateSyncTask) {
        List<Table> sourceTables = validateSyncTask.getSourceTable();
        List<Table> targetTables = validateSyncTask.getTargetTable();
        if (CollectionUtils.isEmpty(sourceTables) || CollectionUtils.isEmpty(targetTables)) {
            logger.warn("Source library or target library table list empty, skip matching table, tabskId={}", taskId);
            return;
        }
        Map<String, Table> targetTableMap = new LinkedHashMap<>();
        for (Table table : targetTables) {
            if (table == null || StringUtil.isBlank(table.getName())) {
                continue;
            }
            targetTableMap.putIfAbsent(table.getName().toUpperCase(Locale.ROOT), table);
        }

        List<String> sourceNames = new ArrayList<>();
        List<String> targetNames = new ArrayList<>();
        for (Table sourceTable : sourceTables) {
            if (sourceTable == null || StringUtil.isBlank(sourceTable.getName())) {
                continue;
            }
            Table targetTable = targetTableMap.get(sourceTable.getName().toUpperCase(Locale.ROOT));
            if (targetTable == null) {
                continue;
            }
            String targetType = targetTable.getType();
            if (StringUtil.isNotBlank(targetType) && !TableTypeEnum.isTable(targetType)) {
                continue;
            }
            sourceNames.add(sourceTable.getName());
            targetNames.add(targetTable.getName());
        }
        if (sourceNames.isEmpty()) {
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("taskId", validateSyncTask.getId());
        params.put("sourceTable", StringUtil.join(sourceNames, StringUtil.VERTICAL_LINE));
        params.put("targetTable", StringUtil.join(targetNames, StringUtil.VERTICAL_LINE));
        params.put("sourceType", TableTypeEnum.TABLE.getCode());
        params.put("targetType", TableTypeEnum.TABLE.getCode());
        validateSyncService.addTableGroupBatch(params);
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

}
