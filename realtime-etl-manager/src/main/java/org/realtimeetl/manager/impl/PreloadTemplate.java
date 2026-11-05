/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.manager.impl;

import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.plugin.PluginFactory;
import org.realtimeetl.plugin.impl.DingTalkNoticeService;
import org.realtimeetl.plugin.impl.HttpNoticeService;
import org.realtimeetl.plugin.impl.MailNoticeService;
import org.realtimeetl.plugin.impl.WeChatNoticeService;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.NoticeChannelEnum;
import org.realtimeetl.sdk.model.NoticeConfig;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.parser.message.NoticeStrategyRuntimeBridge;
import org.realtimeetl.sdk.notice.MessageService;
import org.realtimeetl.sdk.service.ScheduledScanManager;
import org.realtimeetl.sdk.spi.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * PreloadConfigureTemplates
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/9/16 23:59
 */
@Component
public final class PreloadTemplate implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private PluginFactory pluginFactory;

    @Resource
    private MessageService messageService;

    @Resource
    private NoticeStrategyRuntimeBridge noticeStrategyRuntimeBridge;

    @Resource
    private LogService logService;

    @Resource
    private Executor generalExecutor;

    private boolean preloadCompleted;

    @Resource
    private ClusterService clusterService;

    @Resource
    private ScheduledScanManager scheduledScanManager;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        // Load plugins
        pluginFactory.loadPlugins();

        // Load Notification Channels
        loadNotificationChannel();

        // Single unit/clusterTotalUse: Semi-suspensiveMeta ReturnREADY，Avoid always「Stopping」
        reconcileStoppingMappingMetas();
        clusterService.init();
        if (clusterService.isStandalone()) {
            // Load connectorInstances
            loadConnectorInstance();
            // Sync task：PressTasksAdministrationMeta RestoreMapping
            launchSyncMappings();
            //InitializeScheduledTestTasks
            scheduledScanManager.start();
        }
        // Load notice strategies into decision engine
        loadNoticeStrategies();

        preloadCompleted = true;
    }

    public void loadNotificationChannel() {
        try {
            SystemConfig systemConfig = initSystemConfigIfAbsent();
            NoticeConfig noticeConfig = systemConfig.getNoticeConfig();
            if (null == noticeConfig) {
                return;
            }

            // EmailAnnouncements
            if (noticeConfig.getMail().isEnabled()) {
                MailNoticeService service = new MailNoticeService();
                service.setUsername(noticeConfig.getMail().getAccount());
                service.setPassword(noticeConfig.getMail().getCode());
                service.build();
                messageService.registerNotifyService(NoticeChannelEnum.EMAIL, service);
            } else {
                messageService.removeNotifyService(NoticeChannelEnum.EMAIL);
            }

            // Corporate WisdomAnnouncements
            if (noticeConfig.getWechat().isEnabled()) {
                WeChatNoticeService service = new WeChatNoticeService();
                messageService.registerNotifyService(NoticeChannelEnum.WE_CHAT, service);
            } else {
                messageService.removeNotifyService(NoticeChannelEnum.WE_CHAT);
            }

            // Nail.Announcements
            if (noticeConfig.getDingTalk().isEnabled()) {
                DingTalkNoticeService service = new DingTalkNoticeService();
                messageService.registerNotifyService(NoticeChannelEnum.DING_TALK, service);
            } else {
                messageService.removeNotifyService(NoticeChannelEnum.DING_TALK);
            }

            // HTTPAnnouncements
            if (noticeConfig.getHttp().isEnabled()) {
                HttpNoticeService service = new HttpNoticeService();
                messageService.registerNotifyService(NoticeChannelEnum.HTTP, service);
            } else {
                messageService.removeNotifyService(NoticeChannelEnum.HTTP);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * From the strategy sheet loads the alarm's decision-making mode.Registry / Cycleflush）。
     */
    private void loadNoticeStrategies() {
        try {
            noticeStrategyRuntimeBridge.reloadFromStore();
        } catch (Exception e) {
            logger.error("Failed to preload alarm strategy", e);
        }
    }

    /**
     * StartTimeNoneSystem settingsWrite default lines to avoid subsequent 40.getSystemConfig() EmptyNPE。
     *
     * @return Existing or NewSystem settings
     */
    private SystemConfig initSystemConfigIfAbsent() {
        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        if (systemConfig != null) {
            return systemConfig;
        }
        systemConfig = new SystemConfig();
        systemConfig.setName("System settings");
        long now = System.currentTimeMillis();
        systemConfig.setCreateTime(now);
        systemConfig.setUpdateTime(now);
        systemConfig.setClusterSecret(systemConfigProfile.generateApiSecret());
        systemConfigProfile.saveSystemConfig(systemConfig);
        logger.warn("No system config found, created default system config");
        return systemConfig;
    }

    /**
     * YesNoFinishPreloadConfigure
     */
    public boolean isPreloadCompleted() {
        return preloadCompleted;
    }

    /**
     * ConfigureImportFinishEnd of period: reconstructionConnectStressingDriver；OtherRunning TasksBy continuing.
     */
    public void afterConfigImport() {
        // Clusters don't preheat.AllConnector；Single machine stillFullPreheat
        if (clusterService.isStandalone()) {
            loadConnectorInstance();
        }
        reconcileStoppingMappingMetas();
        launchSyncMappings();
    }

    /**
     * To remainSTOPPING Yes.Sync task Meta AttributionREADY（Process restarts / Half-stops bottom.
     * <p>UnitandClusterStartcalling;noStartTasks，Correct control onlyStatus。
     */
    public void reconcileStoppingMappingMetas() {
        mappingProfile.pageScanTasks(ConfigConstant.PAGE_SIZE, mappings -> {
            if (CollectionUtils.isEmpty(mappings)) {
                return;
            }
            List<String> taskIds = new ArrayList<>();
            for (Mapping mapping : mappings) {
                if (mapping != null && StringUtil.isNotBlank(mapping.getId())) {
                    taskIds.add(mapping.getId());
                }
            }
            Map<String, Meta> metaMap = taskMetaProfile.getTaskMetaMap(taskIds);
            if (CollectionUtils.isEmpty(metaMap)) {
                return;
            }
            for (Mapping mapping : mappings) {
                if (mapping == null || StringUtil.isBlank(mapping.getId())) {
                    continue;
                }
                Meta meta = metaMap.get(mapping.getId());
                if (meta == null || meta.getState() != CommonTaskStatusEnum.STOPPING.getCode()) {
                    continue;
                }
                try {
                    changeMetaState(meta, CommonTaskStatusEnum.READY);
                } catch (Exception e) {
                    logger.error("Semi-suspensive Meta shut-down failed,", mapping.getId(), mapping.getName(), e.getMessage(), e);
                }
            }
        });
    }

    /**
     * Restore SyncDriver(Mapping)：Just keep running.Meta=RUNNING Yes.Tasks。
     * <p>STOPPING See you later.{@link #reconcileStoppingMappingMetas()}。
     * <p>Press first.Task Type {@code mapping} minPagePull manipulateTasks，Re-profilingTasksAdministrationMeta（{@code isTaskDetail=0}），
     * Addressing single loadsAll Meta。DetailsMeta Validation/relocationResultor table progress, not involvedandDriverStop.
     */
    private void launchSyncMappings() {
        mappingProfile.pageScanTasks(ConfigConstant.PAGE_SIZE, mappings -> {
            if (CollectionUtils.isEmpty(mappings)) {
                return;
            }
            List<String> taskIds = new ArrayList<>();
            for (Mapping mapping : mappings) {
                if (mapping != null && StringUtil.isNotBlank(mapping.getId())) {
                    taskIds.add(mapping.getId());
                }
            }
            Map<String, Meta> metaMap = taskMetaProfile.getTaskMetaMap(taskIds);
            if (CollectionUtils.isEmpty(metaMap)) {
                return;
            }
            for (Mapping mapping : mappings) {
                if (mapping == null || StringUtil.isBlank(mapping.getId())) {
                    continue;
                }
                Meta meta = metaMap.get(mapping.getId());
                if (meta == null || meta.getState() != CommonTaskStatusEnum.RUNNING.getCode()) {
                    continue;
                }
                try {
                    reConnect(mapping);
                    // Autorecover:CDC ListenStart FailedPressConfigureTry again
                    clusterService.start(mapping, true);
                } catch (Exception e) {
                    logger.error("Restore synchronous drive failed,", meta.getId(), mapping.getId(), e.getMessage(), e);
                }
            }
        });
    }

    public void reConnect(Mapping mapping) {
        reConnect(mapping.getId(), mapping.getSourceConnectorId(), mapping.getSourceDatabase(), mapping.getSourceSchema(),
                mapping.getTargetConnectorId(), mapping.getTargetDatabase(), mapping.getTargetSchema());
    }

    public void reConnect(ValidateSyncTask task) {
        //Source as query,Objective CombinedIt also needs to be asked.GeneratesqlStatement
        reConnect(task.getId(), task.getSourceConnectorId(), task.getSourceDatabase(), task.getSourceSchema(),
                task.getTargetConnectorId(), task.getTargetDatabase(), task.getTargetSchema());

    }

    public void reConnect(String uniqueId, String sourceConnectorId, String sourceDatabase, String sourceSchema,
                          String targetConnectorId, String targetDatabase, String targetSchema) {
        String sourceInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(uniqueId, sourceConnectorId, ConnectorInstanceUtil.SOURCE_SUFFIX);
        String targetInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(uniqueId, targetConnectorId, ConnectorInstanceUtil.TARGET_SUFFIX);
        Connector connector = connectorProfile.getConnector(sourceConnectorId);
        ConnectorInstance instance = connectorFactory.connect(sourceInstanceId, connector.getConfig(), sourceDatabase, sourceSchema);
        Assert.notNull(instance, "Source connector instance can not null");
        connector = connectorProfile.getConnector(targetConnectorId);
        instance = connectorFactory.connect(targetInstanceId, connector.getConfig(), targetDatabase, targetSchema);
        Assert.notNull(instance, "Target connector instance can not null");
    }

    private void loadConnectorInstance() {
        List<Connector> list = connectorProfile.getConnectorAll();
        if (!CollectionUtils.isEmpty(list)) {
            list.forEach(connector -> generalExecutor.execute(() -> {
                try {
                    ConnectorInstance connectorInstance = connectorFactory.connect(connector.getId(), connector.getConfig(), StringUtil.EMPTY, StringUtil.EMPTY);
                    logger.info("Completed connection {} {}", connector.getConfig().getConnectorType(), connectorInstance.getServiceUrl());
                } catch (Exception e) {
                    logger.error("Connection configuration anomaly", e);
                    logService.log(LogType.ConnectorLog.FAILED, e.getMessage());
                }
            }));
        }
    }

    public void changeMetaState(Meta meta, CommonTaskStatusEnum status) {
        if (null != meta && meta.getState() != status.getCode()) {
            long now = Instant.now().toEpochMilli();
            meta.setState(status.getCode());
            meta.setUpdateTime(now);
            // 进入运行中时记录本轮启动时间，供耗时（updateTime - startTime）计算
            if (CommonTaskStatusEnum.RUNNING == status) {
                meta.setStartTime(now);
            }
            taskMetaProfile.updateMeta(meta);
        }
    }

    public void changeMetaState(String taskId, CommonTaskStatusEnum status) {
        Meta meta = taskMetaProfile.getMeta(taskId);
        changeMetaState(meta, status);
    }
}