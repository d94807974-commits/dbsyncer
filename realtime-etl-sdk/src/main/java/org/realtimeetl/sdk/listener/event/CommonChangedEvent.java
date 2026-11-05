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

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.listener.ChangedEvent;
import org.realtimeetl.sdk.model.ChangedOffset;

/**
 * Generic changeEvent
 *
 * @version 1.0.0
 * @Author Realtime ETL
 * @Date 2023-08-20 20:00
 */
public abstract class CommonChangedEvent implements ChangedEvent {

    /**
     * traceId
     */
    private String traceId = StringUtil.EMPTY;

    /**
     * Change tableName
     */
    private String sourceTableName;

    /**
     * ChangeEvent
     */
    private String event;

    /**
     * IncrementalOffset
     */
    private final ChangedOffset changedOffset = new ChangedOffset();

    @Override
    public String getTraceId() {
        return traceId;
    }

    @Override
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @Override
    public String getSourceTableName() {
        return sourceTableName;
    }

    @Override
    public void setSourceTableName(String sourceTableName) {
        this.sourceTableName = sourceTableName;
    }

    @Override
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public ChangedOffset getChangedOffset() {
        return changedOffset;
    }

    public void setNextFileName(String nextFileName) {
        changedOffset.setNextFileName(nextFileName);
    }

    public void setPosition(Object position) {
        changedOffset.setPosition(position);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
