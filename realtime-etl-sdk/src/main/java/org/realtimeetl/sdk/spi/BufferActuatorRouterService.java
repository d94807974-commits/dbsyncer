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
package org.realtimeetl.sdk.spi;

import org.realtimeetl.sdk.listener.ChangedEvent;
import org.realtimeetl.sdk.model.BufferActuatorMetric;

import java.util.List;

/**
 * IncrementalCache Executor Route
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-12
 */
public interface BufferActuatorRouterService {

    /**
     * TieSync taskYes.IncrementalExecutor.
     *
     * @param taskId           Sync taskID
     * @param sourceTableNames Source Table NameList
     * @param channelSize      Executors
     */
    void bind(String taskId, List<String> sourceTableNames, int channelSize);

    /**
     * UntieStopTheMappingUnder the implementer.
     *
     * @param taskId Sync taskID
     */
    void unbind(String taskId);

    /**
     * OrganisationIncrementalChangeEvent。
     *
     * @param taskId Sync taskID
     * @param event  ChangeEvent
     */
    void execute(String taskId, ChangedEvent event);

    /**
     * Queue Addressing All Executors within RouteBacklogTotal（does not containGeneral actuator）。
     *
     * @return BacklogNumber
     */
    long getQueueSize();

    /**
     * Sum of Queue Capacity of All Executors within RoutesGeneral actuator）。
     *
     * @return Capacity
     */
    long getQueueCapacity();

    /**
     * Execut MOUMonitorSc 49.
     *
     * @return PhotoList，NoneExecutor onBackEmptyList
     */
    List<BufferActuatorMetric> listMetrics();
}
