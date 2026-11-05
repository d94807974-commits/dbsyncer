/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.impl;

import org.dbsyncer.biz.BizException;
import org.dbsyncer.biz.DatabaseSyncService;
import org.dbsyncer.biz.GroupSearchFilter;
import org.dbsyncer.biz.GroupService;
import org.dbsyncer.biz.ResourceAclService;
import org.dbsyncer.biz.enums.ResourceAclActionEnum;
import org.dbsyncer.biz.UserConfigService;
import org.dbsyncer.biz.model.TableProgressBundle;
import org.dbsyncer.biz.vo.DatabaseMappingVO;
import org.dbsyncer.biz.vo.DatabaseSyncTaskVO;
import org.dbsyncer.biz.vo.TablePreviewVO;
import org.dbsyncer.common.enums.CommonTaskStatusEnum;
import org.dbsyncer.common.enums.CommonTaskTypeEnum;
import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.common.model.Paging;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.common.util.NumberUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.connector.base.ConnectorFactory;
import org.dbsyncer.parser.ConnectorProfile;
import org.dbsyncer.parser.DatabaseSyncProfile;
import org.dbsyncer.parser.ParserComponent;
import org.dbsyncer.parser.TableGroupProfile;
import org.dbsyncer.parser.TaskDetailProfile;
import org.dbsyncer.parser.TaskMetaProfile;
import org.dbsyncer.parser.TaskProfile;
import org.dbsyncer.parser.enums.TaskDetailMetricEnum;
import org.dbsyncer.parser.enums.TaskDetailStatusEnum;
import org.dbsyncer.parser.model.Connector;
import org.dbsyncer.parser.model.Meta;
import org.dbsyncer.parser.model.TableGroup;
import org.dbsyncer.parser.model.TaskDetailQuery;
import org.dbsyncer.parser.util.ConnectorInstanceUtil;
import org.dbsyncer.parser.util.ConnectorServiceContextUtil;
import org.dbsyncer.parser.util.DatabaseSyncMappingUtil;
import org.dbsyncer.sdk.connector.ConnectorInstance;
import org.dbsyncer.sdk.connector.DefaultConnectorServiceContext;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.dbsyncer.sdk.enums.TableTypeEnum;
import org.dbsyncer.sdk.model.CommonTaskSnapshot;
import org.dbsyncer.sdk.model.DatabaseMapping;
import org.dbsyncer.sdk.model.DatabaseSyncTableSnapshot;
import org.dbsyncer.sdk.model.DatabaseSyncTask;
import org.dbsyncer.sdk.model.MetaInfo;
import org.dbsyncer.sdk.model.Table;
import org.dbsyncer.sdk.model.TableMapping;
import org.dbsyncer.sdk.spi.ClusterService;
import org.dbsyncer.sdk.util.DatabaseSyncProgressUtil;
import org.dbsyncer.sdk.util.TaskSnapshotUtil;
import org.dbsyncer.storage.impl.SnowflakeIdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Database migrationOperational achievements
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-05-22 00:00
 */
