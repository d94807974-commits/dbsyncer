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
package org.realtimeetl.parser.flush.impl;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.flush.AbstractBufferActuatorRouter;
import org.realtimeetl.sdk.listener.ChangedEvent;
import org.realtimeetl.sdk.model.BufferActuatorMetric;
import org.realtimeetl.sdk.spi.TableGroupBufferActuatorService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-12
 */
public class DefaultBufferActuatorRouter extends AbstractBufferActuatorRouter {

    @Resource
    private TableGroupBufferActuatorService tableGroupBufferActuatorService;

    /**
     * Execute RouteList
     */
    private final Map<String, Map<String, TableGroupBufferActuator>> router = new ConcurrentHashMap<>();

    @Override
    public void execute(String taskId, ChangedEvent event) {
        event.getChangedOffset().setTaskId(taskId);
        printTraceInfo(event);
        Map<String, TableGroupBufferActuator> processor = router.get(taskId);
        if (processor == null) {
            offer(generalBufferActuator, event);
            return;
        }
        TableGroupBufferActuator actuator = processor.get(event.getSourceTableName());
        if (actuator == null) {
            offer(generalBufferActuator, event);
            return;
        }
        offer(actuator, event);
    }

    @Override
    public void bind(String taskId, List<String> sourceTableNames, int channelSize) {
        if (StringUtil.isBlank(taskId) || sourceTableNames == null) {
            return;
        }
        final int maxBufferActuatorSize = systemConfigProfile.getSystemConfig().getMaxBufferActuatorSize();
        router.computeIfAbsent(taskId, k -> {
            Map<String, TableGroupBufferActuator> processor = new ConcurrentHashMap<>();
            for (String tableName : sourceTableNames) {
                if (StringUtil.isBlank(tableName)) {
                    logger.warn("Skip bind tableGroup with empty source table, taskId={}", taskId);
                    continue;
                }
                if (processor.size() >= maxBufferActuatorSize) {
                    logger.warn("Not allowed more than table processor limited size:{}", maxBufferActuatorSize);
                    break;
                }
                if (processor.containsKey(tableName)) {
                    continue;
                }
                try {
                    TableGroupBufferActuator newBufferActuator =
                            (TableGroupBufferActuator) tableGroupBufferActuatorService.clone();
                    newBufferActuator.setTableName(tableName);
                    newBufferActuator.start();
                    processor.put(tableName, newBufferActuator);
                } catch (CloneNotSupportedException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
            return processor;
        });
    }

    @Override
    public void unbind(String taskId) {
        router.computeIfPresent(taskId, (k, processor) -> {
            processor.values().forEach(this::stopActuator);
            return null;
        });
    }

    @Override
    public void destroy() {
        router.values().forEach(map -> map.values().forEach(this::stopActuator));
        router.clear();
    }

    @Override
    public long getQueueSize() {
        AtomicLong total = new AtomicLong();
        router.values().forEach(map -> map.values().forEach(actuator -> total.addAndGet(actuator.getQueue().size())));
        return total.get();
    }

    @Override
    public long getQueueCapacity() {
        AtomicLong total = new AtomicLong();
        router.values().forEach(map -> map.values().forEach(actuator -> total.addAndGet(actuator.getQueueCapacity())));
        return total.get();
    }

    @Override
    public List<BufferActuatorMetric> listMetrics() {
        if (router.isEmpty()) {
            return Collections.emptyList();
        }
        List<BufferActuatorMetric> metrics = new ArrayList<>();
        router.forEach((taskId, group) -> group.forEach((tableName, actuator) ->
                metrics.add(toMetric(taskId, actuator.getTableName(), actuator))));
        return metrics;
    }
}
