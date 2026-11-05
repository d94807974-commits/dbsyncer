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
package org.realtimeetl.manager.impl;

import org.realtimeetl.common.config.IncrementRecoveryConfig;
import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.common.scheduled.ScheduledTaskJob;
import org.realtimeetl.common.scheduled.ScheduledTaskService;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.manager.AbstractPuller;
import org.realtimeetl.manager.ManagerException;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.TableGroupContext;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.consumer.ParserConsumer;
import org.realtimeetl.parser.enums.ParserEnum;
import org.realtimeetl.parser.event.RefreshOffsetEvent;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.Picker;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.parser.util.FullTableProgressUtil;
import org.realtimeetl.parser.util.PickerUtil;
import org.realtimeetl.sdk.config.ListenerConfig;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.AbstractListener;
import org.realtimeetl.sdk.listener.AbstractQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.ChangedOffset;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.model.TableGroupQuartzCommand;
import org.realtimeetl.sdk.spi.BufferActuatorRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Incremental sync
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020-04-26 15:28
 */
@Component
public final class IncrementPuller extends AbstractPuller implements ApplicationListener<RefreshOffsetEvent>, ScheduledTaskJob {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private BufferActuatorRouterService bufferActuatorRouter;

    @Resource
    private ScheduledTaskService scheduledTaskService;

    @Resource
    private RsaManager rsaManager;

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
    private LogService logService;

    @Resource
    private TableGroupContext tableGroupContext;

    @Resource
    private IncrementRecoveryConfig incrementRecoveryConfig;

    @Resource
    private ParserConsumer parserConsumer;

