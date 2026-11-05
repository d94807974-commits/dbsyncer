/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.model;

import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.constant.ConfigConstant;

/**
 * Alert notificationRule: PressGrouporSync taskAssign@phoneandEmail。
 *
 * @author wuji
 * @version 1.0.0
 */
public class NoticeRule extends ConfigModel {

    /**
     * Scope：{@code group} / {@code mapping}
     */
    private String scopeType;

    /**
     * Group ID orSync task ID
     */
    private String scopeId;

    /**
     * YesNo @everyone：0 No / 1 Yes
     */
    private int atAll;

    /**
     * @UserCell phone number，Multiple commasminBreak
     */
    private String atPhones;

    /**
     * EmailAddress, multiple commasminBreak
     */
    private String emails;

    public NoticeRule() {
        super.setName("");
        super.setType(ConfigConstant.NOTICE_RULE);
    }

    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    public String getScopeId() {
        return scopeId;
    }

    public void setScopeId(String scopeId) {
        this.scopeId = scopeId;
    }

    public int getAtAll() {
        return atAll;
    }

    public void setAtAll(int atAll) {
        this.atAll = atAll;
    }

    public boolean isAtAll() {
        return atAll == 1;
    }

    public String getAtPhones() {
        return atPhones;
    }

    public void setAtPhones(String atPhones) {
        this.atPhones = atPhones;
    }

    public String getEmails() {
        return emails;
    }

    public void setEmails(String emails) {
        this.emails = emails;
    }
}
