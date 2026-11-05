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
package org.realtimeetl.sdk.model;

/**
 * Yeah.EnduringYes.Police strategy CorruptDefinitionsDTO，Corresponding Tablerealtime_etl_notice_strategy）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public class NoticeStrategyDef {

    private String id;

    private String name;

    /**
     * YesNoEnable
     */
    private boolean enabled = true;

    /**
     * {@link org.realtimeetl.sdk.enums.NoticeFrequencyEnum#getCode()}
     */
    private int frequency;

    /**
     * CycleCron（INTERVAL 78.
     */
    private String cron;

    /**
     * {@link org.realtimeetl.sdk.enums.NoticeTypeEnum#getCode()}
     */
    private int noticeTypes;

    /**
     * Remarks
     */
    private String remark;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getNoticeTypes() {
        return noticeTypes;
    }

    public void setNoticeTypes(int noticeTypes) {
        this.noticeTypes = noticeTypes;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
