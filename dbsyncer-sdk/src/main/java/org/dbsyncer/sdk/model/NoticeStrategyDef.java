/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.model;

/**
 * Yeah.EnduringYes.Police strategy CorruptDefinitionsDTO，Corresponding Tabledbsyncer_notice_strategy）。
 *
 * @author wuji
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
     * {@link org.dbsyncer.sdk.enums.NoticeFrequencyEnum#getCode()}
     */
    private int frequency;

    /**
     * CycleCron（INTERVAL 78.
     */
    private String cron;

    /**
     * {@link org.dbsyncer.sdk.enums.NoticeTypeEnum#getCode()}
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
