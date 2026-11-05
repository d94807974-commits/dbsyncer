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
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.RepeatedTableGroupException;
import org.realtimeetl.biz.TableGroupService;
import org.realtimeetl.biz.checker.impl.tablegroup.TableGroupChecker;
import org.realtimeetl.biz.task.TableGroupCountTask;
import org.realtimeetl.common.dispatch.DispatchTaskService;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.util.PickerUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.model.Field;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/11/27 23:14
 */
@Service
public class TableGroupServiceImpl extends BaseServiceImpl implements TableGroupService {

    @Resource
    private TableGroupChecker tableGroupChecker;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private DispatchTaskService dispatchTaskService;

    @Resource
    private TableGroupCountTask tableGroupCountTask;

    @Override
    public String add(Map<String, String> params) {
        String mappingId = params.get("mappingId");
        Mapping mapping = mappingProfile.get(mappingId);
        assertRunning(mapping.getId());

        synchronized (LOCK) {
            List<TableGroup> models = createAndPersistTableGroups(params, true);
            if (CollectionUtils.isEmpty(models)) {
                return StringUtil.EMPTY;
            }
            List<String> list = new ArrayList<>(models.size());
            for (TableGroup model : models) {
                list.add(model.getId());
            }
            submitTableGroupCountTask(mapping, list);
            mergeMappingColumn(mapping);
            return 1 < models.size() ? String.valueOf(models.size()) : list.get(0);
        }
    }

    @Override
    public int addWithoutCount(Map<String, String> params) {
        String mappingId = params.get("mappingId");
        Mapping mapping = mappingProfile.get(mappingId);
        Assert.notNull(mapping, "mapping can not be null.");
        assertRunning(mapping.getId());

        synchronized (LOCK) {
            List<TableGroup> models = createAndPersistTableGroups(params, false);
            if (CollectionUtils.isEmpty(models)) {
                return 0;
            }
            mergeMappingColumn(mapping);
            return models.size();
        }
    }

    @Override
    public int addWithoutCountBatch(List<Map<String, String>> paramsList) {
        if (CollectionUtils.isEmpty(paramsList)) {
            return 0;
        }
        String mappingId = paramsList.get(0).get("mappingId");
        Mapping mapping = mappingProfile.get(mappingId);
        Assert.notNull(mapping, "mapping can not be null.");
        assertRunning(mapping.getId());

        synchronized (LOCK) {
            int baseIndex = tableGroupProfile.getTableGroupCount(mappingId);
            List<TableGroup> models = new ArrayList<>(paramsList.size());
            for (Map<String, String> params : paramsList) {
                if (params == null || StringUtil.isBlank(params.get("sourceTable")) || StringUtil.isBlank(params.get("targetTable"))) {
                    continue;
                }
                Assert.isTrue(StringUtil.equals(mappingId, params.get("mappingId")), "I'd have to be consistent.");
                params.remove("skipRepeatedCheck");
                try {
                    TableGroup model = (TableGroup) tableGroupChecker.checkAddConfigModel(params);
                    model.setIndex(baseIndex + models.size() + 1);
                    models.add(model);
                } catch (RepeatedTableGroupException e) {
                    // CustomMap such scenes: exist and skip
                }
            }
            if (CollectionUtils.isEmpty(models)) {
                return 0;
            }
            tableGroupProfile.addTableGroupBatch(models);
            mergeMappingColumn(mapping);
            return models.size();
        }
    }

    /**
     * Verify and BatchLibraryMap.
     *
     * @param skipRepeatedCheck true Skip repeat check(s)PageBlanketInsert）；false Time and again skip that table
     */
    private List<TableGroup> createAndPersistTableGroups(Map<String, String> params, boolean skipRepeatedCheck) {
        String mappingId = params.get("mappingId");
        String[] sourceTableArray = StringUtil.split(params.get("sourceTable"), StringUtil.VERTICAL_LINE);
        String[] targetTableArray = StringUtil.split(params.get("targetTable"), StringUtil.VERTICAL_LINE);
        int tableSize = sourceTableArray.length;
        Assert.isTrue(tableSize == targetTableArray.length, "Data source table and target source table relationships must be set");

        int baseIndex = tableGroupProfile.getTableGroupCount(mappingId);
        if (skipRepeatedCheck) {
            params.put("skipRepeatedCheck", Boolean.TRUE.toString());
        } else {
            params.remove("skipRepeatedCheck");
        }
        List<TableGroup> models = new ArrayList<>(tableSize);
        for (int i = 0; i < tableSize; i++) {
            params.put("sourceTable", sourceTableArray[i]);
            params.put("targetTable", targetTableArray[i]);
            try {
                TableGroup model = (TableGroup) tableGroupChecker.checkAddConfigModel(params);
                model.setIndex(baseIndex + models.size() + 1);
                models.add(model);
            } catch (RepeatedTableGroupException e) {
                if (skipRepeatedCheck) {
                    throw e;
                }
                // Addition of scenes: Skip
            }
        }
        if (!CollectionUtils.isEmpty(models)) {
            tableGroupProfile.addTableGroupBatch(models);
        }
        return models;
    }

    @Override
    public String edit(Map<String, String> params) {
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        TableGroup tableGroup = tableGroupProfile.getTableGroup(id);
        Assert.notNull(tableGroup, "Can not find tableGroup.");
        Mapping mapping = mappingProfile.get(tableGroup.getTaskId());
        assertRunning(mapping.getId());

        TableGroup model = (TableGroup) tableGroupChecker.checkEditConfigModel(params);
        tableGroupProfile.editTableGroup(model);
        List<String> list = new ArrayList<>();
        list.add(model.getId());
        submitTableGroupCountTask(mapping, list);
        return id;
    }