@Service
public class DatabaseSyncServiceImpl extends BaseServiceImpl implements DatabaseSyncService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private DatabaseSyncProfile databaseSyncProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TaskProfile taskProfile;

    @Resource
    private TaskDetailProfile taskDetailProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    private ClusterService clusterService;

    @Resource
    private ParserComponent parserComponent;

    @Resource
    private GroupService groupService;

    @Resource
    private ResourceAclService resourceAclService;

    @Override
    public DatabaseSyncTaskVO get(String id) {
        DatabaseSyncTask task = databaseSyncProfile.get(id);
        Assert.notNull(task, "Task does not exist");
        return convertTask2Vo(task);
    }

    @Override
    public String add(Map<String, String> params) {
        String name = params.get("name");
        if (StringUtil.isBlank(name)) {
            throw new BizException("Task name cannot be empty");
        }
        List<DatabaseMappingVO> mappings = parseDatabaseMappings(params.get("databaseMappingsJson"));
        checkDatabaseMapping(mappings);
        if (CollectionUtils.isEmpty(mappings)) {
            throw new BizException("Please add at least one library map");
        }
        normalizeAndSortMappings(mappings);
        validateMappingConnectors(mappings);
        validateTableMappings(mappings);

        DatabaseSyncTask task = new DatabaseSyncTask();
        fillTaskOnAdd(task, params);
        task.setDatabaseMappings(toPersistMappings(mappings));
        // TasksWe need to drop first.Library（HamTasksAdministrationMeta）To presstaskId CompanyandWritetable_group；FailedAnd roll back in one piece.
        String taskId = databaseSyncProfile.add(task);
        try {
            tableGroupProfile.addTableGroupBatch(buildTableGroups(taskId, mappings));
        } catch (Exception e) {
            try {
                databaseSyncProfile.delete(taskId);
            } catch (Exception cleanupEx) {
                logger.error("Library Migration TaskCreateFailedBackRoll backDelete failed: id={}", taskId, cleanupEx);
            }
            throw new BizException(e.getMessage(), e);
        }
        // Preset DetailsminThis post is part of our special coverage Syria Protests 2011.DetailsPage JOIN Query does not dependTasksYesNoSynchronising folder
        databaseSyncProfile.syncTaskTableMetaDetails(taskId);
        resourceAclService.grantOwner(params.get(UserConfigService.CURRENT_USER_NAME),
                ConfigConstant.DATABASE_SYNC, taskId);
        logger.info("Library Migration TaskSaved: id={}, name={}, mappingCount={}", taskId, name, mappings.size());
        return taskId;
    }

    @Override
    public String edit(Map<String, String> params) {
        String id = params.get("id");
        Assert.hasText(id, "Tasks ID Can't be empty.");
        DatabaseSyncTask task = databaseSyncProfile.get(id);
        Assert.notNull(task, "Task does not exist");
        assertRunning(task.getId());
        List<DatabaseMappingVO> mappings = parseDatabaseMappings(params.get("databaseMappingsJson"));
        checkDatabaseMapping(mappings);
        if (CollectionUtils.isEmpty(mappings)) {
            throw new BizException("Please add at least one library map");
        }
        normalizeAndSortMappings(mappings);
        validateMappingConnectors(mappings);
        validateTableMappings(mappings);
        fillTaskOnEdit(task, params);
        task.setDatabaseMappings(toPersistMappings(mappings));
        // New mapping of precursorLibraryThis post is part of our special coverage Syria Protests 2011.FailedI'm not old.table_group
        List<TableGroup> newGroups = buildTableGroups(id, mappings);
        List<TableGroup> oldGroups = new ArrayList<>();
        tableGroupProfile.pageScanTableGroups(id, ConfigConstant.PAGE_SIZE, oldGroups::addAll);
        try {
            tableGroupProfile.removeTableGroupsByTaskId(id);
            tableGroupProfile.addTableGroupBatch(newGroups);
        } catch (Exception e) {
            restoreTableGroups(id, oldGroups);
            throw new BizException(e.getMessage(), e);
        }
        // MapdownLibrarySuccessThen clear and run.ResultandTasksAdministrationMeta，Avoid writingFailedI'll leave you with half a body.Tasks
        databaseSyncProfile.clearRunData(id);
        taskMetaProfile.resetMeta(id);
        String editedId = databaseSyncProfile.update(task);
        // EditYes.ClearRun the details and map the current scaleandOnTypeRematch
        databaseSyncProfile.syncTaskTableMetaDetails(editedId);
        return editedId;
    }

    private void checkDatabaseMapping(List<DatabaseMappingVO> mappings) {
        for (DatabaseMappingVO mapping : mappings) {
            if (StringUtil.equals(mapping.getSourceConnectorId(), mapping.getTargetConnectorId())) {
                boolean selectedDB = StringUtil.isNotBlank(mapping.getSourceDatabase()) && StringUtil.isNotBlank(mapping.getTargetDatabase());
                if (selectedDB && StringUtil.equals(mapping.getSourceDatabase(), mapping.getTargetDatabase())) {
                    throw new BizException("SameSourceSameLibraryOh, no.verifieSync，Please replace the target connection or database.！");
                }
                boolean selectedSchema = StringUtil.isNotBlank(mapping.getSourceSchema()) && StringUtil.isNotBlank(mapping.getTargetSchema());
                if (!selectedDB && selectedSchema && StringUtil.equals(mapping.getSourceSchema(), mapping.getTargetSchema())) {
                    throw new BizException("SameSourceSameschemaOh, no.verifieSync，Please replace the target connection orschema！");
                }

            }
        }
    }

    @Override
    public String delete(String id) {
        Assert.hasText(id, "Tasks ID Can't be empty.");
        assertRunning(id);
        databaseSyncProfile.delete(id);
        groupService.bind(null, ConfigConstant.DATABASE_SYNC, id);
        resourceAclService.revokeAllForResource(ConfigConstant.DATABASE_SYNC, id);
        return "Deleted successfully";
    }

    @Override
    public String start(String id) {
        Assert.hasText(id, "Tasks ID Can't be empty.");
        DatabaseSyncTask task = databaseSyncProfile.get(id);
        Assert.notNull(task, "Task does not exist");
        if (CollectionUtils.isEmpty(task.getDatabaseMappings())) {
            throw new BizException("TasksNotConfigureLibraryMap，NoneFrenchStart");
        }
        if (tableGroupProfile.getTableGroupCount(id) <= 0) {
            throw new BizException("TasksNotConfigureLibraryMap，NoneFrenchStart");
        }
        Meta meta = taskMetaProfile.getMeta(id);
        if (meta != null && CommonTaskStatusEnum.isDone(meta.getState())) {
            //ResetTasksmeta and Table Levelmeta
            taskMetaProfile.resetMeta(id);
            taskProfile.resetTableDetailMetas(id);
            //ResetDetailsminTable
            databaseSyncProfile.syncTaskTableMetaDetails(id);
        }
        clusterService.start(task, false);
        return "Started successfully";
    }

    @Override
    public String stop(String id) {
        Assert.hasText(id, "Tasks ID Can't be empty.");
        ConfigModel task = databaseSyncProfile.get(id);
        Assert.notNull(task, "Task does not exist");
        Meta taskMeta = taskMetaProfile.getMeta(id);
        if (taskMeta != null && CommonTaskStatusEnum.isRunning(taskMeta.getState())) {
            taskMeta.setState(CommonTaskStatusEnum.STOPPING.getCode());
            taskMeta.setUpdateTime(System.currentTimeMillis());
            taskMetaProfile.updateMeta(taskMeta);
        }
        clusterService.stop(id, CommonTaskTypeEnum.DATABASE_SYNC);
        return "Stopped successfully";
    }

    @Override
    public Paging<DatabaseSyncTaskVO> search(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String searchKey = params.get("searchKey");
        GroupSearchFilter groupFilter = groupService.resolveSearchFilter(params.get(ConfigConstant.GROUP_REL_GROUP_ID), ConfigConstant.DATABASE_SYNC);
        Set<String> allowedIds = resourceAclService.resolveAllowedIds(
                params.get(UserConfigService.CURRENT_USER_NAME), ConfigConstant.DATABASE_SYNC, ResourceAclActionEnum.READ.getCode());
        groupFilter = resourceAclService.applyToSearchFilter(groupFilter, allowedIds);
        if (groupFilter.isEmptyResult()) {
            return new Paging<>(pageNum, pageSize);
        }
        Paging<DatabaseSyncTask> paging = databaseSyncProfile.query(pageNum, pageSize, searchKey, groupFilter.getIds());
        Collection<DatabaseSyncTask> data = paging.getData();
        if (CollectionUtils.isEmpty(data)) {
            return null;
        }
        List<DatabaseSyncTaskVO> list = new ArrayList<>();
        data.forEach(item -> {
            DatabaseSyncTask task = item;
            DatabaseSyncTaskVO vo = convertTask2Vo(task);
            if (vo != null) {
                int tableCount = tableGroupProfile.getTableGroupCount(task.getId());
                Meta taskMeta = taskMetaProfile.getMeta(task.getId());
                boolean roundDone = taskMeta != null && DatabaseSyncProgressUtil.isRoundDone(taskMeta.getState());
                TableProgressBundle progressBundle = collectTableProgressBundle(task.getId());
                vo.setProgress(DatabaseSyncProgressUtil.calculateProgressPercent(
                        task, tableCount, roundDone,
                        progressBundle.getSnapshots(), progressBundle.getSyncedRowsPerTable(),
                        progressBundle.getSourceTotalPerTable()));
                vo.setTotalTableCount(tableCount);
                vo.setCompletedTableCount(DatabaseSyncProgressUtil.countCompletedTables(
                        task, tableCount, roundDone, progressBundle.getSnapshots()));
                vo.setSchemaCompletedCount(DatabaseSyncProgressUtil.countSchemaDoneTables(
                        progressBundle.getSnapshots()));
                vo.setSyncedRows(progressBundle.sumSyncedRows());
                vo.setSourceTotal(progressBundle.sumSourceTotal());
                vo.setErrorCount(0L);
                if (taskMeta != null) {
                    if (taskMeta.getFail() != null) {
                        vo.setErrorCount(taskMeta.getFail().get());
                    }
                    vo.setMetaState(taskMeta.getState());
                    vo.setStartTime(taskMeta.getStartTime() > 0 ? taskMeta.getStartTime() : null);
                    vo.setUpdateTime(taskMeta.getUpdateTime() > 0 ? taskMeta.getUpdateTime() : null);
                }
                list.add(vo);
            }
        });
        Paging<DatabaseSyncTaskVO> result = new Paging<>(paging.getPageNum(), paging.getPageSize());
        result.setTotal(paging.getTotal());
        result.setData(list);
        return result;
    }

    @Override
    public Paging searchResult(Map<String, String> params) {
        String taskId = params.get("taskId");
        Assert.hasText(taskId, "TasksIDCan't be empty.");
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String detailType = StringUtil.trimToEmpty(params.get("detailType"));
        String detailStatus = StringUtil.trimToEmpty(params.get("detailStatus"));
        return taskDetailProfile.queryResults(TaskDetailQuery.of(taskId)
                .setPage(pageNum, pageSize)
                .setDetailType(StringUtil.isBlank(detailType) ? null : detailType)
                .setDetailStatus(TaskDetailStatusEnum.from(detailStatus))
                .setStatusMetric(TaskDetailMetricEnum.FAIL));
    }

    @Override
    public TablePreviewVO previewTables(Map<String, String> params) {
        String connectorId = params.get("connectorId");
        String database = params.get("database");
        String schema = params.get("schema");
        String searchKey = StringUtil.trimToEmpty(params.get("searchKey"));
        int offset = Math.max(0, NumberUtil.toInt(params.get("offset"), 0));
        int limit = NumberUtil.toInt(params.get("limit"), 0);
        if (limit <= 0) {
            throw new BizException("limit I have to.Greater than 0");
        }
        if (StringUtil.isBlank(connectorId)) {
            throw new BizException("Connector cannot be empty");
        }
        Connector connector = connectorProfile.getConnector(connectorId);
        if (connector == null) {
            throw new BizException("Connector does not exist");
        }
        DefaultConnectorServiceContext context = ConnectorServiceContextUtil.buildConnectorServiceContext(
                "database-sync-preview",
                connectorId, database, schema,
                connectorId, database, schema,
                true);
        // By SelectedLibrary/schema ConstructionConnectChecklist ConceptualPostgreSQL Wait.NoneHoly shit.setCatalog Cut.Library，It has to be rebuilt.JDBC URL）
        String previewInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(
                "database-sync-preview", connectorId, database, schema, ConnectorInstanceUtil.SOURCE_SUFFIX);
        try {
            connectorFactory.connect(previewInstanceId, connector.getConfig(), database, schema);
            ConnectorInstance connectorInstance = connectorFactory.connect(previewInstanceId);
            List<Table> tables = connectorFactory.getTables(connectorInstance, context);
            if (CollectionUtils.isEmpty(tables)) {
                return TablePreviewVO.of(Collections.emptyList(), 0, String.valueOf(offset), limit);
            }
            if (StringUtil.isNotBlank(searchKey)) {
                String key = searchKey.toUpperCase();
                tables = tables.stream()
                        .filter(t -> t.getName() != null && t.getName().toUpperCase().contains(key))
                        .collect(Collectors.toList());
            }
            // Exclude when adding table mapAddSource table，AvoidminPageOff Reinforce error after exclusion and trigger a continuous front-end skyrocketPage
            Set<String> excludeTables = parseExcludeTables(params.get("excludeTablesJson"));
            if (!CollectionUtils.isEmpty(excludeTables)) {
                tables = tables.stream()
                        .filter(t -> t.getName() == null || !excludeTables.contains(t.getName()))
                        .collect(Collectors.toList());
            }
            tables.sort(Comparator.comparing(Table::getName, String.CASE_INSENSITIVE_ORDER));
            int realTotal = tables.size();
            Map<String, Integer> typeCounts = new HashMap<>(4);
            for (Table table : tables) {
                String type = table.getType() != null ? table.getType() : TableTypeEnum.TABLE.getCode();
                typeCounts.merge(type.toUpperCase(), 1, Integer::sum);
            }
            List<Table> pageRows = tables.stream().skip(offset).limit(limit).collect(Collectors.toList());
            TablePreviewVO result = TablePreviewVO.of(pageRows, realTotal, String.valueOf(offset), limit);
            result.setTypeCounts(typeCounts);
            return result;
        } finally {
            connectorFactory.disconnect(previewInstanceId);
        }
    }

    private List<DatabaseMappingVO> parseDatabaseMappings(String mappingsJson) {
        if (StringUtil.isBlank(mappingsJson)) {
            return Collections.emptyList();
        }
        List<DatabaseMappingVO> mappings = JsonUtil.jsonToArray(mappingsJson, DatabaseMappingVO.class);
        return mappings == null ? Collections.emptyList() : mappings;
    }

    /**
     * ParticipationVO Turntask.JSON EnduringLibraryMap (only)LibraryV.
     */
    private List<DatabaseMapping> toPersistMappings(List<DatabaseMappingVO> mappings) {
        if (CollectionUtils.isEmpty(mappings)) {
            return new ArrayList<>();
        }
        List<DatabaseMapping> result = new ArrayList<>(mappings.size());
        for (DatabaseMappingVO mapping : mappings) {
            if (mapping != null) {
                result.add(mapping.toDatabaseMapping());
            }
        }
        return result;
    }

    private Set<String> parseExcludeTables(String excludeTablesJson) {
        if (StringUtil.isBlank(excludeTablesJson)) {
            return Collections.emptySet();
        }
        List<String> names = JsonUtil.jsonToArray(excludeTablesJson, String.class);
        if (CollectionUtils.isEmpty(names)) {
            return Collections.emptySet();
        }
        Set<String> exclude = new HashSet<>();
        for (String name : names) {
            if (StringUtil.isNotBlank(name)) {
                exclude.add(name);
            }
        }
        return exclude;
    }

    /**
     * PressNo.Sorting from childhood to age and regulating continuityNo. 1..n，Easy.TasksImplementationandRestore.
     */
    private void normalizeAndSortMappings(List<DatabaseMappingVO> mappings) {
        if (CollectionUtils.isEmpty(mappings)) {
            return;
        }
        for (int i = 0; i < mappings.size(); i++) {
            DatabaseMappingVO mapping = mappings.get(i);
            if (mapping.getIndex() <= 0) {
                mapping.setIndex(i + 1);
            }
            List<TableMapping> tableMappings = mapping.getTableMappings();
            if (CollectionUtils.isEmpty(tableMappings)) {
                continue;
            }
            for (int j = 0; j < tableMappings.size(); j++) {
                TableMapping row = tableMappings.get(j);
                if (row.getIndex() <= 0) {
                    row.setIndex(j + 1);
                }
            }
            tableMappings.sort(Comparator.comparingInt(TableMapping::getIndex));
            for (int j = 0; j < tableMappings.size(); j++) {
                tableMappings.get(j).setIndex(j + 1);
            }
        }
        mappings.sort(Comparator.comparingInt(DatabaseMappingVO::getIndex));
        for (int i = 0; i < mappings.size(); i++) {
            mappings.get(i).setIndex(i + 1);
        }
    }

    private void fillTaskOnAdd(DatabaseSyncTask task, Map<String, String> params) {
        if (StringUtil.isBlank(task.getId())) {
            task.setId(String.valueOf(snowflakeIdWorker.nextId()));
            task.setType(CommonTaskTypeEnum.DATABASE_SYNC.getCode());
        }
        long now = Instant.now().toEpochMilli();
        task.setCreateTime(task.getCreateTime() == null ? now : task.getCreateTime());
        task.setUpdateTime(now);
        task.setName(params.get("name"));
        fillSyncStrategy(task, params);
    }

    private void fillTaskOnEdit(DatabaseSyncTask task, Map<String, String> params) {
        String name = params.get("name");
        if (StringUtil.isBlank(name)) {
            throw new BizException("Task name cannot be empty");
        }
        task.setName(name);
        task.setUpdateTime(Instant.now().toEpochMilli());
        fillSyncStrategy(task, params);
    }

    private void fillSyncStrategy(DatabaseSyncTask task, Map<String, String> params) {
        task.setEnableCopySchema(StringUtil.isNotBlank(params.get("enableCopySchema")));
        task.setEnableCopyData(StringUtil.isNotBlank(params.get("enableCopyData")));
        task.setOverwriteSchema(task.isEnableCopySchema() && StringUtil.isNotBlank(params.get("overwriteSchema")));
        task.setOverwriteData(task.isEnableCopyData() && StringUtil.isNotBlank(params.get("overwriteData")));
    }

    /**
     * EditFailedTimebacktable_group，AvoidTasksNoneMap.
     * Run HistoryminThe table is mapping.SuccessThis is to be Wider.
     */
    private void restoreTableGroups(String taskId, List<TableGroup> oldGroups) {
        try {
            tableGroupProfile.removeTableGroupsByTaskId(taskId);
        } catch (Exception cleanupEx) {
            logger.error("EditFailedRoll back：Clear the residue. table_group Failed: taskId={}", taskId, cleanupEx);
        }
        if (CollectionUtils.isEmpty(oldGroups)) {
            return;
        }
        try {
            tableGroupProfile.addTableGroupBatch(oldGroups);
        } catch (Exception restoreEx) {
            logger.error("EditFailedRoll back：RestoreAppeal table_group Failed: taskId={}", taskId, restoreEx);
        }
    }

    private void validateMappingConnectors(List<? extends DatabaseMapping> mappings) {
        for (int i = 0; i < mappings.size(); i++) {
            DatabaseMapping mapping = mappings.get(i);
            if (StringUtil.isBlank(mapping.getSourceConnectorId())) {
                throw new BizException("LibraryMap " + (i + 1) + " MissingSourceConnector");
            }
            if (StringUtil.isBlank(mapping.getTargetConnectorId())) {
                throw new BizException("LibraryMap " + (i + 1) + " MissingObjective CombinedEndConnector");
            }
        }
    }

    /**
     * TableMapTarget Table NameCan not open messageadd/edit Symmetrical verification)
     */
    private void validateTableMappings(List<DatabaseMappingVO> mappings) {
        if (CollectionUtils.isEmpty(mappings)) {
            return;
        }
        for (int i = 0; i < mappings.size(); i++) {
            List<TableMapping> tableMappings = mappings.get(i).getTableMappings();
            if (CollectionUtils.isEmpty(tableMappings)) {
                continue;
            }
            for (TableMapping row : tableMappings) {
                if (row == null) {
                    continue;
                }
                if (StringUtil.isBlank(row.getTargetTable())) {
                    String source = StringUtil.isNotBlank(row.getSourceTable()) ? row.getSourceTable() : "";
                    throw new BizException(String.format("LibraryMap %d：Source table「%s」The target list name cannot be empty", i + 1, source));
                }
            }
        }
    }

    private DatabaseSyncTaskVO convertTask2Vo(DatabaseSyncTask task) {
        if (task == null) {
            return null;
        }
        List<TableGroup> tableGroups = new ArrayList<>();
        tableGroupProfile.pageScanTableGroups(task.getId(), ConfigConstant.PAGE_SIZE, tableGroups::addAll);
        List<DatabaseMappingVO> mappingViews = buildDatabaseMappingVo(
                DatabaseSyncMappingUtil.sortByIndex(task.getDatabaseMappings()), tableGroups);
        DatabaseMappingVO first = CollectionUtils.isEmpty(mappingViews) ? null : mappingViews.get(0);
        Connector source = first == null ? null : connectorProfile.getConnector(first.getSourceConnectorId());
        Connector target = first == null ? null : connectorProfile.getConnector(first.getTargetConnectorId());
        DatabaseSyncTaskVO vo = new DatabaseSyncTaskVO(source, target);
        BeanUtils.copyProperties(task, vo);
        // Overwrite BeanUtils Write OnlyLibraryWide map, hang up.table_group Map statementEditPage
        vo.setMappingViews(mappingViews);
        vo.setMappingCount(CollectionUtils.isEmpty(mappingViews) ? 0 : mappingViews.size());
        return vo;
    }

    /**
     * Summarize each showing a fine snapshot.andLine Changes (in the same order)ListProgress is calculated by weighting the table.
     */
    private TableProgressBundle collectTableProgressBundle(String taskId) {
        TableProgressBundle bundle = new TableProgressBundle();
        Map<String, Long> sourceTotalById = new HashMap<>();
        tableGroupProfile.pageScanTableGroups(taskId, ConfigConstant.PAGE_SIZE, page -> {
            if (CollectionUtils.isEmpty(page)) {
                return;
            }
            List<String> ids = page.stream().map(TableGroup::getId).collect(Collectors.toList());
            Map<String, Meta> metaMap = taskMetaProfile.getDetailMetaMap(ids);
            for (TableGroup tableGroup : page) {
                if (tableGroup == null || StringUtil.isBlank(tableGroup.getId())) {
                    continue;
                }
                if (tableGroup.getSourceTotal() > 0) {
                    sourceTotalById.put(tableGroup.getId(), tableGroup.getSourceTotal());
                }
                if (StringUtil.isBlank(tableGroup.getId())) {
                    bundle.addTable(null, 0L, 0L);
                    continue;
                }
                Meta meta = metaMap == null ? null : metaMap.get(tableGroup.getId());
                CommonTaskSnapshot snapshot = meta == null ? null : TaskSnapshotUtil.readTableSnapshot(meta.getSnapshot());
                long fromMeta = counterValue(meta == null ? null : meta.getSuccess())
                        + counterValue(meta == null ? null : meta.getFail());
                long fromSnap = 0L;
                if (snapshot instanceof DatabaseSyncTableSnapshot) {
                    DatabaseSyncTableSnapshot dts = (DatabaseSyncTableSnapshot) snapshot;
                    fromSnap = Math.max(0L, dts.getSuccessTotal()) + Math.max(0L, dts.getFailTotal());
                }
                long synced = Math.max(fromMeta, fromSnap);
                Long sourceTotal = sourceTotalById.get(tableGroup.getId());
                bundle.addTable(snapshot, synced, sourceTotal == null ? 0L : sourceTotal);
            }
        });
        return bundle;
    }

    private static long counterValue(java.util.concurrent.atomic.AtomicLong counter) {
        return counter == null ? 0L : Math.max(0L, counter.get());
    }

    /**
     * Willtable_group PressLibraryHang toLibrary Map VO Yes.tableMappings（EditPageRetrospect.
     */
    private List<DatabaseMappingVO> buildDatabaseMappingVo(List<DatabaseMapping> mappings, List<TableGroup> tableGroups) {
        if (CollectionUtils.isEmpty(mappings)) {
            return new ArrayList<>();
        }
        Map<String, List<TableGroup>> groupsByKey = new HashMap<>();
        if (!CollectionUtils.isEmpty(tableGroups)) {
            for (TableGroup group : tableGroups) {
                if (group == null) {
                    continue;
                }
                groupsByKey.computeIfAbsent(group.buildDatabaseMappingKey(), k -> new ArrayList<>()).add(group);
            }
        }
        List<DatabaseMappingVO> result = new ArrayList<>(mappings.size());
        for (DatabaseMapping src : mappings) {
            if (src == null) {
                continue;
            }
            DatabaseMappingVO vo = DatabaseMappingVO.from(src);
            List<TableMapping> tableMappings = new ArrayList<>();
            List<TableGroup> groups = groupsByKey.getOrDefault(src.buildDatabaseMappingKey(), Collections.emptyList());
            groups.stream().sorted(Comparator.comparingInt(TableGroup::getIndex)).forEach(group -> {
                Table sourceTable = group.getSourceTable();
                Table targetTable = group.getTargetTable();
                if (sourceTable != null && targetTable != null) {
                    tableMappings.add(DatabaseSyncMappingUtil.toTableMapping(
                            sourceTable.getName(), targetTable.getName(), group.getIndex()));
                }
            });
            vo.setTableMappings(tableMappings);
            result.add(vo);
        }
        return result;
    }

    /**
     * PressLibraryMappingtable_group（CompanySourceRead Table Metadata)Library。
     */
    private List<TableGroup> buildTableGroups(String taskId, List<DatabaseMappingVO> mappings) {
        if (StringUtil.isBlank(taskId) || CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyList();
        }
        List<TableGroup> groups = new ArrayList<>();
        int sortIndex = 0;
        long now = Instant.now().toEpochMilli();
        for (DatabaseMappingVO mapping : mappings) {
            List<TableMapping> tableMappings = mapping.getSortedTableMappings();
            if (CollectionUtils.isEmpty(tableMappings)) {
                continue;
            }
            Connector sourceConnector = connectorProfile.getConnector(mapping.getSourceConnectorId());
            Assert.notNull(sourceConnector, "Source connector does not exist");
            Assert.notNull(sourceConnector.getConfig(), "Source Connection Configuration does not exist");
            // and ParserComponentImpl#getMetaInfo ExamplesIDConsistencymappingId + connectorId + suffix）
            connectorFactory.connect(
                    ConnectorInstanceUtil.buildConnectorInstanceId(taskId, mapping.getSourceConnectorId(), ConnectorInstanceUtil.SOURCE_SUFFIX),
                    sourceConnector.getConfig(),
                    mapping.getSourceDatabase(),
                    mapping.getSourceSchema()
            );

            List<String> sourceNames = tableMappings.stream().map(TableMapping::getSourceTable).collect(Collectors.toList());
            Map<String, Table> sourceMetaMap = loadMetaTableMap(taskId, mapping, sourceNames);

            for (TableMapping tableMapping : tableMappings) {
                Table sourceTable = sourceMetaMap.get(tableMapping.getSourceTable());
                if (sourceTable == null) {
                    throw new BizException(String.format("Source table [%s] Existing orNoneFrenchReadMetadata", tableMapping.getSourceTable()));
                }
                sortIndex++;
                TableGroup group = new TableGroup();
                group.setId(String.valueOf(snowflakeIdWorker.nextId()));
                group.setTaskId(taskId);
                group.setIndex(sortIndex);
                group.setSourceConnectorId(mapping.getSourceConnectorId());
                group.setTargetConnectorId(mapping.getTargetConnectorId());
                group.setSourceDatabase(StringUtil.getIfBlank(mapping.getSourceDatabase(), StringUtil.EMPTY));
                group.setTargetDatabase(StringUtil.getIfBlank(mapping.getTargetDatabase(), StringUtil.EMPTY));
                group.setSourceSchema(StringUtil.getIfBlank(mapping.getSourceSchema(), StringUtil.EMPTY));
                group.setTargetSchema(StringUtil.getIfBlank(mapping.getTargetSchema(), StringUtil.EMPTY));
                group.setSourceTable(sourceTable);
                Table targetTable = new Table();
                targetTable.setName(tableMapping.getTargetTable());
                targetTable.setType(TableTypeEnum.TABLE.getCode());
                group.setTargetTable(targetTable);
                group.setCreateTime(now);
                group.setUpdateTime(now);
                groups.add(group);
            }
        }
        return groups;
    }

    public Map<String, Table> loadMetaTableMap(String taskId, DatabaseMappingVO ctx, List<String> tableNames) {
        DefaultConnectorServiceContext context = ConnectorServiceContextUtil.buildConnectorServiceContext(
                taskId,
                ctx.getSourceConnectorId(),
                ctx.getSourceDatabase(),
                ctx.getSourceSchema(),
                ctx.getTargetConnectorId(),
                ctx.getTargetDatabase(),
                ctx.getTargetSchema(),
                true);
        tableNames.stream().distinct().forEach(context::addTablePattern);
        List<MetaInfo> metaInfos = parserComponent.getMetaInfo(context);
        Map<String, Table> tableMap = new HashMap<>(metaInfos.size());
        for (MetaInfo metaInfo : metaInfos) {
            if (metaInfo == null || StringUtil.isBlank(metaInfo.getTable())) {
                continue;
            }
            Table table = new Table();
            table.setName(metaInfo.getTable());
            table.setType(metaInfo.getTableType());
            table.setColumn(metaInfo.getColumn());
            tableMap.put(metaInfo.getTable(), table);
        }
        return tableMap;
    }

}