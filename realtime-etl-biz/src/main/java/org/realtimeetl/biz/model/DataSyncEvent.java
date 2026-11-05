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
package org.realtimeetl.biz.model;

import java.util.List;

/**
 * @author Realtime ETL.
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
