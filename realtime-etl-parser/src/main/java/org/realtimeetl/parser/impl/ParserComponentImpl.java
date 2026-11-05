/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.parser.impl;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.common.util.TaskSplitUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.ParserComponent;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.parser.model.FieldMapping;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Picker;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.parser.util.ConvertUtil;
import org.realtimeetl.parser.util.PickerUtil;
import org.realtimeetl.plugin.PluginFactory;
import org.realtimeetl.plugin.enums.ProcessEnum;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.DefaultConnectorServiceContext;
import org.realtimeetl.sdk.connector.FullPluginContext;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Filter;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.model.Task;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.realtimeetl.sdk.spi.FullFlushService;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/9/29 22:38
 */
@Component
public class ParserComponentImpl implements ParserComponent {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private PluginFactory pluginFactory;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private RsaManager rsaManager;

    @Resource
    private FullFlushService fullFlushService;

    @Override
    public List<MetaInfo> getMetaInfo(DefaultConnectorServiceContext context) {
        ConnectorInstance connectorInstance = requireInstance(context.getMappingId(), context.getConnectorId(),
                context.getCatalog(), context.getSchema(), context.getSuffix());
        return connectorFactory.getMetaInfo(connectorInstance, context);
    }

    @Override
    public Map<String, String> getCommand(Mapping mapping, TableGroup tableGroup) {
        return buildConnectorCommand(mapping.getId(), mapping.getSourceConnectorId(), mapping.getTargetConnectorId(),
                mapping.getSourceDatabase(), mapping.getSourceSchema(), mapping.getTargetDatabase(), mapping.getTargetSchema(),
                mapping.isForceUpdate(), tableGroup);
    }

    @Override
    public Map<String, String> getCommand(ValidateSyncTask task, TableGroup tableGroup) {
        return buildConnectorCommand(task.getId(), task.getSourceConnectorId(), task.getTargetConnectorId(),
                task.getSourceDatabase(), task.getSourceSchema(), task.getTargetDatabase(), task.getTargetSchema(), true, tableGroup);
    }

    /**
     * Based onDriver/Tasks id andTable Group BuildConnectorYou can't.
     */
    private Map<String, String> buildConnectorCommand(String mappingId, String sourceConnectorId, String targetConnectorId,
                                                      String sourceDatabase, String sourceSchema, String targetDatabase,
                                                      String targetSchema, boolean forceUpdate, TableGroup tableGroup) {
        ConnectorConfig sConnConfig = getConnectorConfig(sourceConnectorId);
        ConnectorConfig tConnConfig = getConnectorConfig(targetConnectorId);
        Table sourceTable = tableGroup.getSourceTable();
        Table targetTable = tableGroup.getTargetTable();
        Table sTable = sourceTable.clone().setColumn(new ArrayList<>());
        Table tTable = targetTable.clone().setColumn(new ArrayList<>());
        List<FieldMapping> fieldMapping = tableGroup.getFieldMapping();
        List<Filter> targetFilter = getTargetFilter(tableGroup, fieldMapping);
        if (!CollectionUtils.isEmpty(fieldMapping)) {
            fieldMapping.forEach(m -> {
                if (null != m.getSource()) {
                    sTable.getColumn().add(m.getSource());
                }
                if (null != m.getTarget()) {
                    tTable.getColumn().add(m.getTarget());
                }
            });
        }
        ConnectorInstance sourceInstance = requireInstance(mappingId, sourceConnectorId, sourceDatabase, sourceSchema,
                ConnectorInstanceUtil.SOURCE_SUFFIX);
        ConnectorInstance targetInstance = requireInstance(mappingId, targetConnectorId, targetDatabase, targetSchema,
                ConnectorInstanceUtil.TARGET_SUFFIX);
        final CommandConfig sourceConfig = new CommandConfig(sConnConfig.getConnectorType(), sourceSchema, sTable, sourceInstance, tableGroup.getFilter());
        sourceConfig.setCatalog(sourceDatabase);
        final CommandConfig targetConfig = new CommandConfig(tConnConfig.getConnectorType(), targetSchema, tTable, targetInstance, targetFilter);
        targetConfig.setCatalog(targetDatabase);
        targetConfig.setForceUpdate(forceUpdate);
        return connectorFactory.getCommand(sourceConfig, targetConfig);
    }

