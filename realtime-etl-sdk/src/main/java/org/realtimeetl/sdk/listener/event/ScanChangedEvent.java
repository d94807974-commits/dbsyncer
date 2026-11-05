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
package org.realtimeetl.sdk.listener.event;

import org.realtimeetl.sdk.enums.ChangedEventTypeEnum;

import java.util.List;

/**
 * ScheduledSweep changesEvent
 *
 * @version 1.0.0
 * @Author Realtime ETL
 * @Date 2023-08-20 20:00
 */
public final class ScanChangedEvent extends CommonChangedEvent {

    private final List<Object> changedRow;

    public ScanChangedEvent(String sourceTableName, String event, List<Object> data) {
        setSourceTableName(sourceTableName);
        setEvent(event);
        this.changedRow = data;
    }

    @Override
    public ChangedEventTypeEnum getType() {
        return ChangedEventTypeEnum.SCAN;
    }

    @Override
    public List<Object> getChangedRow() {
        return changedRow;
    }
}
