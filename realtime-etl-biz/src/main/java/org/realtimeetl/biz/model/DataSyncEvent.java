/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.model;

import java.util.List;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-02-23 15:27
 */
public class DataSyncEvent {

    /**
     * EventType INSERT/UPDATE/DELETE {@link org.realtimeetl.sdk.constant.ConnectorConstant}
     */
    private String event;

    /**
     * ChangedRow data
     */
    private List<Object> data;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public List<Object> getData() {
        return data;
    }

    public void setData(List<Object> data) {
        this.data = data;
    }
}
