/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.biz.task;

import org.realtimeetl.common.dispatch.AbstractDispatchTask;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.ParserComponent;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.parser.util.PickerUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.DefaultMetaContext;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.MetaIncrement;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.Map;

/**
 * Abstract classCount sync tasksTasks
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2025-06-25 01:00
 */
public abstract class AbstractCountTask extends AbstractDispatchTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private RsaManager rsaManager;

    @Resource
    private ParserComponent parserComponent;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private MappingProfile mappingProfile;

    protected void updateTableGroupCount(Mapping mapping, TableGroup tableGroup) {
        long now = Instant.now().toEpochMilli();
        TableGroup group = PickerUtil.mergeTableGroupConfig(mapping, tableGroup);
        Map<String, String> command = parserComponent.getCommand(mapping, group);
        String sourceConnectorId = mapping.getSourceConnectorId();
        String instanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), sourceConnectorId, ConnectorInstanceUtil.SOURCE_SUFFIX);
        ConnectorConfig config = connectorProfile.getConnector(sourceConnectorId).getConfig();
        ConnectorInstance connectorInstance = connectorFactory.connect(instanceId);
        Assert.notNull(command, "command can not null");
        ConnectorService connectorService = connectorFactory.getConnectorService(config);

        DefaultMetaContext metaContext = new DefaultMetaContext();
        metaContext.setCommand(command);
        metaContext.setSourceTable(group.getSourceTable());
        metaContext.setSourceConnectorInstance(connectorInstance);
        setRsaConfig(metaContext);

        long count = connectorService.getCount(connectorInstance, metaContext);
        // SOURCE_TOTAL Columnsand JSON InternalsourceTable.count Sync, for detail /Tasks Meta Summary
        tableGroup.setSourceTotal(count);
        tableGroup.getSourceTable().setCount(count);
        tableGroupProfile.editTableGroup(tableGroup);
        syncTableDetailMetaTotal(tableGroup.getId(), count);
        logger.info("Table {}, Total: }, {ms", mapping.getName(), tableGroup.getSourceTable().getName(), count, (Instant.now().toEpochMilli() - now));
    }

    /**
     * ClassifyMeta.TOTAL Align toSource tableStatisticsValue（AtomIncremental，Avoid whole lineOverwrite success/fail）。
     */
    private void syncTableDetailMetaTotal(String tableGroupId, long count) {
        if (taskMetaProfile == null || StringUtil.isBlank(tableGroupId)) {
            return;
        }
        Meta tableMeta = taskMetaProfile.getMetaDetail(tableGroupId);
        if (tableMeta == null || StringUtil.isBlank(tableMeta.getId())) {
            return;
        }
        long oldTotal = tableMeta.getTotal() == null ? 0L : tableMeta.getTotal().get();
        long delta = count - oldTotal;
        if (delta == 0L) {
            return;
        }
        taskMetaProfile.incrementMeta(MetaIncrement.of(tableMeta.getTaskId()).total(delta));
    }

    protected boolean shouldStop(String mappingId) {
        // Running
        if (isRunning()) {
            return false;
        }

        Mapping mapping = mappingProfile.get(mappingId);
        if (!ModelEnum.isFull(mapping.getModel())) {
            logger.warn("Sync Task modified, pre-termised ({,{)", mapping.getName(), mapping.getModel());
            return true;
        }
        return false;
    }

    private void setRsaConfig(DefaultMetaContext context) {
        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        if (systemConfig.isEnableOpenAPI()) {
            context.setRsaManager(rsaManager);
            context.setRsaConfig(systemConfig.getRsaConfig());
        }
    }
}
