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
package org.realtimeetl.sdk.listener;

import org.realtimeetl.common.model.RsaConfig;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.common.scheduled.ScheduledTaskService;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.sdk.config.ListenerConfig;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.model.ChangedOffset;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.spi.ConnectorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0.0
 * @author Realtime ETL
 * @date 2020-05-25 22:35
 */
public abstract class AbstractListener<C extends ConnectorInstance> implements Listener {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final int FLUSH_DELAYED_SECONDS = 20;
    protected String database;
    protected String schema;
    protected ConnectorInstance connectorInstance;
    protected ConnectorInstance targetConnectorInstance;
    protected ConnectorService connectorService;
    protected ScheduledTaskService scheduledTaskService;
    protected RsaManager rsaManager;
    protected RsaConfig rsaConfig;
    protected ConnectorConfig connectorConfig;
    protected ListenerConfig listenerConfig;
    protected Set<String> filterTable;
    protected List<Table> sourceTable;
    protected List<Table> customTable;
    protected Map<String, String> snapshot;
    protected String taskId;
    private Watcher watcher;

    @Override
    public void register(Watcher watcher) {
        this.watcher = watcher;
    }

    @Override
    public void changeEventBefore(QuartzListenerContext context) {
        watcher.changeEventBefore(context);
    }

    @Override
    public void changeEvent(ChangedEvent event) {
        if (null != event) {
            switch (event.getEvent()) {
                case ConnectorConstant.OPERTION_UPDATE:
                    // YesNoSupport listeningUpdateEvent
                    processEvent(listenerConfig.isEnableUpdate(), event);
                    break;
                case ConnectorConstant.OPERTION_INSERT:
                    // YesNoSupport listeningInsertEvent
                    processEvent(listenerConfig.isEnableInsert(), event);
                    break;
                case ConnectorConstant.OPERTION_DELETE:
                    // YesNoSupport listeningDeleteEvent
                    processEvent(listenerConfig.isEnableDelete(), event);
                    break;
                case ConnectorConstant.OPERTION_ALTER:
                    // Table schemaChangeEvent
                    watcher.changeEvent(event);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void refreshEvent(ChangedOffset offset) {
        // nothing to do
    }

    @Override
    public void flushEvent() {
        // 20sInternal update, execute writing
        if (watcher.getMetaUpdateTime() > Timestamp.valueOf(LocalDateTime.now().minusSeconds(FLUSH_DELAYED_SECONDS)).getTime()) {
            if (!CollectionUtils.isEmpty(snapshot)) {
                logger.info("snapshot：{}", snapshot);
                watcher.flushEvent(snapshot);
            }
        }
    }

    @Override
    public void forceFlushEvent() {
        if (!CollectionUtils.isEmpty(snapshot)) {
            logger.info("snapshot：{}", snapshot);
            watcher.flushEvent(snapshot);
        }
    }

    @Override
    public void errorEvent(Exception e) {
        watcher.errorEvent(e);
    }

    protected void sleepInMills(long timeout) {
        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            logger.info(e.getMessage());
        }
    }

    /**
     * If you're allowed to listen,Event，TriggerEventAnnouncements
     *
     * @param permitEvent
     * @param event
     */
    private void processEvent(boolean permitEvent, ChangedEvent event) {
        if (permitEvent) {
            watcher.changeEvent(event);
        }
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setConnectorInstance(ConnectorInstance connectorInstance) {
        this.connectorInstance = connectorInstance;
    }

    public C getConnectorInstance() {
        return (C) connectorInstance;
    }

    public void setTargetConnectorInstance(ConnectorInstance targetConnectorInstance) {
        this.targetConnectorInstance = targetConnectorInstance;
    }

    public void setConnectorService(ConnectorService connectorService) {
        this.connectorService = connectorService;
    }

    public void setScheduledTaskService(ScheduledTaskService scheduledTaskService) {
        this.scheduledTaskService = scheduledTaskService;
    }

    public void setRsaManager(RsaManager rsaManager) {
        this.rsaManager = rsaManager;
    }

    public void setRsaConfig(RsaConfig rsaConfig) {
        this.rsaConfig = rsaConfig;
    }

    public void setConnectorConfig(ConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

    public void setListenerConfig(ListenerConfig listenerConfig) {
        this.listenerConfig = listenerConfig;
    }

    public void setFilterTable(Set<String> filterTable) {
        this.filterTable = filterTable;
    }

    public void setSourceTable(List<Table> sourceTable) {
        this.sourceTable = sourceTable;
    }

    public void setCustomTable(List<Table> customTable) {
        this.customTable = customTable;
    }

    public void setSnapshot(Map<String, String> snapshot) {
        this.snapshot = snapshot;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
