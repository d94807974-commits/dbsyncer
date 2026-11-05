/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.parser.flush.impl;

import org.realtimeetl.common.QueueOverflowException;
import org.realtimeetl.common.config.GeneralBufferConfig;
import org.realtimeetl.common.metric.TimeRegistry;
import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.ParserComponent;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.TableGroupContext;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.ddl.DDLParser;
import org.realtimeetl.parser.event.RefreshOffsetEvent;
import org.realtimeetl.parser.flush.AbstractBufferActuator;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.model.TableGroupPicker;
import org.realtimeetl.parser.model.WriterRequest;
import org.realtimeetl.parser.model.WriterResponse;
import org.realtimeetl.parser.strategy.FlushStrategy;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.parser.util.ConnectorServiceContextUtil;
import org.realtimeetl.parser.util.ConvertUtil;
import org.realtimeetl.plugin.PluginFactory;
import org.realtimeetl.plugin.enums.ProcessEnum;
import org.realtimeetl.plugin.impl.IncrementPluginContext;
import org.realtimeetl.sdk.config.DDLConfig;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.DefaultConnectorServiceContext;
import org.realtimeetl.sdk.enums.ChangedEventTypeEnum;
import org.realtimeetl.sdk.model.ChangedOffset;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * General actuator（SingleThreadsConsumption, muchThreadsBulk, sequenced)
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022-03-27 16:50
 */
