/**
 * DBSyncer Copyright 2019-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.listener.event;

import org.realtimeetl.sdk.enums.ChangedEventTypeEnum;

import java.util.List;

/**
 * Listening line changesEvent
 *
 * @version 1.0.0
 * @Author AE86
 * @Date 2020-06-15 20:00
 */
public final class RowChangedEvent extends CommonChangedEvent {

    private final List<Object> changedRow;

    public RowChangedEvent(String sourceTableName, String event, List<Object> data, String nextFileName, Object position) {
        setSourceTableName(sourceTableName);
        setEvent(event);
        setNextFileName(nextFileName);
        setPosition(position);
        this.changedRow = data;
    }

    @Override
    public ChangedEventTypeEnum getType() {
        return ChangedEventTypeEnum.ROW;
    }

    @Override
    public List<Object> getChangedRow() {
        return changedRow;
    }
}
