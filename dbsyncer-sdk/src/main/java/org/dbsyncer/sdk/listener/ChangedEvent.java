/**
 * DBSyncer Copyright 2019-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.listener;

import org.dbsyncer.sdk.enums.ChangedEventTypeEnum;
import org.dbsyncer.sdk.listener.event.DDLChangedEvent;
import org.dbsyncer.sdk.listener.event.RowChangedEvent;
import org.dbsyncer.sdk.listener.event.ScanChangedEvent;
import org.dbsyncer.sdk.model.ChangedOffset;

import java.util.List;

/**
 * ChangeEvent
 *
 * @version 1.0.0
 * @Author AE86
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
