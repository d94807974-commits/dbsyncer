/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.model;

import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.sdk.constant.ConfigConstant;

/**
 * Police strategy CorruptConfigure（Tabledbsyncer_notice_strategy），Each noticeTypeOne line.
 *
 * @author wuji
 * @version 1.0.0
 */
public class NoticeStrategyConfig extends ConfigModel {

    /**
     * AnnouncementsTypeCode, deposit.{@link org.dbsyncer.sdk.enums.NoticeTypeEnum#getCode()}
     */
    private int noticeTypes;

    /**
     * YesNoEnable：0 No / 1 Yes
     */
    private int enabled = 1;

    /**
     * Frequency:0-IMMEDIATE / 1-INTERVAL
     */
    private int frequency;

    /**
     * CycleCron（INTERVAL 78.
     */
    private String cron;

    /**
     * Remarks
     */
    private String remark;

    public NoticeStrategyConfig() {
        super.setName("");
        super.setType(ConfigConstant.NOTICE_STRATEGY);
    }

    public int getNoticeTypes() {
        return noticeTypes;
    }

    public void setNoticeTypes(int noticeTypes) {
        this.noticeTypes = noticeTypes;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled == 1;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
