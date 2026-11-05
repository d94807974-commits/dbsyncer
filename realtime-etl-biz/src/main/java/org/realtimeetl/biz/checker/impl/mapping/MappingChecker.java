/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz.checker.impl.mapping;

import org.realtimeetl.biz.checker.AbstractChecker;
import org.realtimeetl.biz.checker.MappingConfigChecker;
import org.realtimeetl.biz.checker.impl.tablegroup.TableGroupChecker;
import org.realtimeetl.common.enums.TaskLevelEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.manager.impl.PreloadTemplate;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.sdk.config.ListenerConfig;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.EditionEnum;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.spi.LicenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020/1/8 15:17
 */
@Component
public class MappingChecker extends AbstractChecker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private Map<String, MappingConfigChecker> map;

    @Resource
    private TableGroupChecker tableGroupChecker;

    @Resource
    private PreloadTemplate preloadTemplate;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private LicenseService licenseService;

    @Override
    public Mapping checkAddConfigModel(Map<String, String> params) {
        logger.info("params:{}", params);
        String name = params.get(ConfigConstant.CONFIG_MODEL_NAME);
        Assert.hasText(name, "Driver name cannot be empty");

        Mapping mapping = new Mapping();
        mapping.setName(name);
        mapping.setModel(ModelEnum.FULL.getCode());
        mapping.setListener(new ListenerConfig(ListenerTypeEnum.LOG.getType()));
        mapping.setParams(new HashMap<>());

        // UpdateBasicConfigure
        this.modifyConfigModel(mapping, params);

        // Modify ConnectionConfigure
        updateConnector(params, mapping);

        // Createmeta
        addMeta(mapping);

        return mapping;
    }

    @Override
    public Mapping checkEditConfigModel(Map<String, String> params) {
        logger.info("params:{}", params);
        Assert.notEmpty(params, "MappingChecker check params is null.");
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        Mapping mapping = mappingProfile.get(id);
        Assert.notNull(mapping, "Can not find mapping.");

        // UpdateBasicConfigure
        this.modifyConfigModel(mapping, params);

        // Synchronisation Method(Only supportFullorIncremental syncModalities)
        String model = params.get("model");
        mapping.setModel(ModelEnum.getModelEnum(model).getCode());

        // FullConfigure
        mapping.setReadNum(NumberUtil.toInt(params.get("readNum"), mapping.getReadNum()));
        mapping.setBatchNum(NumberUtil.toInt(params.get("batchNum"), mapping.getBatchNum()));
        mapping.setThreadNum(NumberUtil.toInt(params.get("threadNum"), mapping.getThreadNum()));
        mapping.setForceUpdate(StringUtil.isNotBlank(params.get("forceUpdate")));
        applyChannelSize(mapping, params);

        // IncrementalConfigure(Log/Scheduled)
        String incrementStrategy = params.get("incrementStrategy");
        Assert.hasText(incrementStrategy, "MappingChecker check params incrementStrategy is empty");
        String type = StringUtil.toLowerCaseFirstOne(incrementStrategy).concat("ConfigChecker");
        MappingConfigChecker checker = map.get(type);
        Assert.notNull(checker, "Checker can not be null.");
        checker.modify(mapping, params);

        // CustomListenEventConfigure
        updateListenerConfig(mapping.getListener(), params);

        // UpdateAdvanced settings：Filter Conditions/Convert Configuration/Plugin Configuration
        this.modifySuperConfigModel(mapping, params);
        // Modify ConnectionConfigure
        updateConnector(params, mapping);

        // Merge AssociatedMappingConfigure
        batchMergeConfig(mapping, params);

        return mapping;
    }

    /**
     * ExecutorsConfigure
     *
     * @param mapping Mapping
     * @param params  Request parameters
     */
    private void applyChannelSize(Mapping mapping, Map<String, String> params) {
        if (licenseService.getEditionEnum() == EditionEnum.COMMUNITY) {
            return;
        }
        String raw = params.get("channelSize");
        mapping.setChannelSize(NumberUtil.toInt(raw));
    }

    private void updateConnector(Map<String, String> params, Mapping mapping) {
        String sourceConnectorId = params.get("sourceConnectorId");
        String sourceDatabase = params.get("sourceDatabase");
        String sourceSchema = params.get("sourceSchema");
        String targetConnectorId = params.get("targetConnectorId");
        String targetDatabase = params.get("targetDatabase");
        String targetSchema = params.get("targetSchema");
        Assert.hasText(sourceConnectorId, "Data sources cannot be empty.");
        Assert.hasText(targetConnectorId, "Target source cannot be empty.");

        // IfConnectChanges occurring, ReleaseSource databaseConnectResource
        String sId = mapping.getSourceConnectorId();
        if (StringUtil.isNotBlank(sId) && !StringUtil.equals(sId, sourceConnectorId)) {
            String sourceInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), sId, ConnectorInstanceUtil.SOURCE_SUFFIX);
            connectorFactory.disconnect(sourceInstanceId);
        }

        // IfConnectChanges occurring, ReleaseTarget databaseConnectResource
        String tId = mapping.getTargetConnectorId();
        if (StringUtil.isNotBlank(tId) && !StringUtil.equals(tId, targetConnectorId)) {
            String targetInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), tId, ConnectorInstanceUtil.TARGET_SUFFIX);
            connectorFactory.disconnect(targetInstanceId);
        }

        mapping.setSourceConnectorId(sourceConnectorId);
        mapping.setSourceDatabase(sourceDatabase);
        mapping.setSourceSchema(sourceSchema);
        mapping.setTargetConnectorId(targetConnectorId);
        mapping.setTargetDatabase(targetDatabase);
        mapping.setTargetSchema(targetSchema);
        preloadTemplate.reConnect(mapping);
    }

    public void addMeta(Mapping mapping) {
        Meta meta = new Meta();
        // TasksAdministrationMeta：taskId=Task ID，isTaskDetail=0；Primary keyBymodifyConfigModel GenerateSnowflake; detailminShit.taskId
        meta.setTaskId(mapping.getId());
        meta.setIsTaskDetail(TaskLevelEnum.TASK.getCode());
        this.modifyConfigModel(meta, new HashMap<>());
        taskMetaProfile.addMeta(meta);
    }

    /**
     * UpdateListenerConfigure
     *
     * @param listener
     * @param params
     */
    private void updateListenerConfig(ListenerConfig listener, Map<String, String> params) {
        Assert.notNull(listener, "ListenerConfig can not be null.");

        listener.setEnableUpdate(StringUtil.isNotBlank(params.get("enableUpdate")));
        listener.setEnableInsert(StringUtil.isNotBlank(params.get("enableInsert")));
        listener.setEnableDelete(StringUtil.isNotBlank(params.get("enableDelete")));
        listener.setEnableDDL(StringUtil.isNotBlank(params.get("enableDDL")));
    }

    private void batchMergeConfig(Mapping mapping, Map<String, String> params) {
        String sortedIds = params == null ? null : params.get("sortedTableGroupIds");
        if (StringUtil.isNotBlank(sortedIds)) {
            // Manual sorting requires a full set to reorderindex
            List<TableGroup> groupAll = new ArrayList<>();
            tableGroupProfile.pageScanTableGroups(mapping.getId(), ConfigConstant.PAGE_SIZE, groupAll::addAll);
            if (CollectionUtils.isEmpty(groupAll)) {
                return;
            }
            Map<String, Integer> indexBefore = snapshotIndexes(groupAll);
            Map<String, Map<String, String>> commandBefore = snapshotCommands(groupAll);
            sortTableGroup(groupAll, params);
            List<TableGroup> dirty = new ArrayList<>();
            for (TableGroup g : groupAll) {
                if (g == null) {
                    continue;
                }
                tableGroupChecker.mergeConfig(mapping, g);
                Integer oldIndex = indexBefore.get(g.getId());
                boolean indexChanged = oldIndex == null || oldIndex != g.getIndex();
                if (indexChanged || !commandEquals(commandBefore.get(g.getId()), g.getCommand())) {
                    dirty.add(g);
                }
            }
            tableGroupProfile.editTableGroupBatch(dirty);
            return;
        }
        // NoneSort: Press AccordinglyPageMerge, avoid whole-watchMemory；command Unchanged SkipUPDATE；If you change, pressPageBatchback
        tableGroupProfile.pageScanTableGroups(mapping.getId(), ConfigConstant.PAGE_SIZE, page -> {
            if (CollectionUtils.isEmpty(page)) {
                return;
            }
            List<TableGroup> dirty = new ArrayList<>(page.size());
            for (TableGroup g : page) {
                if (g == null) {
                    continue;
                }
                Map<String, String> before = copyCommand(g.getCommand());
                tableGroupChecker.mergeConfig(mapping, g);
                if (!commandEquals(before, g.getCommand())) {
                    dirty.add(g);
                }
            }
            tableGroupProfile.editTableGroupBatch(dirty);
        });
    }

    private Map<String, Integer> snapshotIndexes(List<TableGroup> groups) {
        Map<String, Integer> map = new HashMap<>(groups.size());
        for (TableGroup g : groups) {
            if (g != null && StringUtil.isNotBlank(g.getId())) {
                map.put(g.getId(), g.getIndex());
            }
        }
        return map;
    }

    private Map<String, Map<String, String>> snapshotCommands(List<TableGroup> groups) {
        Map<String, Map<String, String>> map = new HashMap<>(groups.size());
        for (TableGroup g : groups) {
            if (g != null && StringUtil.isNotBlank(g.getId())) {
                map.put(g.getId(), copyCommand(g.getCommand()));
            }
        }
        return map;
    }

    private Map<String, String> copyCommand(Map<String, String> command) {
        if (command == null || command.isEmpty()) {
            return null;
        }
        return new HashMap<>(command);
    }

    private boolean commandEquals(Map<String, String> before, Map<String, String> after) {
        if (before == after) {
            return true;
        }
        boolean beforeEmpty = before == null || before.isEmpty();
        boolean afterEmpty = after == null || after.isEmpty();
        if (beforeEmpty && afterEmpty) {
            return true;
        }
        if (beforeEmpty || afterEmpty) {
            return false;
        }
        return before.equals(after);
    }

    public void sortTableGroup(List<TableGroup> groupAll, Map<String, String> params) {
        // Sort Manually
        String[] sortedTableGroupIds = StringUtil.split(params.get("sortedTableGroupIds"), StringUtil.VERTICAL_LINE);
        if (null != sortedTableGroupIds && sortedTableGroupIds.length > 0) {
            Map<String, TableGroup> tableGroupMap = groupAll.stream().collect(Collectors.toMap(TableGroup::getId, f -> f, (k1, k2) -> k1));
            groupAll.clear();
            int size = sortedTableGroupIds.length;
            int i = size;
            while (i > 0) {
                TableGroup g = tableGroupMap.get(sortedTableGroupIds[size - i]);
                Assert.notNull(g, "Invalid sorted tableGroup.");
                g.setIndex(i);
                groupAll.add(g);
                i--;
            }
        }
    }
}