@Component
public class GeneralBufferActuator extends AbstractBufferActuator<WriterRequest, WriterResponse> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private GeneralBufferConfig generalBufferConfig;

    @Resource
    private Executor generalExecutor;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private ParserComponent parserComponent;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private PluginFactory pluginFactory;

    @Resource
    private FlushStrategy flushStrategy;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private DDLParser ddlParser;

    @Resource
    private TableGroupContext tableGroupContext;

    @Resource
    private RsaManager rsaManager;

    @PostConstruct
    public void init() {
        setConfig(generalBufferConfig);
        buildConfig();
    }

    @Override
    protected String getPartitionKey(WriterRequest request) {
        return request.getTableName();
    }

    @Override
    protected void partition(WriterRequest request, WriterResponse response) {
        if (!CollectionUtils.isEmpty(request.getRow())) {
            response.addData(request.getRow());
        }
        if (request.getChangedOffset() != null) {
            response.setChangedOffset(request.getChangedOffset());
        }
        if (!response.isMerged()) {
            response.setTraceId(request.getTraceId());
            response.setTableName(request.getTableName());
            response.setEvent(request.getEvent());
            response.setTypeEnum(request.getTypeEnum());
            response.setSql(request.getSql());
            response.setMerged(true);
        } else if (systemConfigProfile.getSystemConfig().isEnablePrintTraceInfo() && StringUtil.isNotBlank(request.getTraceId())) {
            logger.info("traceId:{} merge into traceId:{}", request.getTraceId(), response.getTraceId());
        }
    }

    @Override
    protected boolean skipPartition(WriterRequest nextRequest, WriterResponse response) {
        // The same data could trigger consecutively.Insert > Delete > Insert，BatchTasksThere' Schedule InitiativesEventTime, SkipminArea processing
        // Skip ActionsTable schemaUpdateEvent（Promise.Table schemaUpdateAtomic)
        return !StringUtil.equals(nextRequest.getEvent(), response.getEvent()) || ChangedEventTypeEnum.isDDL(response.getTypeEnum());
    }

    @Override
    public void pull(WriterResponse response) {
        Meta meta = taskMetaProfile.getMeta(response.getChangedOffset().getTaskId());
        if (meta == null) {
            return;
        }
        // Can not open message
        printTraceInfo(response);
        final Mapping mapping = mappingProfile.get(meta.getTaskId());
        List<TableGroupPicker> pickers = tableGroupContext.getTableGroupPickers(meta.getTaskId(), response.getTableName());

        switch (response.getTypeEnum()) {
            case DDL:
                tableGroupContext.update(mapping, pickers.stream().map(picker -> {
                    TableGroup tableGroup = tableGroupProfile.getTableGroup(picker.getTableGroup().getId());
                    parseDDl(response, mapping, tableGroup);
                    return tableGroup;
                }).collect(Collectors.toList()));
                break;
            case SCAN:
                pickers.forEach(picker -> distributeTableGroup(response, mapping, picker, picker.getSourceFields(), false));
                break;
            case ROW:
                pickers.forEach(picker -> distributeTableGroup(response, mapping, picker, picker.getTableGroup().getSourceTable().getColumn(), true));
                if (shouldPublishOffsetRefresh()) {
                    publishOffsetRefresh(response.getChangedOffset());
                }
                break;
            default:
                break;
        }
    }

    protected boolean shouldPublishOffsetRefresh() {
        return true;
    }

    /**
     * ReleaseIncrementalBitsRefreshEvent。
     *
     * @param changedOffset Bits
     */
    protected void publishOffsetRefresh(ChangedOffset changedOffset) {
        if (changedOffset != null) {
            applicationContext.publishEvent(new RefreshOffsetEvent(applicationContext, changedOffset));
        }
    }

    @Override
    protected void offerFailed(BlockingQueue<WriterRequest> queue, WriterRequest request) {
        throw new QueueOverflowException("Cache queue is calculateable");
    }

    @Override
    protected void meter(TimeRegistry timeRegistry, long count) {
        // /280Sync efficiency of statistical implementersTPS
        timeRegistry.meter(TimeRegistry.GENERAL_BUFFER_ACTUATOR_TPS).add(count);
    }

    @Override
    public Executor getExecutor() {
        return generalExecutor;
    }

    private void distributeTableGroup(WriterResponse response, Mapping mapping, TableGroupPicker tableGroupPicker, List<Field> sourceFields, boolean enableFilter) {
        // 1、Table GroupField
        ConnectorConfig sourceConfig = getConnectorConfig(mapping.getSourceConnectorId());
        ConnectorService sourceConnector = connectorFactory.getConnectorService(sourceConfig.getConnectorType());
        List<Map> sourceDataList = new ArrayList<>();
        List<Map> targetDataList = tableGroupPicker.getPicker().setSourceResolver(sourceConnector.getSchemaResolver())
                .pickTargetData(sourceFields, enableFilter, response.getDataList(), sourceDataList);
        if (CollectionUtils.isEmpty(targetDataList)) {
            return;
        }

        // 2、ParameterConvert
        TableGroup tableGroup = tableGroupPicker.getTableGroup();
        ConvertUtil.convert(tableGroup.getConvert(), targetDataList);

        // 3、PluginConvert
        final IncrementPluginContext context = new IncrementPluginContext();
        String sourceInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), mapping.getSourceConnectorId(), ConnectorInstanceUtil.SOURCE_SUFFIX);
        String targetInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), mapping.getTargetConnectorId(), ConnectorInstanceUtil.TARGET_SUFFIX);
        context.setSourceConnectorInstance(connectorFactory.connect(sourceInstanceId));
        context.setTargetConnectorInstance(connectorFactory.connect(targetInstanceId));
        context.setSourceTable(tableGroup.getSourceTable());
        context.setTargetTable(tableGroup.getTargetTable());
        context.setTraceId(response.getTraceId());
        context.setEvent(response.getEvent());
        context.setTargetFields(tableGroupPicker.getTargetFields());
        context.setCommand(tableGroup.getCommand());
        context.setBatchSize(getBufferWriterCount());
        context.setSourceList(sourceDataList);
        context.setTargetList(targetDataList);
        context.setPlugin(tableGroup.getPlugin());
        context.setPluginExtInfo(tableGroup.getPluginExtInfo());
        context.setForceUpdate(mapping.isForceUpdate());
        context.setEnablePrintTraceInfo(StringUtil.isNotBlank(response.getTraceId()));
        setRsaConfig(context);
        pluginFactory.process(context, ProcessEnum.CONVERT);

        // 4、Batch execution sync
        Result result = parserComponent.writeBatch(context, getExecutor());

        // 5、EnduringSyncResult
        result.setTableGroupId(tableGroup.getId());
        result.setTargetTableGroupName(context.getTargetTable().getName());
        result.setTaskId(mapping.getId());
        result.setEvent(response.getEvent());
        ConnectorConfig targetConfig = getConnectorConfig(mapping.getTargetConnectorId());
        ConnectorService targetConnectorService = connectorFactory.getConnectorService(targetConfig);
        flushStrategy.flushIncrementData(result, targetConnectorService.getSchemaResolver(), tableGroupPicker.getTargetFieldMap());

        // 6、Execute Post Processing
        pluginFactory.process(context, ProcessEnum.AFTER);
    }

    private void setRsaConfig(IncrementPluginContext context) {
        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        if (systemConfig.isEnableOpenAPI()) {
            context.setRsaManager(rsaManager);
            context.setRsaConfig(systemConfig.getRsaConfig());
        }
    }

    /**
     * ParsingDDL
     */
    private void parseDDl(WriterResponse response, Mapping mapping, TableGroup tableGroup) {
        try {
            ConnectorConfig sConnConfig = getConnectorConfig(mapping.getSourceConnectorId());
            ConnectorConfig tConnConfig = getConnectorConfig(mapping.getTargetConnectorId());
            String sConnType = sConnConfig.getConnectorType();
            String tConnType = tConnConfig.getConnectorType();
            String instanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), mapping.getTargetConnectorId(), ConnectorInstanceUtil.TARGET_SUFFIX);
            ConnectorInstance tConnectorInstance = connectorFactory.connect(instanceId);
            ConnectorService connectorService = connectorFactory.getConnectorService(tConnType);
            DDLConfig targetDDLConfig = ddlParser.parse(tConnectorInstance, connectorService, tableGroup, response.getSql());
            // 1.GenerateTarget tableImplementationSQL(Organisation)
            if (mapping.getListener().isEnableDDL() && StringUtil.equals(sConnType, tConnType)) {
                Result result = connectorFactory.writerDDL(tConnectorInstance, targetDDLConfig);
                // 2.EnduringIncremental eventsData Fact
                result.setTableGroupId(tableGroup.getId());
                result.setTargetTableGroupName(tableGroup.getTargetTable().getName());
                result.setTaskId(mapping.getId());
                result.setEvent(response.getEvent());
                flushStrategy.flushIncrementData(result, connectorService.getSchemaResolver(), null);
            }

            // 3.Update Table PropertiesField
            updateTableColumn(mapping, ConnectorInstanceUtil.SOURCE_SUFFIX, tableGroup.getSourceTable());
            updateTableColumn(mapping, ConnectorInstanceUtil.TARGET_SUFFIX, tableGroup.getTargetTable());

            // 4.EmpowermentFieldMapping
            ddlParser.refreshFiledMappings(tableGroup, targetDDLConfig);

            // 5.Update Enforcement Command
            tableGroup.setCommand(parserComponent.getCommand(mapping, tableGroup));

            // 6.EnduringStorage& Update CacheConfigure
            tableGroupProfile.editTableGroup(tableGroup);

            // 7.This is a matter of information.Event
            if (shouldPublishOffsetRefresh()) {
                publishOffsetRefresh(response.getChangedOffset());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void updateTableColumn(Mapping mapping, String suffix, Table table) {
        boolean isSource = StringUtil.equals(ConnectorInstanceUtil.SOURCE_SUFFIX, suffix);
        DefaultConnectorServiceContext context = ConnectorServiceContextUtil.buildConnectorServiceContext(mapping, isSource);
        context.addTablePattern(table);

        List<MetaInfo> metaInfos = parserComponent.getMetaInfo(context);
        MetaInfo metaInfo = CollectionUtils.isEmpty(metaInfos) ? null : metaInfos.get(0);
        Assert.notNull(metaInfo, "Could not close temporary folder: %s" + table.getName());
        table.setColumn(metaInfo.getColumn());
    }

    /**
     * AccessConnectorConfigure
     */
    private ConnectorConfig getConnectorConfig(String connectorId) {
        Assert.hasText(connectorId, "Connector id can not be empty.");
        Connector conn = connectorProfile.getConnector(connectorId);
        Assert.notNull(conn, "Connector can not be null.");
        return conn.getConfig();
    }

    private void printTraceInfo(WriterResponse response) {
        if (systemConfigProfile.getSystemConfig().isEnablePrintTraceInfo() && StringUtil.isNotBlank(response.getTraceId())) {
            logger.info("traceId:{}, tableName:{}, event:{}, offset:{}, row:{}", response.getTraceId(), response.getTableName(), response.getEvent(), JsonUtil
                    .objToJson(response.getChangedOffset()), response.getDataList());
        }
    }

}