/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.impl;

import org.dbsyncer.biz.BizException;
import org.dbsyncer.biz.GroupSearchFilter;
import org.dbsyncer.biz.GroupService;
import org.dbsyncer.biz.RepeatedTableGroupException;
import org.dbsyncer.biz.ResourceAclService;
import org.dbsyncer.biz.TableGroupService;
import org.dbsyncer.biz.UserConfigService;
import org.dbsyncer.biz.ValidateSyncService;
import org.dbsyncer.biz.checker.impl.mapping.MappingChecker;
import org.dbsyncer.biz.checker.impl.tablegroup.ValidateSyncTableGroupChecker;
import org.dbsyncer.biz.enums.ResourceAclActionEnum;
import org.dbsyncer.biz.task.ValidateSyncMatchTableTask;
import org.dbsyncer.biz.vo.MappingListConnectorConfigVO;
import org.dbsyncer.biz.vo.MappingListConnectorVO;
import org.dbsyncer.biz.vo.ValidateSyncListVO;
import org.dbsyncer.biz.vo.ValidateSyncTaskVO;
import org.dbsyncer.common.dispatch.DispatchTaskService;
import org.dbsyncer.common.enums.CommonTaskStatusEnum;
import org.dbsyncer.common.enums.CommonTaskTriggerEnum;
import org.dbsyncer.common.enums.CommonTaskTypeEnum;
import org.dbsyncer.common.enums.YesNoEnum;
import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.common.model.Paging;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.common.util.NumberUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.connector.base.ConnectorFactory;
import org.dbsyncer.manager.impl.PreloadTemplate;
import org.dbsyncer.parser.ConnectorProfile;
import org.dbsyncer.parser.MappingProfile;
import org.dbsyncer.parser.TableGroupProfile;
import org.dbsyncer.parser.TaskDetailMetaProfile;
import org.dbsyncer.parser.TaskDetailProfile;
import org.dbsyncer.parser.TaskMetaProfile;
import org.dbsyncer.parser.TaskProfile;
import org.dbsyncer.parser.ValidateSyncProfile;
import org.dbsyncer.parser.enums.TaskDetailMetricEnum;
import org.dbsyncer.parser.enums.TaskDetailStatusEnum;
import org.dbsyncer.parser.model.Connector;
import org.dbsyncer.parser.model.Mapping;
import org.dbsyncer.parser.model.Meta;
import org.dbsyncer.parser.model.TableGroup;
import org.dbsyncer.parser.model.TaskDetailQuery;
import org.dbsyncer.parser.util.ConnectorInstanceUtil;
import org.dbsyncer.parser.util.ConnectorServiceContextUtil;
import org.dbsyncer.parser.util.PickerUtil;
import org.dbsyncer.sdk.connector.ConnectorInstance;
import org.dbsyncer.sdk.connector.DefaultConnectorServiceContext;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.dbsyncer.sdk.enums.TableTypeEnum;
import org.dbsyncer.sdk.model.CommonTaskSnapshot;
import org.dbsyncer.sdk.model.ConnectorConfig;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.model.Filter;
import org.dbsyncer.sdk.model.Table;
import org.dbsyncer.sdk.model.ValidateSyncTask;
import org.dbsyncer.sdk.spi.ClusterService;
import org.dbsyncer.sdk.spi.ValidateSyncDetailService;
import org.dbsyncer.sdk.util.TaskSnapshotUtil;
import org.dbsyncer.storage.impl.SnowflakeIdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public final class ValidateSyncServiceImpl extends BaseServiceImpl implements ValidateSyncService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    private ClusterService clusterService;

    @Resource
    private ValidateSyncDetailService validateSyncDetailService;

    @Resource
    private ValidateSyncProfile validateSyncProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private TaskDetailProfile taskDetailProfile;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private TableGroupService tableGroupService;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private PreloadTemplate preloadTemplate;

    @Resource
    private MappingChecker mappingChecker;

    @Resource
    private ValidateSyncTableGroupChecker validateSyncTableGroupChecker;

    @Resource
    private DispatchTaskService dispatchTaskService;

    @Resource
    private ValidateSyncMatchTableTask validateSyncMatchTableTask;

    @Resource
    private TaskProfile taskProfile;

    @Resource
    private TaskDetailMetaProfile taskDetailMetaProfile;

    @Resource
    private GroupService groupService;

    @Resource
    private ResourceAclService resourceAclService;

    /**
     * TasksTurn off the lock.
     */
    private static final Object LOCK = new Object();

    @Override
    public ValidateSyncTaskVO get(String id) {
        return convertTask2Vo(validateSyncProfile.get(id));
    }

    @Override
    public String add(Map<String, String> params) {
        ValidateSyncTask task = new ValidateSyncTask();
        checkTask(task, params);
        // Default checkRow data
        task.setEnableRowData(true);
        // 关联同步任务
        String mappingId = params.get("mappingId");
        if (StringUtil.isNotBlank(mappingId)) {
            Mapping mapping = mappingProfile.get(mappingId);
            Assert.notNull(mapping, "mapping is not exist");
            task.setSourceConnectorId(mapping.getSourceConnectorId());
            task.setSourceDatabase(mapping.getSourceDatabase());
            task.setSourceSchema(mapping.getSourceSchema());
            task.setSourceTable(deepCopy(mapping.getSourceTable()));
            task.setTargetConnectorId(mapping.getTargetConnectorId());
            task.setTargetDatabase(mapping.getTargetDatabase());
            task.setTargetSchema(mapping.getTargetSchema());
            task.setTargetTable(deepCopy(mapping.getTargetTable()));
            task.setReadNum(mapping.getReadNum());
            task.setBatchNum(mapping.getBatchNum());
            task.setThreadNum(mapping.getThreadNum());
            // CopyTable groupsList
            tableGroupProfile.pageScanTableGroups(mappingId, ConfigConstant.PAGE_SIZE, tableGroupAll -> {
                if (CollectionUtils.isEmpty(tableGroupAll)) {
                    return;
                }
                for (TableGroup tableGroup : tableGroupAll) {
                    if (tableGroup == null) {
                        continue;
                    }
                    TableGroup newTable = deepCopy(tableGroup);
                    newTable.setId(String.valueOf(snowflakeIdWorker.nextId()));
                    newTable.setTaskId(task.getId());
                    tableGroupProfile.addTableGroup(newTable);
                }
            });
            // MergeTasksMaleTotalField
            mergeTaskColumn(task);
            String id = validateSyncProfile.add(task);
            validateSyncProfile.syncTaskTableMetaDetails(id);
            preloadTemplate.reConnect(task);
            resourceAclService.grantOwner(params.get(UserConfigService.CURRENT_USER_NAME),
                    ConfigConstant.VALIDATE_SYNC, id);
            return id;
        } else {
            Assert.hasText(params.get("sourceConnectorId"), "Data source cannot be empty");
            Assert.hasText(params.get("targetConnectorId"), "Target source cannot be empty");
            task.setSourceConnectorId(params.get("sourceConnectorId"));
            task.setSourceDatabase(params.get("sourceDatabase"));
            task.setSourceSchema(params.get("sourceSchema"));
            task.setTargetConnectorId(params.get("targetConnectorId"));
            task.setTargetDatabase(params.get("targetDatabase"));
            task.setTargetSchema(params.get("targetSchema"));
            // FirstEnduringOnce again, we can pull all the watches.
            String id = validateSyncProfile.add(task);
            preloadTemplate.reConnect(task);
            ValidateSyncTask validateSyncTask = refreshTablesAndGet(id);
            // Check「Match similar tables」It's an automatic match between steps.NoParsingCustom tableMap text
            if (StringUtil.isNotBlank(params.get("autoMatchTable"))) {
                List<Table> sourceTables = validateSyncTask.getSourceTable();
                List<Table> targetTables = validateSyncTask.getTargetTable();
                if (CollectionUtils.isEmpty(sourceTables) || CollectionUtils.isEmpty(targetTables)) {
                    throw new BizException("No source library or target library table list obtained, matching similar table not possible");
                }
                // At the end of the walk-in.sync；This is foreclosed.minTable, avoid 29.
                taskDetailMetaProfile.clearData(id);
                submitValidateSyncMatchTableTask(id);
            } else {
                String tableGroups = params.get("tableGroups");
                if (StringUtil.isNotBlank(tableGroups)) {
                    matchCustomizedTableGroups(validateSyncTask, tableGroups);
                }
                // sync Internal meetingsTRUNCATE One time and rebuild the skeleton, no advance.clearData
                validateSyncProfile.syncTaskTableMetaDetails(id);
            }
            resourceAclService.grantOwner(params.get(UserConfigService.CURRENT_USER_NAME),
                    ConfigConstant.VALIDATE_SYNC, id);
            return id;
        }

    }

    private List<Table> deepCopy(List<Table> targetTable) {
        return JsonUtil.jsonToArray(JsonUtil.objToJson(targetTable), Table.class);
    }

    private TableGroup deepCopy(TableGroup tableGroup) {
        return JsonUtil.jsonToObj(JsonUtil.objToJson(tableGroup), TableGroup.class);
    }

    /**
     * SubmitA walk.Match similar tablesTasks
     */
    private void submitValidateSyncMatchTableTask(String taskId) {
        ValidateSyncMatchTableTask task = (ValidateSyncMatchTableTask) validateSyncMatchTableTask.clone();
        task.setTaskId(taskId);
        dispatchTaskService.execute(task);
    }

    /**
     * CustomConfigureMap relation（and MappingServiceImpl Text format is consistent).
     * BatchdownLibrary，Not by Tablesync；Lined by the caller.
     */
    private void matchCustomizedTableGroups(ValidateSyncTask validateSyncTask, String tableGroups) {
        List<Table> sourceTables = validateSyncTask.getSourceTable();
        List<Table> targetTables = validateSyncTask.getTargetTable();
        if (CollectionUtils.isEmpty(sourceTables) || CollectionUtils.isEmpty(targetTables)) {
            throw new BizException("Could not close temporary folder: %s");
        }
        Map<String, Table> sourceTableMap = toTableNameMap(sourceTables);
        Map<String, Table> targetTableMap = toTableNameMap(targetTables);
        String[] lines = StringUtil.split(tableGroups, StringUtil.BREAK_LINE);
        assertRunning(validateSyncTask.getId());
        synchronized (LOCK) {
            int baseIndex = tableGroupProfile.getTableGroupCount(validateSyncTask.getId());
            List<TableGroup> models = new ArrayList<>();
            // Source table|Target table=Source fieldsA1*|Target fieldsA2*
            for (String line : lines) {
                if (StringUtil.isBlank(line)) {
                    continue;
                }
                String[] tableGroup = StringUtil.split(line, StringUtil.EQUAL);
                String[] tableGroupNames = StringUtil.split(tableGroup[0], StringUtil.VERTICAL_LINE);
                if (tableGroupNames.length != 2) {
                    continue;
                }
                Table sourceTable = sourceTableMap.get(tableGroupNames[0].toUpperCase(Locale.ROOT));
                Table targetTable = targetTableMap.get(tableGroupNames[1].toUpperCase(Locale.ROOT));
                if (sourceTable == null || targetTable == null) {
                    logger.warn("Custom table map not found: { > {", tableGroupNames[0], tableGroupNames[1]);
                    continue;
                }
                try {
                    Map<String, String> params = new HashMap<>();
                    params.put("taskId", validateSyncTask.getId());
                    params.put("sourceTable", sourceTable.getName());
                    params.put("targetTable", targetTable.getName());
                    params.put("sourceType", StringUtil.isNotBlank(sourceTable.getType()) ? sourceTable.getType() : TableTypeEnum.TABLE.getCode());
                    params.put("targetType", StringUtil.isNotBlank(targetTable.getType()) ? targetTable.getType() : TableTypeEnum.TABLE.getCode());
                    if (tableGroup.length == 2 && StringUtil.isNotBlank(tableGroup[1])) {
                        params.put("fieldMappings", tableGroup[1]);
                    }
                    TableGroup model = (TableGroup) validateSyncTableGroupChecker.checkAddConfigModel(params);
                    validateSyncTableGroupChecker.mergeConfig(validateSyncTask, model);
                    model.setIndex(baseIndex + models.size() + 1);
                    models.add(model);
                } catch (RepeatedTableGroupException e) {
                    // Existing Skip
                } catch (Exception e) {
                    logger.error("Add chart map failed: {, {", sourceTable.getName(), targetTable.getName(), e.getMessage());
                }
            }
            if (!CollectionUtils.isEmpty(models)) {
                tableGroupProfile.addTableGroupBatch(models);
                mergeTaskColumn(validateSyncTask);
            }
        }
    }

    private Map<String, Table> toTableNameMap(List<Table> tables) {
        Map<String, Table> map = new LinkedHashMap<>();
        for (Table table : tables) {
            if (table == null || StringUtil.isBlank(table.getName())) {
                continue;
            }
            map.putIfAbsent(table.getName().toUpperCase(Locale.ROOT), table);
        }
        return map;
    }

    @Override
    public String edit(Map<String, String> params) {
        ValidateSyncTask task = validateSyncProfile.get(params.get("id"));
        if (task == null) {
            throw new BizException("Task does not exist");
        }
        assertRunning(task.getId());
        checkTask(task, params);
        taskMetaProfile.removeMeta(task.getId());
        String sortedIds = params.get("sortedTableGroupIds");
        if (StringUtil.isNotBlank(sortedIds)) {
            List<TableGroup> groupAll = new ArrayList<>();
            tableGroupProfile.pageScanTableGroups(task.getId(), ConfigConstant.PAGE_SIZE, groupAll::addAll);
            if (!CollectionUtils.isEmpty(groupAll)) {
                mappingChecker.sortTableGroup(groupAll, params);
                for (TableGroup g : groupAll) {
                    validateSyncTableGroupChecker.mergeConfig(task, g);
                    tableGroupProfile.editTableGroup(g);
                }
            }
        } else {
            tableGroupProfile.pageScanTableGroups(task.getId(), ConfigConstant.PAGE_SIZE, page -> {
                if (CollectionUtils.isEmpty(page)) {
                    return;
                }
                for (TableGroup g : page) {
                    if (g == null) {
                        continue;
                    }
                    validateSyncTableGroupChecker.mergeConfig(task, g);
                    tableGroupProfile.editTableGroup(g);
                }
            });
        }
        String id = validateSyncProfile.update(task);
        // TableMeta zero; breakdownminTable bysync One-timeRevolutionTRUNCATE And rebuild the skeleton.clearRunData，NoThen I will.TRUNCATE (twice)
        taskProfile.resetTableDetailMetas(id);
        validateSyncProfile.syncTaskTableMetaDetails(id);
        return id;
    }

    @Override
    public String copy(String id, String username) {
        ValidateSyncTask task = validateSyncProfile.get(id);
        Assert.notNull(task, "Task not found");
        String json = JsonUtil.objToJson(task);
        ValidateSyncTask newTask = JsonUtil.jsonToObj(json, ValidateSyncTask.class);
        newTask.setId(String.valueOf(snowflakeIdWorker.nextId()));
        newTask.setName(newTask.getName() + "(Copy)");
        newTask.setType(CommonTaskTypeEnum.VALIDATE_SYNC.getCode());
        newTask.setUpdateTime(System.currentTimeMillis());
        String newId = validateSyncProfile.add(newTask);
        // Deep Copytable_group（Association sunk to this table)
        tableGroupProfile.pageScanTableGroups(id, ConfigConstant.PAGE_SIZE, sourceGroups -> {
            if (CollectionUtils.isEmpty(sourceGroups)) {
                return;
            }
            long now = System.currentTimeMillis();
            for (TableGroup source : sourceGroups) {
                if (source == null) {
                    continue;
                }
                TableGroup copy = JsonUtil.jsonToObj(JsonUtil.objToJson(source), TableGroup.class);
                if (copy == null) {
                    continue;
                }
                copy.setId(String.valueOf(snowflakeIdWorker.nextId()));
                copy.setTaskId(newId);
                copy.setCreateTime(now);
                copy.setUpdateTime(now);
                tableGroupProfile.addTableGroup(copy);
            }
        });
        preloadTemplate.reConnect(newTask);
        validateSyncProfile.syncTaskTableMetaDetails(newId);
        resourceAclService.grantOwner(username, ConfigConstant.VALIDATE_SYNC, newId);
        return newId;
    }

    @Override
    public String delete(String id) {
        assertRunning(id);

        validateSyncProfile.delete(id);
        groupService.bind(null, ConfigConstant.VALIDATE_SYNC, id);
        resourceAclService.revokeAllForResource(ConfigConstant.VALIDATE_SYNC, id);
        return "Deleted successfully";
    }

    @Override
    public String start(String id) {
        Assert.isTrue(tableGroupProfile.getTableGroupCount(id) > 0, "Task not configured map, not enabled");
        Assert.isTrue(!dispatchTaskService.isRunning(id), "Map is matching, please start later");
        ValidateSyncTask task = validateSyncProfile.get(id);
        Assert.notNull(task, "Task does not exist");
        synchronized (LOCK) {

            Meta meta = taskMetaProfile.getMeta(id);
            if (meta != null && CommonTaskStatusEnum.isDone(meta.getState())) {
                // Round run:TasksAdministrationMeta nil+ TableMeta nil+ DetailsminTableTRUNCATE One Families and Rebuild Bones
                taskMetaProfile.resetMeta(id);
                taskProfile.resetTableDetailMetas(id);
                validateSyncProfile.syncTaskTableMetaDetails(id);
            }
            clusterService.start(task, false);
            return "Started successfully";
        }
    }

    @Override
    public String stop(String id) {
        if (isRunning(id)) {
            Meta taskMeta = taskMetaProfile.getMeta(id);
            taskMeta.setState(CommonTaskStatusEnum.STOPPING.getCode());
            taskMeta.setUpdateTime(System.currentTimeMillis());
            taskMetaProfile.updateMeta(taskMeta);
        }
        clusterService.stop(id, CommonTaskTypeEnum.VALIDATE_SYNC);
        return "Stopped successfully";
    }

    @Override
    public Paging<ValidateSyncListVO> search(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String searchKey = params.get("searchKey");
        GroupSearchFilter groupFilter = groupService.resolveSearchFilter(params.get(ConfigConstant.GROUP_REL_GROUP_ID), ConfigConstant.VALIDATE_SYNC);
        Set<String> allowedIds = resourceAclService.resolveAllowedIds(
                params.get(UserConfigService.CURRENT_USER_NAME), ConfigConstant.VALIDATE_SYNC, ResourceAclActionEnum.READ.getCode());
        groupFilter = resourceAclService.applyToSearchFilter(groupFilter, allowedIds);
        if (groupFilter.isEmptyResult()) {
            return new Paging<>(pageNum, pageSize);
        }
        Paging<ValidateSyncTask> search = validateSyncProfile.query(pageNum, pageSize, searchKey, groupFilter.getIds());
        Collection<ValidateSyncTask> data = search.getData();
        if (CollectionUtils.isEmpty(data)) {
            return null;
        }
        List<ValidateSyncListVO> list = new ArrayList<>();
        data.forEach(task -> {
            ValidateSyncListVO vo = convertTask2ListVo(task);
            if (vo != null) {
                list.add(vo);
            }
        });
        Paging<ValidateSyncListVO> result = new Paging<>(search.getPageNum(), search.getPageSize());
        result.setTotal(search.getTotal());
        result.setData(list);
        return result;
    }

    @Override
    public Paging<TableGroup> searchTableGroup(Map<String, String> params) {
        String id = params.get("id");
        ValidateSyncTask task = validateSyncProfile.get(id);
        if (task == null) {
            return null;
        }
        // Reuse Checklist Group
        params.put("mappingId", task.getId());
        return tableGroupService.search(params);
    }

    @Override
    public Paging<Table> searchTables(Map<String, String> params) {
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        String type = params.get(ConfigConstant.CONFIG_MODEL_TYPE);
        String searchKey = params.get("searchKey");
        String tableNames = params.get("tableNames");

        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = Math.max(10, Math.min(200, NumberUtil.toInt(params.get("pageSize"), 50)));

        // 是否过滤已配置的表（exclude=1 表示不过滤）
        boolean excludeMapped = NumberUtil.toInt(params.get("exclude"), 0) != YesNoEnum.YES.getCode();

        ValidateSyncTask task = validateSyncProfile.get(id);
        Assert.notNull(task, "task not found.");

        boolean isSource = !"target".equals(type);
        List<Table> tables = isSource ? task.getSourceTable() : task.getTargetTable();
        tables = CollectionUtils.isEmpty(tables) ? Collections.emptyList() : tables;

        // Mapd/ConfigureYes.Table name
        Set<String> mappedTableNames;
        if (excludeMapped) {
            mappedTableNames = new HashSet<>();
            tableGroupProfile.pageScanTableGroups(id, ConfigConstant.PAGE_SIZE, page -> {
                if (CollectionUtils.isEmpty(page)) {
                    return;
                }
                for (TableGroup g : page) {
                    if (g == null) {
                        continue;
                    }
                    Table t = isSource ? g.getSourceTable() : g.getTargetTable();
                    if (t != null && StringUtil.isNotBlank(t.getName())) {
                        mappedTableNames.add(t.getName().toUpperCase(Locale.ROOT));
                    }
                }
            });
        } else {
            mappedTableNames = Collections.emptySet();
        }

        // Precision Match(s)tableNames: a|b|c）
        Set<String> exactNames = new HashSet<>();
        if (StringUtil.isNotBlank(tableNames)) {
            String[] nameArray = StringUtil.split(tableNames, "\\|");
            if (nameArray != null) {
                Arrays.stream(nameArray)
                        .filter(StringUtil::isNotBlank)
                        .map(n -> n.toUpperCase(Locale.ROOT))
                        .forEach(exactNames::add);
            }
        }

        String key = StringUtil.trimToEmpty(searchKey).toUpperCase(Locale.ROOT);

        List<Table> filtered = tables.stream()
                .filter(Objects::nonNull)
                .filter(t -> StringUtil.isNotBlank(t.getName()))
                .filter(t -> mappedTableNames.isEmpty() || !mappedTableNames.contains(t.getName().toUpperCase(Locale.ROOT)))
                .filter(t -> exactNames.isEmpty() || exactNames.contains(t.getName().toUpperCase(Locale.ROOT)))
                .filter(t -> key.isEmpty() || t.getName().toUpperCase(Locale.ROOT).contains(key))
                .sorted(Comparator.comparing(Table::getName))
                .collect(Collectors.toList());

        Paging<Table> paging = new Paging<>(pageNum, pageSize);
        paging.setTotal(filtered.size());
        int offset = (pageNum - 1) * pageSize;
        if (offset >= 0 && offset < filtered.size()) {
            paging.setData(filtered.stream()
                    .skip(offset)
                    .limit(pageSize)
                    .collect(Collectors.toList()));
        }
        return paging;
    }

    @Override
    public Object result(String id) {
        return validateSyncProfile.get(id);
    }

    @Override
    public Paging searchResult(Map<String, String> params) {
        String taskId = params.get("taskId");
        Assert.hasText(taskId, "Task ID cannot be empty");
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String detailStatus = StringUtil.trimToEmpty(params.get("detailStatus"));
        return taskDetailProfile.queryResults(TaskDetailQuery.of(taskId)
                .setPage(pageNum, pageSize)
                .setDetailStatus(TaskDetailStatusEnum.from(detailStatus))
                .setStatusMetric(TaskDetailMetricEnum.DIFF));
    }

    @Override
    public Object getValidateResultDetail(String taskId, String id) {
        Assert.hasText(taskId, "Task ID cannot be empty");
        Assert.hasText(id, "The details cannot be empty.");
        return taskDetailProfile.getDetail(TaskDetailQuery.of(taskId).setDetailId(id));
    }

    @Override
    public Object manualReviseDetail(String taskId, String detailId) {
        Assert.hasText(taskId, "Task ID cannot be empty");
        Assert.hasText(detailId, "The details cannot be empty.");
        return validateSyncDetailService.manualRevise(taskId, detailId);
    }

    @Override
    public String refreshTables(String id) {
        refreshTablesAndGet(id);
        return id;
    }

    /**
     * Rip & Back Source/Target tableList，BackAlreadyRefreshYes.Tasksobject (for matching immediately after creation).
     */
    private ValidateSyncTask refreshTablesAndGet(String id) {
        ValidateSyncTask task = validateSyncProfile.get(id);
        Assert.notNull(task, "The task id is invalid.");
        task.setSourceTable(updateConnectorTables(task, ConnectorInstanceUtil.SOURCE_SUFFIX));
        task.setTargetTable(updateConnectorTables(task, ConnectorInstanceUtil.TARGET_SUFFIX));
        validateSyncProfile.update(task);
        return task;
    }

    @Override
    public String refreshFields(String id) {
        TableGroup tableGroup = tableGroupProfile.getTableGroup(id);
        Assert.notNull(tableGroup, "Can not find tableGroup.");

        ValidateSyncTask task = validateSyncProfile.get(tableGroup.getTaskId());
        Assert.notNull(task, "The task id is invalid.");
        Table sourceTable = tableGroup.getSourceTable();
        Table targetTable = tableGroup.getTargetTable();
        List<String> sourceTablePks = sourceTable.getColumn().stream().filter(Field::isPk).map(Field::getName).collect(Collectors.toList());
        List<String> targetTablePks = targetTable.getColumn().stream().filter(Field::isPk).map(Field::getName).collect(Collectors.toList());
        validateSyncTableGroupChecker.updateTableColumn(task, ConnectorInstanceUtil.SOURCE_SUFFIX, StringUtil.join(sourceTablePks, ","), sourceTable);
        validateSyncTableGroupChecker.updateTableColumn(task, ConnectorInstanceUtil.TARGET_SUFFIX, StringUtil.join(targetTablePks, ","), targetTable);
        return validateSyncProfile.update(task);
    }

    @Override
    public String addTableGroup(Map<String, String> params) {
        String taskId = params.get("taskId");
        ValidateSyncTask task = validateSyncProfile.get(taskId);
        Assert.notNull(task, "task can not be null.");
        assertRunning(task.getId());
        synchronized (LOCK) {
            try {
                List<TableGroup> models = createAndPersistTableGroups(task, params, false);
                if (CollectionUtils.isEmpty(models)) {
                    return StringUtil.EMPTY;
                }
                mergeTaskColumn(task);
                return 1 < models.size() ? String.valueOf(models.size()) : models.get(0).getId();
            } finally {
                // Align details with map change
                validateSyncProfile.syncTaskTableMetaDetails(taskId);
            }
        }
    }

    @Override
    public void addTableGroupBatch(Map<String, String> params) {
        String taskId = params.get("taskId");
        ValidateSyncTask task = validateSyncProfile.get(taskId);
        Assert.notNull(task, "task can not be null.");
        assertRunning(task.getId());
        synchronized (LOCK) {
            List<TableGroup> models = createAndPersistTableGroups(task, params, true);
            if (CollectionUtils.isEmpty(models)) {
                return;
            }
            mergeTaskColumn(task);
            // Only once after the whole batch is finished
            validateSyncProfile.syncTaskTableMetaDetails(taskId);
        }
    }

    /**
     * Verify and BatchLibraryMap.
     *
     * @param skipRepeated true Repeatedly skips the table;false Time and again throw
     */
    private List<TableGroup> createAndPersistTableGroups(ValidateSyncTask task, Map<String, String> params, boolean skipRepeated) {
        String taskId = task.getId();
        String[] sourceTableArray = StringUtil.split(params.get("sourceTable"), StringUtil.VERTICAL_LINE);
        String[] targetTableArray = StringUtil.split(params.get("targetTable"), StringUtil.VERTICAL_LINE);
        int tableSize = sourceTableArray.length;
        Assert.isTrue(tableSize == targetTableArray.length, "Data source table and target source table relationships must be set");

        int baseIndex = tableGroupProfile.getTableGroupCount(taskId);
        List<TableGroup> models = new ArrayList<>(tableSize);
        for (int i = 0; i < tableSize; i++) {
            params.put("sourceTable", sourceTableArray[i]);
            params.put("targetTable", targetTableArray[i]);
            try {
                TableGroup model = (TableGroup) validateSyncTableGroupChecker.checkAddConfigModel(params);
                validateSyncTableGroupChecker.mergeConfig(task, model);
                model.setIndex(baseIndex + models.size() + 1);
                models.add(model);
            } catch (RepeatedTableGroupException e) {
                if (!skipRepeated) {
                    throw e;
                }
            }
        }
        if (!CollectionUtils.isEmpty(models)) {
            tableGroupProfile.addTableGroupBatch(models);
        }
        return models;
    }

    @Override
    public String editTableGroup(Map<String, String> params) {
        String tableGroupId = params.get(ConfigConstant.CONFIG_MODEL_ID);
        TableGroup tableGroup = tableGroupProfile.getTableGroup(tableGroupId);
        Assert.notNull(tableGroup, "Can not find tableGroup.");
        ValidateSyncTask task = validateSyncProfile.get(tableGroup.getTaskId());
        assertRunning(task.getId());

        TableGroup model = (TableGroup) validateSyncTableGroupChecker.checkEditConfigModel(params);
        validateSyncTableGroupChecker.mergeConfig(task, model);
        tableGroupProfile.editTableGroup(model);
        return tableGroupId;
    }

    @Override
    public String removeTableGroup(String taskId, String ids) {
        Assert.hasText(taskId, "Task id can not be null");
        Assert.hasText(ids, "TableGroup ids can not be null");
        ValidateSyncTask task = validateSyncProfile.get(taskId);
        assertRunning(taskId);
        // BatchDeleteTable
        Stream.of(StringUtil.split(ids, ",")).parallel().forEach(id -> {
            tableGroupProfile.removeTableGroup(id);
        });
        // MergeTasksMaleTotalField
        mergeTaskColumn(task);
        // ResetSort
        resetTableGroupAllIndex(taskId);
        // AlignmentDeleteAlreadyNoneDetails of the map
        validateSyncProfile.syncTaskTableMetaDetails(taskId);
        return taskId;
    }

    public List<Table> updateConnectorTables(ValidateSyncTask task, String suffix) {
        boolean isSource = StringUtil.equals(ConnectorInstanceUtil.SOURCE_SUFFIX, suffix);
        DefaultConnectorServiceContext context = ConnectorServiceContextUtil.buildConnectorServiceContext(task, isSource);
        String instanceId = ConnectorInstanceUtil.buildConnectorInstanceId(context.getMappingId(), context.getConnectorId(), context.getSuffix());
        ConnectorInstance connectorInstance = connectorFactory.connect(instanceId);
        List<Table> tables = connectorFactory.getTables(connectorInstance, context);
        // Ups 55/2
        Collections.sort(tables, Comparator.comparing(Table::getName));
        return tables;
    }

    private void resetTableGroupAllIndex(String taskId) {
        synchronized (LOCK) {
            List<String> orderedIds = new ArrayList<>();
            tableGroupProfile.pageScanTableGroups(taskId, ConfigConstant.PAGE_SIZE, page -> {
                for (TableGroup g : page) {
                    if (g != null && StringUtil.isNotBlank(g.getId())) {
                        orderedIds.add(g.getId());
                    }
                }
            });
            int i = orderedIds.size();
            for (String groupId : orderedIds) {
                TableGroup g = tableGroupProfile.getTableGroup(groupId);
                if (g == null) {
                    continue;
                }
                g.setIndex(i--);
                tableGroupProfile.editTableGroup(g);
            }
        }
    }

    private ValidateSyncTaskVO convertTask2Vo(ConfigModel task) {
        if (task == null) {
            return null;
        }
        ValidateSyncTask validateSyncTask = (ValidateSyncTask) task;
        Connector s = connectorProfile.getConnector(validateSyncTask.getSourceConnectorId());
        Connector t = connectorProfile.getConnector(validateSyncTask.getTargetConnectorId());
        ValidateSyncTaskVO vo = new ValidateSyncTaskVO(s, t);
        BeanUtils.copyProperties(task, vo);
        Meta taskMeta = taskMetaProfile.getMeta(validateSyncTask.getId());
        if (taskMeta != null) {
            vo.setMetaState(taskMeta.getState());
            vo.setStartTime(taskMeta.getStartTime() > 0 ? taskMeta.getStartTime() : null);
            vo.setUpdateTime(taskMeta.getUpdateTime() > 0 ? taskMeta.getUpdateTime() : null);
        }
        return vo;
    }

    /**
     * ListRow data
     */
    private ValidateSyncListVO convertTask2ListVo(ValidateSyncTask task) {
        if (task == null) {
            return null;
        }
        ValidateSyncListVO vo = new ValidateSyncListVO();
        vo.setId(task.getId());
        vo.setName(task.getName());
        vo.setTrigger(task.getTrigger());
        vo.setSourceConnector(toListConnector(connectorProfile.getConnector(task.getSourceConnectorId())));
        vo.setTargetConnector(toListConnector(connectorProfile.getConnector(task.getTargetConnectorId())));

        Meta taskMeta = taskMetaProfile.getMeta(task.getId());
        long errorCount = 0L;
        if (taskMeta != null && taskMeta.getDiff() != null) {
            errorCount = taskMeta.getDiff().get();
        }
        vo.setErrorCount(errorCount);
        int tableCount = tableGroupProfile.getTableGroupCount(task.getId());
        vo.setTotalTableCount(tableCount);
        boolean roundDone = taskMeta != null && CommonTaskStatusEnum.isDone(taskMeta.getState());
        List<CommonTaskSnapshot> tableSnapshots = collectValidateTableSnapshots(task.getId());
        vo.setCompletedTableCount(countCompletedTables(roundDone, tableCount, tableSnapshots));
        vo.setProgress(calculateProgressPercent(roundDone, tableCount, tableSnapshots));
        if (taskMeta != null) {
            vo.setMetaState(taskMeta.getState());
            vo.setStartTime(taskMeta.getStartTime() > 0 ? taskMeta.getStartTime() : null);
            vo.setUpdateTime(taskMeta.getUpdateTime() > 0 ? taskMeta.getUpdateTime() : null);
        }
        return vo;
    }

    private MappingListConnectorVO toListConnector(Connector connector) {
        MappingListConnectorVO vo = new MappingListConnectorVO();
        if (connector == null) {
            return vo;
        }
        vo.setName(connector.getName());
        ConnectorConfig config = connector.getConfig();
        if (config != null && StringUtil.isNotBlank(config.getConnectorType())) {
            vo.setConfig(new MappingListConnectorConfigVO(config.getConnectorType()));
        }
        return vo;
    }

    private void checkTask(ValidateSyncTask task, Map<String, String> params) {
        if (StringUtil.isBlank(task.getId())) {
            task.setId(String.valueOf(snowflakeIdWorker.nextId()));
            task.setType(CommonTaskTypeEnum.VALIDATE_SYNC.getCode());
        }
        long now = Instant.now().toEpochMilli();
        task.setCreateTime(null == task.getCreateTime() ? now : task.getCreateTime());
        task.setUpdateTime(now);
        task.setName(params.get("name"));
        // Filter Conditions
        String filterJson = params.get("filter");
        if (StringUtil.isNotBlank(filterJson)) {
            List<Filter> list = JsonUtil.jsonToArray(filterJson, Filter.class);
            task.setFilter(list);
        }

        String trigger = params.get("trigger");
        String cron = params.get("cron");
        if (StringUtil.isNotBlank(trigger)) {
            CommonTaskTriggerEnum type = CommonTaskTriggerEnum.getType(trigger);
            Assert.notNull(type, "trigger is not valid");
            task.setTrigger(type.getCode());
        }
        if (StringUtil.isNotBlank(cron)) {
            task.setCron(cron);
        }
        task.setEnableSync(StringUtil.isNotBlank(params.get("enableSync")));
        task.setEnableReverseScan(StringUtil.isNotBlank(params.get("enableReverseScan")));
        task.setEnableReverseSync(StringUtil.isNotBlank(params.get("enableReverseSync")));
        task.setEnableSchema(StringUtil.isNotBlank(params.get("enableSchema")));
        task.setEnableRowData(StringUtil.isNotBlank(params.get("enableRowData")));
        // Unified prefixCondition：Row dataNotOn，Scan backwards, sync backwardsAllDisable
        if (!task.isEnableRowData()) {
            task.setEnableReverseScan(false);
            task.setEnableReverseSync(false);
        } else if (!task.isEnableReverseScan()) {
            // Row dataOn，But a reverse scan.Close → SyncCloseReverse Revision
            task.setEnableReverseSync(false);
        }
        task.setEnableIndex(StringUtil.isNotBlank(params.get("enableIndex")));
        task.setEnableTrigger(StringUtil.isNotBlank(params.get("enableTrigger")));
        task.setEnableFunction(StringUtil.isNotBlank(params.get("enableFunction")));
        task.setReadNum(NumberUtil.toInt(params.get("readNum"), task.getReadNum()));
        task.setBatchNum(NumberUtil.toInt(params.get("batchNum"), task.getBatchNum()));
        task.setThreadNum(NumberUtil.toInt(params.get("threadNum"), task.getThreadNum()));
    }

    private void mergeTaskColumn(ValidateSyncTask task) {
        List<Field> sourceColumn = null;
        final List<Field>[] holder = new List[]{sourceColumn};
        tableGroupProfile.pageScanTableGroups(task.getId(), ConfigConstant.PAGE_SIZE, groups -> {
            for (TableGroup g : groups) {
                if (g == null || g.getSourceTable() == null || CollectionUtils.isEmpty(g.getSourceTable().getColumn())) {
                    continue;
                }
                holder[0] = PickerUtil.pickCommonFields(holder[0], g.getSourceTable().getColumn());
            }
        });
        task.setSourceColumn(holder[0]);
    }

    private List<CommonTaskSnapshot> collectValidateTableSnapshots(String taskId) {
        List<CommonTaskSnapshot> snapshots = new ArrayList<>();
        tableGroupProfile.pageScanTableGroups(taskId, ConfigConstant.PAGE_SIZE, groups -> {
            List<String> ids = groups.stream().map(TableGroup::getId).collect(Collectors.toList());
            Map<String, Meta> metaMap = taskMetaProfile.getDetailMetaMap(ids);
            for (String groupId : ids) {
                if (StringUtil.isBlank(groupId)) {
                    snapshots.add(null);
                    continue;
                }
                Meta meta = metaMap.get(groupId);
                snapshots.add(meta == null ? null : TaskSnapshotUtil.readTableSnapshot(meta.getSnapshot()));
            }

        });
        return snapshots;
    }

    /**
     * CompletedTable: DetailsMeta Quickshot.status=Completed The number.
     */
    private int countCompletedTables(boolean roundDone, int totalSize, List<CommonTaskSnapshot> tableSnapshots) {
        if (roundDone) {
            return totalSize;
        }
        if (CollectionUtils.isEmpty(tableSnapshots) || totalSize <= 0) {
            return 0;
        }
        long doneCount = tableSnapshots.stream()
                .filter(snapshot -> snapshot != null && CommonTaskStatusEnum.isDone(snapshot.getStatus()))
                .count();
        return (int) Math.min(doneCount, totalSize);
    }

    /**
     * Progress is 100%.minBy:completed / TableTotal * 100。
     */
    private BigDecimal calculateProgressPercent(boolean roundDone, int totalSize, List<CommonTaskSnapshot> tableSnapshots) {
        if (roundDone) {
            return new BigDecimal("100.00");
        }
        if (totalSize <= 0) {
            return BigDecimal.ZERO;
        }
        int completed = countCompletedTables(false, totalSize, tableSnapshots);
        return BigDecimal.valueOf(completed)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalSize), 2, RoundingMode.HALF_UP);
    }

}