    private final Map<String, Listener> map = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        scheduledTaskService.start(3000, this);
    }

    @Override
    public void start(Mapping mapping) {
        start(mapping, false);
    }

    @Override
    public void start(Mapping mapping, boolean autoRecovery) {
        final String mappingId = mapping.getId();
        Connector connector = connectorProfile.getConnector(mapping.getSourceConnectorId());
        Assert.notNull(connector, "Connector cannot be empty.");
        Connector targetConnector = connectorProfile.getConnector(mapping.getTargetConnectorId());
        Assert.notNull(targetConnector, "Target connector cannot be empty.");
        Assert.isTrue(tableGroupProfile.getTableGroupCount(mappingId) > 0, "The map relationship cannot be empty. Add the source table to the target table relationship.");
        List<TableGroup> list = loadSortedTableGroups(mappingId);
        Meta meta = taskMetaProfile.getMeta(mappingId);
        Assert.notNull(meta, "Meta can't be empty.");

        Thread worker = new Thread(() -> {
            try {
                Listener listener = map.computeIfAbsent(mappingId, k -> {
                    logger.info("Start Incremental Sync: }, }", mappingId, mapping.getName());
                    long now = Instant.now().toEpochMilli();
                    meta.setStartTime(now);
                    meta.setUpdateTime(now);
                    taskMetaProfile.updateMeta(meta);
                    tableGroupContext.put(mapping, list);
                    return buildListener(mapping, connector, targetConnector, list, meta);
                });
                startListener(mapping, mappingId, listener, autoRecovery);
            } catch (Exception e) {
                close(mappingId);
                publishClosedEvent(mappingId);
                logService.log(LogType.TableGroupLog.INCREMENT_FAILED, String.format("Launch Case failed: [%s], %s", mapping.getName(), e.getMessage()));
                logger.error("Run an anomaly, close incremental sync: }", mappingId, e);
            }
        });
        worker.setName("increment-worker-" + mapping.getId());
        worker.setDaemon(false);
        worker.start();
    }

    /**
     * Start listening。Automatically restore (service restart) to avoid dataLibraryAfter serviceStartResultCDC ListenConnection failed，
     * Press{@link IncrementRecoveryConfig} ConfigureNumberandIntervalTry again；ManualStartJust try once.
     */
    private void startListener(Mapping mapping, String taskId, Listener listener, boolean autoRecovery) throws Exception {
        int maxAttempts = autoRecovery ? incrementRecoveryConfig.getRetryTimes() : 1;
        for (int i = 1; i <= maxAttempts; i++) {
            try {
                listener.start();
                if (i > 1) {
                    logger.info("Accurate synchronous retry successful, task name: {, {}", mapping.getName(), i);
                }
                return;
            } catch (Exception e) {
                // Last time.Failed，or while waitingTasksDisabled，Not anymore.Try again
                if (i >= maxAttempts) {
                    throw e;
                }
                logger.error("Failed to start the incremental sync, task name: }ms", mapping.getName(), incrementRecoveryConfig.getRetryInterval(), e);
                TimeUnit.MILLISECONDS.sleep(incrementRecoveryConfig.getRetryInterval());
                if (!map.containsKey(taskId)) {
                    logger.info("The incremental sync task is closed and the retry is terminated: }, {", taskId, mapping.getName());
                    return;
                }
            }
        }
    }

    /**
     * CaptureSaveCurrentIncrementalbits, at the same timeResetFullProgressFull + incrementalMode Publications)
     *
     * @param mapping Driver
     */
    public void captureAndSaveOffset(Mapping mapping) {
        Connector connector = connectorProfile.getConnector(mapping.getSourceConnectorId());
        Assert.notNull(connector, "Connector cannot be empty.");
        Connector targetConnector = connectorProfile.getConnector(mapping.getTargetConnectorId());
        Assert.notNull(targetConnector, "Target connector cannot be empty.");
        Assert.isTrue(tableGroupProfile.getTableGroupCount(mapping.getId()) > 0, "The map relationship cannot be empty. Add the source table to the target table relationship.");
        List<TableGroup> list = loadSortedTableGroups(mapping.getId());
        Meta meta = taskMetaProfile.getMeta(mapping.getId());
        Assert.notNull(meta, "Meta can't be empty.");
        Listener listener = buildListener(mapping, connector, targetConnector, list, meta);
        Map<String, String> snapshot = meta.getSnapshot();
        snapshot.putAll(listener.captureSnapshot());
        snapshot.put(ParserEnum.FULL_INCREMENT_PHASE.getCode(), ModelEnum.FULL.getCode());
        snapshot.put(ParserEnum.PAGE_INDEX.getCode(), String.valueOf(ParserEnum.PAGE_INDEX.getDefaultValue()));
        snapshot.put(ParserEnum.CURSOR.getCode(), StringUtil.EMPTY);
        snapshot.put(ParserEnum.TABLE_GROUP_INDEX.getCode(), String.valueOf(ParserEnum.TABLE_GROUP_INDEX.getDefaultValue()));
        snapshot.remove(ParserEnum.TABLE_PROGRESS.getCode());
        FullTableProgressUtil.clearAll(taskMetaProfile, tableGroupProfile, mapping.getId());
        meta.getSuccess().set(0);
        meta.getFail().set(0);
        taskMetaProfile.updateMeta(meta);
        logger.info("Full Needles + Incremental Mode Saved Incremental Bits: {, {", mapping.getName(), snapshot);
    }

    @Override
    public void close(String taskId) {
        map.compute(taskId, (k, listener) -> {
            if (listener != null) {
                listener.close();
            }
            bufferActuatorRouter.unbind(taskId);
            tableGroupContext.clear(taskId);
            if (shouldPublishClosedAfterStop(taskId)) {
                publishClosedEvent(taskId);
            }
            logger.info("Close success: }", taskId);
            return null;
        });
    }

    @Override
    public List<String> getTaskId() {
        return new ArrayList<>(map.keySet());
    }

    /**
     * UserStopBackMeta YesSTOPPING，YesClosedEvent Shut-down; on-site fenceStopNo. No.
     *
     * @param taskId
     * @return true Should be publishedClosedEvent
     */
    private boolean shouldPublishClosedAfterStop(String taskId) {
        if (StringUtil.isBlank(taskId)) {
            return false;
        }
        Meta meta = taskMetaProfile.getMeta(taskId);
        return meta != null && meta.getState() == CommonTaskStatusEnum.STOPPING.getCode();
    }

    @Override
    public void onApplicationEvent(RefreshOffsetEvent event) {
        ChangedOffset offset = event.getChangedOffset();
        if (offset != null && map.containsKey(offset.getTaskId())) {
            map.get(offset.getTaskId()).refreshEvent(offset);
        }
    }

    @Override
    public void run() {
        // ScheduledSyncIncrementalInfo
        map.values().forEach(Listener::flushEvent);
    }

    private Listener buildListener(Mapping mapping, Connector connector, Connector targetConnector, List<TableGroup> list, Meta meta) {
        ConnectorConfig connectorConfig = connector.getConfig();
        ListenerConfig listenerConfig = mapping.getListener();
        String listenerType = listenerConfig.getListenerType();

        Listener listener = connectorFactory.getListener(connectorConfig.getConnectorType(), listenerType);
        if (null == listener) {
            throw new ManagerException(String.format("Unsupported listener type \"%s\".", connectorConfig.getConnectorType()));
        }
        ParserConsumer consumer = parserConsumer.clone();
        consumer.build(meta.getTaskId(), list, mapping.getChannelSize());
        listener.register(consumer);

        // DefaultScheduledExtract
        if (ListenerTypeEnum.isTiming(listenerType) && listener instanceof AbstractQuartzListener) {
            AbstractQuartzListener quartzListener = (AbstractQuartzListener) listener;
            List<TableGroupQuartzCommand> quartzCommands = list.stream().map(t -> {
                final TableGroup group = PickerUtil.mergeTableGroupConfig(mapping, t);
                final Picker picker = new Picker(group);
                List<Field> fields = picker.getSourceFields();
                Assert.notEmpty(fields, "Table field map cannot be empty:" + group.getSourceTable().getName() + " > " + group.getTargetTable().getName());
                return new TableGroupQuartzCommand(t.getSourceTable(), fields, t.getTargetTable(), t.getCommand(), group.getPlugin(), group.getPluginExtInfo());
            }).collect(Collectors.toList());
            quartzListener.setMappingName(mapping.getName());
            quartzListener.setReadNum(mapping.getReadNum());
            quartzListener.setCommands(quartzCommands);
        }

        if (listener instanceof AbstractListener) {
            AbstractListener abstractListener = (AbstractListener) listener;
            Set<String> filterTable = new HashSet<>();
            List<Table> sourceTable = new ArrayList<>();
            List<Table> customTable = new ArrayList<>();
            list.forEach(t -> addSourceTable(sourceTable, customTable, filterTable, t.getSourceTable()));
            abstractListener.setDatabase(mapping.getSourceDatabase());
            abstractListener.setSchema(mapping.getSourceSchema());
            abstractListener.setConnectorService(connectorFactory.getConnectorService(connectorConfig.getConnectorType()));
            String sourceInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), connector.getId(), ConnectorInstanceUtil.SOURCE_SUFFIX);
            String targetInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), targetConnector.getId(), ConnectorInstanceUtil.TARGET_SUFFIX);
            abstractListener.setConnectorInstance(connectorFactory.connect(sourceInstanceId, connector.getConfig(), mapping.getSourceDatabase(), mapping.getSourceSchema()));
            abstractListener.setTargetConnectorInstance(connectorFactory.connect(targetInstanceId, targetConnector.getConfig(), mapping.getTargetDatabase(), mapping.getTargetSchema()));
            abstractListener.setScheduledTaskService(scheduledTaskService);
            setRsaConfig(abstractListener);
            abstractListener.setConnectorConfig(connectorConfig);
            abstractListener.setListenerConfig(listenerConfig);
            abstractListener.setFilterTable(filterTable);
            abstractListener.setSourceTable(sourceTable);
            abstractListener.setCustomTable(customTable);
            abstractListener.setSnapshot(meta.getSnapshot());
            abstractListener.setTaskId(meta.getId());
        }

        listener.init();
        return listener;
    }

    private void setRsaConfig(AbstractListener listener) {
        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        if (systemConfig.isEnableOpenAPI()) {
            listener.setRsaManager(rsaManager);
            listener.setRsaConfig(systemConfig.getRsaConfig());
        }
    }

    private void addSourceTable(List<Table> sourceTable, List<Table> customTable, Set<String> filterTable, Table table) {
        switch (TableTypeEnum.getTableType(table.getType())) {
            case TABLE:
            case VIEW:
            case MATERIALIZED_VIEW:
                if (!filterTable.contains(table.getName())) {
                    sourceTable.add(table);
                    filterTable.add(table.getName());
                }
                break;
            case SQL:
            case SEMI:
                if (!filterTable.contains(table.getName())) {
                    customTable.add(table);
                    filterTable.add(table.getName());
                    Object mainTable = table.getExtInfo().get(ConnectorConstant.CUSTOM_TABLE_MAIN);
                    if (mainTable instanceof String) {
                        filterTable.add(String.valueOf(mainTable));
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * PressPageDraw Table MapsortIndex ) for registration of listening devices.
     */
    private List<TableGroup> loadSortedTableGroups(String mappingId) {
        List<TableGroup> list = new ArrayList<>();
        tableGroupProfile.pageScanTableGroups(mappingId, ConfigConstant.PAGE_SIZE, list::addAll);
        return list;
    }

}