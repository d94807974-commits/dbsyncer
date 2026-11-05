/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.biz.impl;

import org.dbsyncer.biz.BizException;
import org.dbsyncer.biz.GroupSearchFilter;
import org.dbsyncer.biz.GroupService;
import org.dbsyncer.biz.MappingService;
import org.dbsyncer.biz.ResourceAclService;
import org.dbsyncer.biz.TableGroupService;
import org.dbsyncer.biz.UserConfigService;
import org.dbsyncer.biz.checker.impl.mapping.MappingChecker;
import org.dbsyncer.biz.enums.ResourceAclActionEnum;
import org.dbsyncer.biz.task.MappingCountTask;
import org.dbsyncer.biz.task.MappingMatchTableTask;
import org.dbsyncer.biz.vo.MappingCustomTableVO;
import org.dbsyncer.biz.vo.MappingListConnectorConfigVO;
import org.dbsyncer.biz.vo.MappingListConnectorVO;
import org.dbsyncer.biz.vo.MappingListMetaVO;
import org.dbsyncer.biz.vo.MappingListVO;
import org.dbsyncer.biz.vo.MappingVO;
import org.dbsyncer.biz.vo.MetaVO;
import org.dbsyncer.biz.vo.TableVO;
import org.dbsyncer.common.dispatch.DispatchTaskService;
import org.dbsyncer.common.enums.CommonTaskStatusEnum;
import org.dbsyncer.common.enums.CommonTaskTypeEnum;
import org.dbsyncer.common.model.Paging;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.common.util.NumberUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.connector.base.ConnectorFactory;
import org.dbsyncer.manager.impl.PreloadTemplate;
import org.dbsyncer.parser.ConnectorProfile;
import org.dbsyncer.parser.MappingProfile;
import org.dbsyncer.parser.NoticeRuleProfile;
import org.dbsyncer.parser.TableGroupContext;
import org.dbsyncer.parser.TableGroupProfile;
import org.dbsyncer.parser.TaskDetailMetaProfile;
import org.dbsyncer.parser.TaskMetaProfile;
import org.dbsyncer.parser.TaskProfile;
import org.dbsyncer.parser.enums.ParserEnum;
import org.dbsyncer.parser.model.Connector;
import org.dbsyncer.parser.model.Mapping;
import org.dbsyncer.parser.model.Meta;
import org.dbsyncer.parser.model.TableGroup;
import org.dbsyncer.parser.util.ConnectorInstanceUtil;
import org.dbsyncer.parser.util.ConnectorServiceContextUtil;
import org.dbsyncer.plugin.model.MappingStopContent;
import org.dbsyncer.sdk.connector.ConfigValidator;
import org.dbsyncer.sdk.connector.ConnectorInstance;
import org.dbsyncer.sdk.connector.DefaultConnectorServiceContext;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.dbsyncer.sdk.enums.ModelEnum;
import org.dbsyncer.sdk.enums.TableTypeEnum;
import org.dbsyncer.sdk.model.ConnectorConfig;
import org.dbsyncer.sdk.model.MetaInfo;
import org.dbsyncer.sdk.model.Table;
import org.dbsyncer.sdk.spi.ClusterService;
import org.dbsyncer.sdk.spi.ConnectorService;
import org.dbsyncer.storage.impl.SnowflakeIdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/17 23:20
 */