    @Override
    public void execute(Task task, Mapping mapping, TableGroup tableGroup, Executor executor) {
        final String taskId = task.getId();
        final String sourceConnectorId = mapping.getSourceConnectorId();
        final String targetConnectorId = mapping.getTargetConnectorId();

        ConnectorConfig sConfig = getConnectorConfig(sourceConnectorId);
        Assert.notNull(sConfig, "Data source configuration cannot be empty Regulatory");
        ConnectorConfig tConfig = getConnectorConfig(targetConnectorId);
        Assert.notNull(tConfig, "Target source configuration cannot be empty.");
        TableGroup group = PickerUtil.mergeTableGroupConfig(mapping, tableGroup);
        Map<String, String> command = group.getCommand();
        Assert.notEmpty(command, "Executing orders cannot be empty.");
        List<FieldMapping> fieldMapping = group.getFieldMapping();
        String sTableName = group.getSourceTable().getName();
        String tTableName = group.getTargetTable().getName();
        Assert.notEmpty(fieldMapping, String.format("Data source table [%s] synchronized to target source table [%s], map relationship not empty.", sTableName, tTableName));
        // Get SyncField
        Picker picker = new Picker(group);
        // CursorminPageuseandBuildQUERY_CURSOR The same.Primary keyList，AvoidfindTablePrimaryKeys BackNot included in tableandThe cursor.Primary key（Likeid）ResultgetLastCursors More CursorValue、ParameterNumberand SQL Placeholders are inconsistent
        boolean enableCursor = StringUtil.isNotBlank(command.get(ConnectorConstant.OPERTION_QUERY_CURSOR));
        List<String> primaryKeys = getPrimaryKeysForCursor(command, group.getSourceTable(), enableCursor);
        final FullPluginContext context = new FullPluginContext();

        context.setSourceConnectorInstance(requireInstance(mapping.getId(), sourceConnectorId, mapping.getSourceDatabase(),
                mapping.getSourceSchema(), ConnectorInstanceUtil.SOURCE_SUFFIX));
        context.setTargetConnectorInstance(requireInstance(mapping.getId(), targetConnectorId, mapping.getTargetDatabase(),
                mapping.getTargetSchema(), ConnectorInstanceUtil.TARGET_SUFFIX));
        context.setEvent(ConnectorConstant.OPERTION_INSERT);
        context.setCommand(command);
        context.setBatchSize(mapping.getBatchNum());
        context.setPlugin(group.getPlugin());
        context.setPluginExtInfo(group.getPluginExtInfo());
        context.setForceUpdate(mapping.isForceUpdate());
        context.setSourceTable(group.getSourceTable());
        context.setTargetTable(group.getTargetTable());
        context.setTargetFields(picker.getTargetFields());
        context.setSupportedCursor(enableCursor);
        context.setPageSize(mapping.getReadNum());
        setRsaConfig(context);
        ConnectorService sourceConnector = connectorFactory.getConnectorService(context.getSourceConnectorInstance().getConfig());
        ConnectorService targetConnector = connectorFactory.getConnectorService(context.getTargetConnectorInstance().getConfig());
        picker.setSourceResolver(sourceConnector.getSchemaResolver());
        Map<String, Field> targetFieldMap = picker.getTargetFieldMap();
        // 0、PluginPreprocess
        pluginFactory.process(context, ProcessEnum.BEFORE);

        for (; ; ) {
            if (!task.isRunning()) {
                logger.warn("Task aborted: {}", taskId);
                break;
            }

            // 1、Getting dataSourceData Fact
            context.setArgs(new ArrayList<>());
            context.setCursors(task.getCursors());
            context.setPageIndex(task.getPageIndex());
            Result reader = connectorFactory.reader(context);
            List<Map> source = reader.getSuccessData();
            if (CollectionUtils.isEmpty(source)) {
                logger.info("Completion of full sync task: {}, [} > [}]", taskId, sTableName, tTableName);
                break;
            }
            // minPage SQL NoneOver, Ben.PageCould cross.endCursors；We have to cut and write first.Nothensuccess It'll exceed the budget.
            source = PrimaryKeyUtil.trimToEndInclusive(source, primaryKeys, task.getEndCursors());
            if (CollectionUtils.isEmpty(source)) {
                break;
            }
            boolean hitEnd = PrimaryKeyUtil.reachedEnd(PrimaryKeyUtil.getLastCursors(source, primaryKeys), task.getEndCursors());

            // 2、MapField
            List<Map> target = picker.pickTargetData(source);

            // 3、ParameterConvert
            ConvertUtil.convert(group.getConvert(), target);

            // 4、PluginConvert
            context.setSourceList(source);
            context.setTargetList(target);
            pluginFactory.process(context, ProcessEnum.CONVERT);

            // 5、WritingTarget
            Result result = writeBatch(context, executor);

            // 6、UpdateResult
            task.setPageIndex(task.getPageIndex() + 1);
            task.setCursors(PrimaryKeyUtil.getLastCursors(source, primaryKeys));
            result.setTableGroupId(tableGroup.getId());
            result.setTargetTableGroupName(tTableName);
            fullFlushService.flush(task, result, targetConnector.getSchemaResolver(), targetFieldMap);
            // 7、SyncFinishAnnouncementsPluginReprocessing
            pluginFactory.process(context, ProcessEnum.AFTER);

            // 8、Release the data references in this instalment, avoidcontext Long-term holdingList（Like10000 (Article ), mitigationLinkedList ReservationsMemory
            if (context.getSourceList() != null) {
                context.getSourceList().clear();
            }
            if (context.getTargetList() != null) {
                context.getTargetList().clear();
            }
            if (hitEnd) {
                break;
            }
        }
    }

