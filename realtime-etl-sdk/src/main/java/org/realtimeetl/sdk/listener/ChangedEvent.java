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

import org.realtimeetl.sdk.enums.ChangedEventTypeEnum;
import org.realtimeetl.sdk.listener.event.DDLChangedEvent;
import org.realtimeetl.sdk.listener.event.RowChangedEvent;
import org.realtimeetl.sdk.listener.event.ScanChangedEvent;
import org.realtimeetl.sdk.model.ChangedOffset;

import java.util.List;

/**
 * ChangeEvent
 *
 * @version 1.0.0
 * @Author Realtime ETL
 * @Date 2023-08-20 20:00
 */
public interface ChangedEvent extends Cloneable {

    /**
     * AccesstraceId
     */
    String getTraceId();

    /**
     * SettingstraceId
     */
    void setTraceId(String traceId);

    /**
     * Get changesEventType
     */
    ChangedEventTypeEnum getType();

    /**
     * Get Change Sheet ComorosName
     */
    String getSourceTableName();

    /**
     * Get Change Sheet ComorosName(DQLsceneCustom tableName)
     */
    void setSourceTableName(String sourceTableName);

    /**
     * Get changesEvent
     */
    String getEvent();

    /**
     * AccessIncrementalOffset
     */
    ChangedOffset getChangedOffset();

    /**
     * Get changesSQL
     * {@link DDLChangedEvent}
     */
    default String getSql() {
        return null;
    }

    /**
     * Get changesRow data
     * {@link RowChangedEvent}
     * {@link ScanChangedEvent}
     */
    default List<Object> getChangedRow() {
        return null;
    }

    Object clone() throws CloneNotSupportedException;
}