@Service
public class MappingServiceImpl extends BaseServiceImpl implements MappingService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MappingChecker mappingChecker;

    @Resource
    private TableGroupService tableGroupService;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TaskDetailMetaProfile taskDetailMetaProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private DispatchTaskService dispatchTaskService;

    @Resource
    private ClusterService clusterService;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private TableGroupContext tableGroupContext;

    @Resource
    private PreloadTemplate preloadTemplate;

    @Resource
    private MappingCountTask mappingCountTask;

    @Resource
    private MappingMatchTableTask mappingMatchTableTask;
    @Resource
    private TaskProfile taskProfile;

    @Resource
    private GroupService groupService;

    @Resource
    private ResourceAclService resourceAclService;

    @Resource
    private NoticeRuleProfile noticeRuleProfile;

    @Override
    public String add(Map<String, String> params) {
        Mapping model = mappingChecker.checkAddConfigModel(params);

        String id = mappingProfile.add(model);
        // LoadDriverTableEnduring Mapping，We need to retake it and then match it.
        refreshMappingTables(id);
        Mapping mapping = mappingProfile.get(id);
        resourceAclService.grantOwner(params.get(UserConfigService.CURRENT_USER_NAME),
                ConfigConstant.MAPPING, id);

        // Match similar tables / Custom tableMapLibraryReunify afterwardsMappingCountTask）
        if (StringUtil.isNotBlank(params.get("autoMatchTable"))) {
            submitMappingMatchTableTask(mapping, null);
            return id;
        }
        String tableGroups = params.get("tableGroups");
        if (StringUtil.isNotBlank(tableGroups)) {
            submitMappingMatchTableTask(mapping, tableGroups);
            return id;
        }
        // NoneShow still on mapSubmitStatisticstotal=0）
        submitMappingCountTask(mapping, null);
        return id;
    }

    @Override
    public String copy(String id, String username) {
        Mapping mapping = mappingProfile.get(id);
        Assert.notNull(mapping, "The mapping id is invalid.");

        String json = JsonUtil.objToJson(mapping);
        Mapping newMapping = JsonUtil.jsonToObj(json, Mapping.class);
        newMapping.setName(mapping.getName() + "(Copy)");
        newMapping.setId(String.valueOf(snowflakeIdWorker.nextId()));
        newMapping.setUpdateTime(Instant.now().toEpochMilli());
        mappingChecker.addMeta(newMapping);

        mappingProfile.add(newMapping);
        preloadTemplate.reConnect(newMapping);

        // CopyMap Table Relationships
        tableGroupProfile.pageScanTableGroups(mapping.getId(), ConfigConstant.PAGE_SIZE, groupList -> {
            if (CollectionUtils.isEmpty(groupList)) {
                return;
            }
            for (TableGroup tableGroup : groupList) {
                if (tableGroup == null) {
                    continue;
                }
                String tableGroupJson = JsonUtil.objToJson(tableGroup);
                TableGroup newTableGroup = JsonUtil.jsonToObj(tableGroupJson, TableGroup.class);
                newTableGroup.setId(String.valueOf(snowflakeIdWorker.nextId()));
                newTableGroup.setTaskId(newMapping.getId());
                tableGroupProfile.addTableGroup(newTableGroup);
            }
        });
        resourceAclService.grantOwner(username, ConfigConstant.MAPPING, newMapping.getId());
        return newMapping.getId();
    }

    @Override
    public String edit(Map<String, String> params) {
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        Mapping mapping = assertMappingExist(id);
        String metaSnapshot = params.get("metaSnapshot");
        synchronized (LOCK) {
            assertRunning(mapping.getId());
            Mapping model = mappingChecker.checkEditConfigModel(params);
            // After validation.ClearRunResult，Avoid verificationFailedIt's time to erase history.
            mappingProfile.clearRunData(id);
            mappingProfile.update(model);
            // Step check.Source database COUNT and summaryMeta.total
            submitMappingCountTask(mapping, metaSnapshot);
        }
        return id;
    }

    @Override
    public String remove(String id) {
        Mapping mapping = assertMappingExist(id);
        String taskId = mapping.getId();
        synchronized (LOCK) {
            assertRunning(taskId);
            // ConditionDelete table_group + DetailsMeta，And physics.DROP DetailsminTable (no empty tables recreated)
            mappingProfile.dropTaskDetailTable(id);
            tableGroupProfile.removeTableGroupsByTaskId(taskId);

            // DeleteTasksAdministrationmeta
            taskMetaProfile.removeMeta(taskId);

            // DeleteSyncMap relation
            tableGroupContext.clear(taskId);

            // ReleaseConnectJi.
            String sourceInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), mapping.getSourceConnectorId(), ConnectorInstanceUtil.SOURCE_SUFFIX);
            String targetInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), mapping.getTargetConnectorId(), ConnectorInstanceUtil.TARGET_SUFFIX);
            connectorFactory.disconnect(sourceInstanceId);
            connectorFactory.disconnect(targetInstanceId);

            // DeleteSync task
            mappingProfile.delete(id);
            groupService.bind(null, ConfigConstant.MAPPING, id);
            resourceAclService.revokeAllForResource(ConfigConstant.MAPPING, id);
            noticeRuleProfile.removeByScopeId(ConfigConstant.MAPPING, id);
        }
        return "Unemployment";
    }

    @Override
    public MappingVO getMapping(String id) {
        Mapping mapping = mappingProfile.get(id);
        return convertMapping2Vo(mapping);
    }

    @Override
    public MappingCustomTableVO getMappingCustomTable(String id, String type) {
        Mapping mapping = mappingProfile.get(id);
        MappingCustomTableVO vo = new MappingCustomTableVO();
        vo.setId(mapping.getId());
        vo.setName(mapping.getName());
        boolean isSource = StringUtil.equals("source", type);
        List<Table> tables = isSource ? mapping.getSourceTable() : mapping.getTargetTable();
        String connectorId = isSource ? mapping.getSourceConnectorId() : mapping.getTargetConnectorId();
        ConnectorConfig config = connectorProfile.getConnector(connectorId).getConfig();
        ConnectorService<?, ?> connectorService = connectorFactory.getConnectorService(config);
        vo.setConnectorConfig(config);
        vo.setExtendedType(connectorService.getExtendedTableType().getCode());

        // OnlyBackCustom tableType
        if (!CollectionUtils.isEmpty(tables)) {
            List<Table> mainTables = new ArrayList<>();
            List<TableVO> customTables = new ArrayList<>();
            tables.forEach(t -> {
                switch (TableTypeEnum.getTableType(t.getType())) {
                    case SQL:
                    case SEMI:
                        TableVO tableVO = new TableVO();
                        BeanUtils.copyProperties(t, tableVO);
                        customTables.add(tableVO);
                        break;
                    case TABLE:
                        mainTables.add(t);
                    default:
                        break;
                }
            });
            vo.setCustomTables(customTables.stream().sorted(Comparator.comparing(Table::getName)).collect(Collectors.toList()));
            vo.setMainTables(mainTables.stream().sorted(Comparator.comparing(Table::getName)).collect(Collectors.toList()));
        }
        // MetaInfo
        vo.setMeta(taskMetaProfile.getMeta(mapping.getId()));
        return vo;
    }

    @Override
    public Paging<MappingListVO> search(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String searchKey = params.get("searchKey");
        GroupSearchFilter groupFilter = groupService.resolveSearchFilter(params.get(ConfigConstant.GROUP_REL_GROUP_ID), ConfigConstant.MAPPING);
        Set<String> allowedIds = resourceAclService.resolveAllowedIds(
                params.get(UserConfigService.CURRENT_USER_NAME), ConfigConstant.MAPPING, ResourceAclActionEnum.READ.getCode());
        groupFilter = resourceAclService.applyToSearchFilter(groupFilter, allowedIds);
        Paging<MappingListVO> result = new Paging<>(pageNum, pageSize);
        if (groupFilter.isEmptyResult()) {
            return result;
        }
        Paging<Mapping> paging = mappingProfile.query(pageNum, pageSize, searchKey, groupFilter.getIds());
        if (paging == null) {
            return result;
        }
        result.setTotal(paging.getTotal());
        if (CollectionUtils.isEmpty(paging.getData())) {
            return result;
        }
        List<MappingListVO> rows = new ArrayList<>(paging.getData().size());

        List<String> taskIds = paging.getData().stream()
                .filter(Objects::nonNull)
                .map(Mapping::getId)
                .filter(StringUtil::isNotBlank)
                .collect(Collectors.toList());

        Map<String, Meta> taskMetaMap = taskMetaProfile.getTaskMetaMap(taskIds);

        for (Mapping mapping : paging.getData()) {
            if (mapping == null) {
                continue;
            }
            try {
                rows.add(convertMapping2ListVo(mapping, taskMetaMap.get(mapping.getId())));
            } catch (Exception e) {
                logger.error("Failed to convert sync tasklist line, Skipped.",
                        mapping.getId(), mapping.getName(), mapping.getId(), e.getMessage());
            }
        }
        result.setData(rows);
        return result;
    }

    @Override
    public Paging searchTableGroupResult(Map<String, String> params) {
        String mappingId = params.get(ConfigConstant.CONFIG_MODEL_ID);
        if (StringUtil.isBlank(mappingId)) {
            mappingId = params.get("id");
        }
        Assert.hasText(mappingId, "Synchronising task ID cannot be empty.");
        assertMappingExist(mappingId);

        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String detailStatus = params.get("detailStatus");
        return tableGroupProfile.queryTableGroupResults(mappingId, detailStatus, pageNum, pageSize);
    }

    @Override
    public String start(String id) {
        Mapping mapping = assertMappingExist(id);
        //Verify INTRODUCTIONMappingYesNoExistence
        assertTableGroupExist(id);
        final String taskId = mapping.getId();
        synchronized (LOCK) {
            assertRunning(taskId);
            Assert.isTrue(!dispatchTaskService.isRunning(taskId), "Synchronization of task map is matching or counting, please start later");
            // If it's...FinishThere.ResetStatus
            clearMetaIfFinished(mapping);
            // Tag           Running
            preloadTemplate.changeMetaState(taskId, CommonTaskStatusEnum.RUNNING);

            try {
                clusterService.start(mapping, false);
            } catch (Exception e) {
                // rollback
                preloadTemplate.changeMetaState(taskId, CommonTaskStatusEnum.READY);
                throw new BizException(e.getMessage());
            }

        }
        return "Synchronize job startup successful";
    }

    @Override
    public String stop(String id) {
        Mapping mapping = assertMappingExist(id);
        synchronized (LOCK) {
            if (!isRunning(mapping.getId())) {
                throw new BizException("Synchronising tasks stopped.");
            }
            preloadTemplate.changeMetaState(mapping.getId(), CommonTaskStatusEnum.STOPPING);
            clusterService.stop(mapping.getId(), CommonTaskTypeEnum.MAPPING);
            // SendCloseOrganisation
            MappingStopContent content = new MappingStopContent();
            content.setTitle("Stop Sync Task Manually");
            content.setName(mapping.getName());
            content.setModel(ModelEnum.getModelEnum(mapping.getModel()));
            content.setResourceType(ConfigConstant.MAPPING);
            content.setResourceId(mapping.getId());
            sendNotifyMessage(content);
        }
        return "Synchronising tasks stopped successful";
    }

    @Override
    public String refreshMappingTables(String id) {
        Mapping mapping = mappingProfile.get(id);
        Assert.notNull(mapping, "The mapping id is invalid.");
        mapping.setSourceTable(updateConnectorTables(mapping, ConnectorInstanceUtil.SOURCE_SUFFIX));
        mapping.setTargetTable(updateConnectorTables(mapping, ConnectorInstanceUtil.TARGET_SUFFIX));
        mappingProfile.update(mapping);
        return "Synchronising %s: successful";
    }

    @Override
    public Paging<Table> searchTables(Map<String, String> params) {
        // 1. ParameterAccess
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        String type = params.get(ConfigConstant.CONFIG_MODEL_TYPE);
        String searchKey = params.get("searchKey");
        String tableNames = params.get("tableNames");
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = Math.max(10, Math.min(200, NumberUtil.toInt(params.get("pageSize"), 50)));
        //YesNoFilter
        boolean excludeMapped = NumberUtil.toInt(params.get("exclude"), 0) != 1;

        // 2. Access to basic data
        Mapping mapping = assertMappingExist(id);
        boolean isSource = !"target".equals(type);
        List<Table> tables = getMappingTables(mapping, isSource);

        // 3. MapdTable namePool (to be excluded)
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
                        mappedTableNames.add(t.getName());
                    }
                }
            });
        } else {
            mappedTableNames = Collections.emptySet();
        }

        // 4. Accurate Match (Pression)Source Table NameBatch exact queriesTarget table）
        Set<String> exactNames = new HashSet<>();
        if (StringUtil.isNotBlank(tableNames)) {
            String[] nameArray = StringUtil.split(tableNames, "\\|");
            if (nameArray != null) {
                Arrays.stream(nameArray)
                        .filter(StringUtil::isNotBlank)
                        .map(String::toUpperCase)
                        .forEach(exactNames::add);
            }
        }

        // 4. Fluid Filter+ Search + Sort
        String key = StringUtil.isBlank(searchKey) ? StringUtil.EMPTY : searchKey.trim().toUpperCase();
        List<Table> filtered = tables.stream()
                .filter(Objects::nonNull)
                .filter(t -> StringUtil.isNotBlank(t.getName()))
                .filter(t -> !mappedTableNames.contains(t.getName()))
                .filter(t -> exactNames.isEmpty() || exactNames.contains(t.getName().toUpperCase()))
                .filter(t -> key.isEmpty() || t.getName().toUpperCase().contains(key))
                .sorted(Comparator.comparing(Table::getName))
                .collect(Collectors.toList());

        // 5. MemoryminPage
        Paging<Table> paging = new Paging<>(pageNum, pageSize);
        paging.setTotal(filtered.size());

        int offset = (pageNum - 1) * pageSize;
        paging.setData(filtered.stream()
                .skip(offset)
                .limit(pageSize)
                .collect(Collectors.toList()));

        return paging;
    }

    @Override
    public List<Table> searchCustomTable(Map<String, String> params) {
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        Mapping mapping = assertMappingExist(id);
        String type = params.get(ConfigConstant.CONFIG_MODEL_TYPE);
        String searchKey = params.get("searchKey");
        boolean isSource = StringUtil.equals("source", type);
        List<Table> tables = getMappingTables(mapping, isSource);
        if (!CollectionUtils.isEmpty(tables) && StringUtil.isNotBlank(searchKey)) {
            return tables.stream().filter(t -> t.getName().contains(searchKey)).collect(Collectors.toList());
        }
        return tables;
    }

    @Override
    public String saveCustomTable(Map<String, String> params) {
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        Mapping mapping = assertMappingExist(id);
        synchronized (LOCK) {
            assertRunning(mapping.getId());
            saveCustomTable(mapping, params);
            mappingProfile.update(mapping);
        }
        return id;
    }

    @Override
    public String removeCustomTable(Map<String, String> params) {
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        Mapping mapping = assertMappingExist(id);
        synchronized (LOCK) {
            assertRunning(mapping.getId());
            removeCustomTable(mapping, params);
            mappingProfile.update(mapping);
        }
        return id;
    }

    /**
     * SubmitStatistical comparisonTasksTotalTasks
     */
    private void submitMappingCountTask(Mapping mapping, String metaSnapshot) {
        if (ModelEnum.isFull(mapping.getModel())) {
            MappingCountTask task = (MappingCountTask) mappingCountTask.clone();
            task.setMappingId(mapping.getId());
            task.setMetaSnapshot(metaSnapshot);
            dispatchTaskService.execute(task);
        } else {
            // Restore onlyMeta
            tableGroupService.resetMeta(mapping, metaSnapshot);
        }
    }

    /**
     * SubmitStep 251Tasks（Similar table orCustom tableGroups；Continue after completionMappingCountTask）。
     *
     * @param tableGroups CustomMap text;null/Empty Representation Automatically Matches with Same Name Table
     */
    private void submitMappingMatchTableTask(Mapping mapping, String tableGroups) {
        MappingMatchTableTask task = (MappingMatchTableTask) mappingMatchTableTask.clone();
        task.setMappingId(mapping.getId());
        task.setTableGroups(tableGroups);
        dispatchTaskService.execute(task);
    }

    private List<Table> updateConnectorTables(Mapping mapping, String suffix) {
        boolean isSource = StringUtil.equals(ConnectorInstanceUtil.SOURCE_SUFFIX, suffix);
        DefaultConnectorServiceContext context = ConnectorServiceContextUtil.buildConnectorServiceContext(mapping, isSource);

        // MergeCustom table
        List<Table> customTables = new ArrayList<>();
        List<Table> tables = getMappingTables(mapping, isSource);
        tables.forEach(t -> {
            switch (TableTypeEnum.getTableType(t.getType())) {
                case SQL:
                case SEMI:
                    customTables.add(t);
                    break;
                default:
                    break;
            }
        });
        String instanceId = ConnectorInstanceUtil.buildConnectorInstanceId(context.getMappingId(), context.getConnectorId(), context.getSuffix());
        Connector connector = connectorProfile.getConnector(context.getConnectorId());
        ConnectorInstance connectorInstance = connectorFactory.connect(instanceId, connector.getConfig(),
                context.getCatalog(), context.getSchema());
        tables = connectorFactory.getTables(connectorInstance, context);
        tables.addAll(customTables);
        // Ups 55/2
        Collections.sort(tables, Comparator.comparing(Table::getName));
        return tables;
    }

    private MappingVO convertMapping2Vo(Mapping mapping) {
        Assert.notNull(mapping, "Mapping can not be null.");
        Meta resolved = resolveMappingMeta(mapping);
        Assert.notNull(resolved, String.format("Meta can not be null. mappingId:%s, taskId:%s",
                mapping.getId(), mapping.getId()));

        String model = mapping.getModel();
        MetaVO metaVo = new MetaVO(ModelEnum.getModelEnum(model).getName(), mapping.getName());
        BeanUtils.copyProperties(resolved, metaVo);
        metaVo.setCounting(dispatchTaskService.isRunning(mapping.getId()));

        Connector s = connectorProfile.getConnector(mapping.getSourceConnectorId());
        Connector t = connectorProfile.getConnector(mapping.getTargetConnectorId());
        MappingVO vo = new MappingVO(s, t, metaVo);
        BeanUtils.copyProperties(mapping, vo);
        // FullTableListBig, pull/SearchCome on.searchTables minPage；VO No returns
        vo.setSourceTable(null);
        vo.setTargetTable(null);
        return vo;
    }

    /**
     * ListLine: Only for assembly displayField。
     */
    private MappingListVO convertMapping2ListVo(Mapping mapping, Meta meta) {
        Assert.notNull(mapping, "Mapping can not be null.");
        Meta resolved = meta != null ? meta : resolveMappingMeta(mapping);
        Assert.notNull(resolved, String.format("Meta can not be null. mappingId:%s, taskId:%s",
                mapping.getId(), mapping.getId()));

        MappingListVO vo = new MappingListVO();
        vo.setId(mapping.getId());
        vo.setName(mapping.getName());
        vo.setModel(mapping.getModel());
        vo.setSourceConnector(toListConnector(connectorProfile.getConnector(mapping.getSourceConnectorId())));
        vo.setTargetConnector(toListConnector(connectorProfile.getConnector(mapping.getTargetConnectorId())));
        vo.setMeta(toListMeta(resolved, mapping.getId()));
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

    private MappingListMetaVO toListMeta(Meta meta, String mappingId) {
        MappingListMetaVO vo = new MappingListMetaVO();
        vo.setState(meta.getState());
        vo.setUpdateTime(meta.getUpdateTime());
        vo.setStartTime(meta.getStartTime());
        vo.setTotal(meta.getTotal().get());
        vo.setSuccess(meta.getSuccess().get());
        vo.setFail(meta.getFail().get());
        vo.setCounting(dispatchTaskService.isRunning(mappingId));
        vo.setSnapshot(slimListSnapshot(meta.getSnapshot()));
        return vo;
    }

    /**
     * ListPhase presentation requiredsnapshot keys;the remaining bitsInfoNo return.
     */
    private Map<String, String> slimListSnapshot(Map<String, String> snapshot) {
        if (CollectionUtils.isEmpty(snapshot)) {
            return Collections.emptyMap();
        }
        ParserEnum[] keys = {
                ParserEnum.FULL_INCREMENT_PHASE,
                ParserEnum.TABLE_GROUP_INDEX,
                ParserEnum.PAGE_INDEX,
                ParserEnum.CURSOR
        };
        Map<String, String> slim = new HashMap<>(keys.length);
        for (ParserEnum key : keys) {
            String value = snapshot.get(key.getCode());
            if (StringUtil.isNotBlank(value)) {
                slim.put(key.getCode(), value);
            }
        }
        return slim;
    }

    /**
     * AccessSync taskTasksAdministrationMeta
     */
    private Meta resolveMappingMeta(Mapping mapping) {
        Meta meta = taskMetaProfile.getMeta(mapping.getId());
        if (meta != null) {
            return meta;
        }
        logger.warn("Synchronization Accounting Task Meta Missing, Trying to Recreate. MappingId:}, name:}, taskId:}", mapping.getId(), mapping.getName(), mapping.getId());
        mappingChecker.addMeta(mapping);
        mappingProfile.update(mapping);
        return taskMetaProfile.getMeta(mapping.getId());
    }

    /**
     * InspectionYesNoExistenceSync task
     */
    private Mapping assertMappingExist(String mappingId) {
        Mapping mapping = mappingProfile.get(mappingId);
        Assert.notNull(mapping, "Sync task does not exist.");
        return mapping;
    }

    /**
     * InspectionMapping
     */
    private void assertTableGroupExist(String mappingId) {
        Assert.isTrue(tableGroupProfile.getTableGroupCount(mappingId) > 0, "Mapping cannot be empty");
    }

    private void clearMetaIfFinished(Mapping mapping) {
        if (ModelEnum.isIncrement(mapping.getModel())) {
            return;
        }
        String taskId = mapping.getId();
        Meta meta = taskMetaProfile.getMeta(taskId);
        Assert.notNull(meta, "Mapping meta can not be null.");
        long total = meta.getTotal().get();
        long processed = meta.getSuccess().get() + meta.getFail().get();
        // FinishTasksthenResetStatus，Easy again.Full（Requesttotal>0，Avoid0==0 Miscalculation)
        if (!CommonTaskStatusEnum.isRunning(meta.getState()) && total > 0 && processed >= total) {
            taskMetaProfile.resetMeta(taskId);
            taskProfile.deleteRunData(taskId);
        }
    }

    private void saveCustomTable(Mapping mapping, Map<String, String> params) {
        String type = params.get(ConfigConstant.CONFIG_MODEL_TYPE);
        String operator = params.get("operator");
        String customTable = params.get("customTable");
        boolean isSource = StringUtil.equals("source", type);
        DefaultConnectorServiceContext context = ConnectorServiceContextUtil.buildConnectorServiceContext(mapping, isSource);

        String instanceId = ConnectorInstanceUtil.buildConnectorInstanceId(context.getMappingId(), context.getConnectorId(), context.getSuffix());
        ConnectorInstance connectorInstance = connectorFactory.connect(instanceId);
        ConnectorService connectorService = connectorFactory.getConnectorService(connectorInstance.getConfig());
        ConfigValidator configValidator = connectorService.getConfigValidator();
        Assert.notNull(configValidator, "ConfigValidator can not be null.");
        Table newTable = configValidator.modifyExtendedTable(connectorService, params);
        Assert.notNull(newTable, "Parsing custom table anomalies");

        context.addTablePattern(newTable);
        List<MetaInfo> metaInfos = connectorService.getMetaInfo(connectorInstance, context);
        Assert.notEmpty(metaInfos, "SQL execution anomaly");
        Assert.notEmpty(metaInfos.get(0).getColumn(), "Can not open message");

        List<Table> tables = getMappingTables(mapping, isSource);
        // First EquallyAdd
        if (CollectionUtils.isEmpty(tables)) {
            tables.add(newTable);
            return;
        }

        // CreateActions
        Set<String> exist = tables.stream().map(Table::getName).collect(Collectors.toSet());
        String newTableName = newTable.getName();
        if (StringUtil.equals("add", operator)) {
            if (exist.contains(newTableName)) {
                throw new BizException(String.format("%s or custom table names repeat, please replace", isSource ? "Source" : "Target"));
            }
            tables.add(newTable);
            // Ups 55/2
            Collections.sort(tables, Comparator.comparing(Table::getName));
            return;
        }

        // EditActions，ChangeTable name
        if (!StringUtil.equals(customTable, newTableName)) {
            if (exist.contains(newTableName)) {
                throw new BizException(String.format("%s or custom table names repeat, please replace", isSource ? "Source" : "Target"));
            }
        }
        for (Table t : tables) {
            if (StringUtil.equals(t.getName(), customTable)) {
                t.setName(newTable.getName());
                t.setColumn(newTable.getColumn());
                t.setExtInfo(newTable.getExtInfo());
                break;
            }
        }

        // Ups 55/2
        Collections.sort(tables, Comparator.comparing(Table::getName));
    }

    private List<Table> getMappingTables(Mapping mapping, boolean isSource) {
        mapping.setSourceTable(!CollectionUtils.isEmpty(mapping.getSourceTable()) ? mapping.getSourceTable() : new ArrayList<>());
        mapping.setTargetTable(!CollectionUtils.isEmpty(mapping.getTargetTable()) ? mapping.getTargetTable() : new ArrayList<>());
        return isSource ? mapping.getSourceTable() : mapping.getTargetTable();
    }

    private void removeCustomTable(Mapping mapping, Map<String, String> params) {
        String type = params.get(ConfigConstant.CONFIG_MODEL_TYPE);
        String tableName = params.get("customTable");
        Assert.hasText(tableName, "No custom table.");
        List<Table> tables = StringUtil.equals("source", type) ? mapping.getSourceTable() : mapping.getTargetTable();
        if (!CollectionUtils.isEmpty(tables)) {
            Iterator<Table> iterator = tables.iterator();
            while (iterator.hasNext()) {
                Table t = iterator.next();
                switch (TableTypeEnum.getTableType(t.getType())) {
                    case SQL:
                    case SEMI:
                        if (StringUtil.equals(t.getName(), tableName)) {
                            iterator.remove();
                            return;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

}