    @Override
    public Result writeBatch(PluginContext context, Executor executor) {
        Result result = new Result();
        // TerminationSync dataPresent.TargetLibrary
        if (context.isTerminated()) {
            result.getSuccessData().addAll(context.getTargetList());
            return result;
        }

        int batchSize = context.getBatchSize();
        List<Map> targetList = context.getTargetList();
        int total = targetList.size();
        // SingleTasks
        if (total <= batchSize) {
            return connectorFactory.writer(context);
        }

        // BatchTasks, Openmin
        int taskSize = (total + batchSize - 1) / batchSize;
        CountDownLatch latch = new CountDownLatch(taskSize);
        TaskSplitUtil.split(targetList, batchSize, batch -> {
            try {
                PluginContext tmpContext = (PluginContext) context.clone();
                tmpContext.setTargetList(batch);
                executor.execute(() -> {
                    try {
                        Result w = connectorFactory.writer(tmpContext);
                        result.addSuccessData(w.getSuccessData());
                        result.addFailData(w.getFailData());
                        result.getError().append(w.getError());
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            } catch (CloneNotSupportedException e) {
                logger.error(e.getMessage(), e);
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        return result;
    }

    private void setRsaConfig(FullPluginContext context) {
        if (systemConfigProfile.getSystemConfig().isEnableOpenAPI()) {
            context.setRsaManager(rsaManager);
            context.setRsaConfig(systemConfigProfile.getSystemConfig().getRsaConfig());
        }
    }

    /**
     * CursorminPageuseandBuildQUERY_CURSOR The same.Primary keyList；Not a cursor or notConfigureTimetablePrimary key。
     * AvoidfindTablePrimaryKeys(sourceTable) BackTechnicalAllPrimary key（UnspecifiedandThe cursor.id）ResultgetLastCursors More CursorValue、Parameterand SQL Placeholders are inconsistent.
     *
     * @param command      Enforcement orders (inclusive)CURSOR_PK_NAMES It means the cursor is actually used.Primary key）
     * @param sourceTable  Source table
     * @param enableCursor YesNoSupport cursor
     * @return ForgetLastCursors Yes.Primary keyFirst NameList
     */
    private List<String> getPrimaryKeysForCursor(Map<String, String> command, Table sourceTable, boolean enableCursor) {
        if (enableCursor) {
            String cursorPkNames = command.get(ConnectorConstant.CURSOR_PK_NAMES);
            if (StringUtil.isNotBlank(cursorPkNames)) {
                return Arrays.stream(cursorPkNames.split(StringUtil.COMMA)).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            }
        }
        // No cursor queries supported, or unstructuredConnectorscene
        return PrimaryKeyUtil.findTablePrimaryKeys(sourceTable);
    }

    /**
     * AccessConnectConfigure
     */
    private ConnectorConfig getConnectorConfig(String connectorId) {
        return connectorProfile.getConnector(connectorId).getConfig();
    }

    /**
     * AccessTasksLevelConnect，When the pool is not pressedConfigureConstruction.
     *
     * @param uniqueId    Tasks ID
     * @param connectorId Connector ID
     * @param catalog     Contents
     * @param schema      Mode
     * @param suffix      Source/Objective CombinedPostfix
     * @return ConnectExamples
     */
    private ConnectorInstance requireInstance(String uniqueId, String connectorId, String catalog, String schema, String suffix) {
        String instanceId = ConnectorInstanceUtil.buildConnectorInstanceId(uniqueId, connectorId, suffix);
        if (connectorFactory.contains(instanceId)) {
            return connectorFactory.connect(instanceId);
        }
        Connector connector = connectorProfile.getConnector(connectorId);
        Assert.notNull(connector, "Connector does not exist");
        return connectorFactory.connect(instanceId, connector.getConfig(), catalog, schema);
    }

    /**
     * Based on SourceCondition assemblyTarget tableCondition
     */
    private static List<Filter> getTargetFilter(TableGroup tableGroup, List<FieldMapping> fieldMapping) {
        List<Filter> filter = tableGroup.getFilter();
        List<Filter> targetFilter = new ArrayList<>();
        if (!CollectionUtils.isEmpty(filter)) {
            filter.forEach(f -> {
                Field matchedField = fieldMapping.stream()
                        .filter(m -> m.getSource().getName().equals(f.getName()))
                        .map(FieldMapping::getTarget)                 // AccessTarget fields
                        .findFirst()
                        .orElse(null);
                if (matchedField != null) {
                    Filter tmp = new Filter();
                    tmp.setName(matchedField.getName());
                    tmp.setFilter(f.getFilter());
                    tmp.setValue(f.getValue());
                    tmp.setOperation(f.getOperation());
                    targetFilter.add(tmp);
                }
            });
        }
        return targetFilter;
    }

}