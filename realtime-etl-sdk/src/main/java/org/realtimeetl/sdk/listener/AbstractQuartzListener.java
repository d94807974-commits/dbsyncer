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

import org.realtimeetl.common.QueueOverflowException;
import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.scheduled.ScheduledTaskJob;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.common.util.UUIDUtil;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.listener.event.ScanChangedEvent;
import org.realtimeetl.sdk.model.Point;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.model.TableGroupQuartzCommand;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ScheduledExtract
 *
 * @version 1.0.0
 * @Author Realtime ETL
 * @Date 2020-05-12 20:35
 */
public abstract class AbstractQuartzListener extends AbstractListener implements ScheduledTaskJob {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String CURSOR = "cursor";
    private  int readNum = 5000;
    private String mappingName;
    private List<TableGroupQuartzCommand> commands;
    private String eventFieldName;
    private boolean customEvent;
    private String event;
    private Set<String> update;
    private Set<String> insert;
    private Set<String> delete;
    private String taskKey;
    private volatile boolean running;
    private final Lock lock = new ReentrantLock();

    /**
     * AccessIncrementalParameter
     */
    protected abstract Point checkLastPoint(TableGroupQuartzCommand command, int index);

    @Override
    public void start() {
        eventFieldName = listenerConfig.getEventFieldName();
        if (StringUtil.isBlank(eventFieldName)) {
            if (listenerConfig.isEnableUpdate()) {
                event = ConnectorConstant.OPERTION_UPDATE;
                customEvent = true;
            } else if (listenerConfig.isEnableInsert()) {
                event = ConnectorConstant.OPERTION_INSERT;
                customEvent = true;
            }
        }
        update = Stream.of(listenerConfig.getUpdate().split(",")).collect(Collectors.toSet());
        insert = Stream.of(listenerConfig.getInsert().split(",")).collect(Collectors.toSet());
        delete = Stream.of(listenerConfig.getDelete().split(",")).collect(Collectors.toSet());

        taskKey = UUIDUtil.getUUID();
        running = true;

        flushPoint();
        scheduledTaskService.start(taskKey, listenerConfig.getCron(), this);
        logger.info("Start a time-bound task: }[{}]", mappingName, listenerConfig.getCron());
    }

    @Override
    public void run() {
        final Lock taskLock = lock;
        boolean locked = false;
        try {
            locked = taskLock.tryLock(3, TimeUnit.SECONDS);
            if (locked) {
                logger.info("Timed tasks: }[}", mappingName, listenerConfig.getCron());
                for (int i = 0; i < commands.size(); i++) {
                    execute(commands.get(i), i);
                }
            }
        } catch (Exception e) {
            errorEvent(e);
            logger.error(e.getMessage(), e);
        } finally {
            if (locked) {
                taskLock.unlock();
            }
        }
    }

    @Override
    public void close() {
        if (StringUtil.isNotBlank(taskKey)) {
            scheduledTaskService.stop(taskKey);
        }
        running = false;
    }

    @Override
    public Map<String, String> captureSnapshot() {
        if (CollectionUtils.isEmpty(commands)) {
            return Collections.emptyMap();
        }
        Map<String, String> captured = new HashMap<>();
        for (int i = 0; i < commands.size(); i++) {
            Point point = checkLastPoint(commands.get(i), i);
            point.refresh();
            if (!CollectionUtils.isEmpty(point.getPosition())) {
                captured.putAll(point.getPosition());
            }
        }
        return captured;
    }

    private void flushPoint() {
        if (CollectionUtils.isEmpty(snapshot)) {
            for (int i = 0; i < commands.size(); i++) {
                // Update BestCreateMeasures.
                Point point = checkLastPoint(commands.get(i), i);
                if (!CollectionUtils.isEmpty(point.getPosition())) {
                    snapshot.putAll(point.getPosition());
                }
            }
            super.forceFlushEvent();
        }
    }

    private void execute(TableGroupQuartzCommand cmd, int index) {
        final Table table = cmd.getTable();

        // InspectionIncrementalPoints 71
        Point point = checkLastPoint(cmd, index);
        int pageIndex = 1;
        Object[] cursors = PrimaryKeyUtil.getLastCursors((String) snapshot.get(index + CURSOR));

        final QuartzListenerContext context = new QuartzListenerContext();
        context.setSourceConnectorInstance(connectorInstance);
        context.setTargetConnectorInstance(targetConnectorInstance);
        context.setSourceTable(table);
        context.setTargetTable(cmd.getTargetTable());
        context.setCommand(point.getCommand());
        context.setSupportedCursor(isSupportedCursor(cmd));
        context.setPageSize(readNum);
        context.setPlugin(cmd.getPlugin());
        context.setPluginExtInfo(cmd.getPluginExtInfo());
        context.setRsaManager(rsaManager);
        context.setRsaConfig(rsaConfig);
        changeEventBefore(context);
        while (running) {
            context.setArgs(point.getArgs());
            context.setCursors(cursors);
            context.setPageIndex(pageIndex++);
            Result reader = connectorService.reader(connectorInstance, context);
            List<Map> data = reader.getSuccessData();
            if (CollectionUtils.isEmpty(data)) {
                cursors = new Object[0];
                break;
            }
            logger.info("{}[{}], data=[{}]", mappingName, context.getSourceTable().getName(), data.size());
            for (Map<String, Object> row : data) {
                if (customEvent) {
                    trySendEvent(new ScanChangedEvent(table.getName(), event, cmd.getChangedRow(row)));
                    continue;
                }

                Object eventValue = StringUtil.toString(row.get(eventFieldName));
                if (update.contains(eventValue)) {
                    trySendEvent(new ScanChangedEvent(table.getName(), ConnectorConstant.OPERTION_UPDATE, cmd.getChangedRow(row)));
                    continue;
                }
                if (insert.contains(eventValue)) {
                    trySendEvent(new ScanChangedEvent(table.getName(), ConnectorConstant.OPERTION_INSERT, cmd.getChangedRow(row)));
                    continue;
                }
                if (delete.contains(eventValue)) {
                    trySendEvent(new ScanChangedEvent(table.getName(), ConnectorConstant.OPERTION_DELETE, cmd.getChangedRow(row)));
                }
            }
            // Update Record Point
            if (context.isSupportedCursor()) {
                cursors = getLastCursors(cmd, data);
            }
            point.refresh();

            // Clean up objects on time
            if (!CollectionUtils.isEmpty(data)) {
                data.clear();
            }
        }

        // Enduring
        if (point.refreshed()) {
            snapshot.putAll(point.getPosition());
        }
        if (context.isSupportedCursor() && cursors != null && cursors.length > 0) {
            snapshot.put(index + CURSOR, StringUtil.join(cursors, ","));
        }
    }

    protected boolean isSupportedCursor(TableGroupQuartzCommand cmd) {
        final Map<String, String> command = cmd.getCommand();
        return StringUtil.isNotBlank(command.get(ConnectorConstant.OPERTION_QUERY_CURSOR));
    }

    protected Object[] getLastCursors(TableGroupQuartzCommand cmd, List<Map> data) {
        return PrimaryKeyUtil.getLastCursors(data, cmd.getPrimaryKeys());
    }

    private void trySendEvent(ChangedEvent event) {
        // If you consumeEventFailed，Try again
        while (running) {
            try {
                changeEvent(event);
                break;
            } catch (QueueOverflowException e) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    public void setReadNum(int readNum) {
        this.readNum = readNum;
    }

    public List<TableGroupQuartzCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<TableGroupQuartzCommand> commands) {
        this.commands = commands;
    }
}
