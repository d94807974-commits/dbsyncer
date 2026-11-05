/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.model;

/**
 * EmailPolice!Configure
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2026-03-04 19:00
 */
public final class MailNoticeChannel extends NoticeChannel {

    /**
     * EmailAccount
     */
    private String account;

    /**
     * Fire!EmailYes.Code,Oh, no.YesMailboxPassword
     */
    private String code;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