    @Override
    public String refreshFields(String id) {
        TableGroup tableGroup = tableGroupProfile.getTableGroup(id);
        Assert.notNull(tableGroup, "Can not find tableGroup.");

        tableGroupChecker.refreshTableFields(tableGroup);
        return tableGroupProfile.editTableGroup(tableGroup);
    }

    @Override
    public String remove(String mappingId, String ids) {
        Assert.hasText(mappingId, "Mapping id can not be null");
        Assert.hasText(ids, "TableGroup ids can not be null");
        Mapping mapping = mappingProfile.get(mappingId);
        assertRunning(mapping.getId());

        // BatchDeleteTable
        Stream.of(StringUtil.split(ids, ",")).parallel().forEach(id -> {
            tableGroupProfile.removeTableGroup(id);
        });

        // MergeMappingMaleTotalField
        mergeMappingColumn(mapping);
        submitTableGroupCountTask(mapping, Collections.emptyList());

        // ResetSort
        resetTableGroupAllIndex(mappingId);
        return mappingId;
    }

    @Override
    public TableGroup getTableGroup(String id) {
        TableGroup tableGroup = tableGroupProfile.getTableGroup(id);
        Assert.notNull(tableGroup, "TableGroup can not be null");
        return tableGroup;
    }

    @Override
    public Paging<TableGroup> search(Map<String, String> params) {
        String mappingId = params.get("mappingId");
        Assert.hasText(mappingId, "Mapping id can not be null");
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        return tableGroupProfile.queryTableGroup(mappingId, params.get("searchKey"), pageNum, pageSize);
    }

    @Override
    public void resetMeta(Mapping mapping, String metaSnapshot) {
        Meta meta = taskMetaProfile.getMeta(mapping.getId());
        Assert.notNull(meta, "Synchronising task Meta does not exist.");
        meta.clear();
        if (StringUtil.isNotBlank(metaSnapshot)) {
            Map snapshot = JsonUtil.jsonToObj(metaSnapshot, HashMap.class);
            if (!CollectionUtils.isEmpty(snapshot)) {
                meta.setSnapshot(snapshot);
            }
        }
        meta.setUpdateTime(Instant.now().toEpochMilli());
        taskMetaProfile.updateMeta(meta);
    }

    @Override
    public Meta refreshMetaTotal(Mapping mapping, String metaSnapshot) {
        Meta meta = taskMetaProfile.getMeta(mapping.getId());
        Assert.notNull(meta, "Synchronising task Meta does not exist.");
        meta.clear();
        if (StringUtil.isNotBlank(metaSnapshot)) {
            Map snapshot = JsonUtil.jsonToObj(metaSnapshot, HashMap.class);
            if (!CollectionUtils.isEmpty(snapshot)) {
                meta.setSnapshot(snapshot);
            }
        }
        getMetaTotal(meta, mapping.getModel());
        meta.setUpdateTime(Instant.now().toEpochMilli());
        taskMetaProfile.updateMeta(meta);
        return meta;
    }

    private void getMetaTotal(Meta meta, String model) {
        // Full sync
        if (ModelEnum.isFull(model)) {
            // StatisticstableGroupTotal number of articles (priority)SOURCE_TOTAL Columns, compatible only insourceTable.count )
            AtomicLong count = new AtomicLong(0);
            tableGroupProfile.pageScanTableGroups(meta.getTaskId(), ConfigConstant.PAGE_SIZE, groupAll -> {
                for (TableGroup g : groupAll) {
                    if (g == null) {
                        continue;
                    }
                    count.getAndAdd(resolveSourceCount(g));
                }
            });
            meta.setTotal(count);
        }
    }

    private long resolveSourceCount(TableGroup group) {
        if (group.getSourceTotal() > 0) {
            return group.getSourceTotal();
        }
        if (group.getSourceTable() != null) {
            return group.getSourceTable().getCount();
        }
        return 0L;
    }

    private void resetTableGroupAllIndex(String mappingId) {
        synchronized (LOCK) {
            List<String> orderedIds = new ArrayList<>();
            tableGroupProfile.pageScanTableGroups(mappingId, ConfigConstant.PAGE_SIZE, page -> {
                for (TableGroup g : page) {
                    if (g != null && StringUtil.isNotBlank(g.getId())) {
                        orderedIds.add(g.getId());
                    }
                }
            });
            int i = orderedIds.size();
            for (String id : orderedIds) {
                TableGroup g = tableGroupProfile.getTableGroup(id);
                if (g == null) {
                    continue;
                }
                g.setIndex(i--);
                tableGroupProfile.editTableGroup(g);
            }
        }
    }

    private void mergeMappingColumn(Mapping mapping) {
        List<Field> sourceColumn = null;
        List<Field> targetColumn = null;
        final List<Field>[] holder = new List[]{sourceColumn, targetColumn};
        tableGroupProfile.pageScanTableGroups(mapping.getId(), ConfigConstant.PAGE_SIZE, groups -> {
            for (TableGroup g : groups) {
                holder[0] = PickerUtil.pickCommonFields(holder[0], g.getSourceTable().getColumn());
                holder[1] = PickerUtil.pickCommonFields(holder[1], g.getTargetTable().getColumn());
            }
        });
        mapping.setSourceColumn(holder[0]);
        mapping.setTargetColumn(holder[1]);
        mappingProfile.update(mapping);
    }

    /**
     * SubmitCount sync task tablesTasks
     */
    private void submitTableGroupCountTask(Mapping mapping, List<String> list) {
        TableGroupCountTask task = (TableGroupCountTask) tableGroupCountTask.clone();
        task.setMappingId(mapping.getId());
        task.setTableGroups(list);
        dispatchTaskService.execute(task);
    }

}