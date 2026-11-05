/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.model;

/**
 * The robot alert channel.Corporate Wisdom / Nail.Wait:webhook；From Sending@ ByRule on notificationOverwriteWrite.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-23
 */
public abstract class RobotNoticeChannel extends NoticeChannel {

    /**
     * Robot.webhook Address
     */
    private String webhookUrl;

    /**
     * YesNo @everyone
     */
    private boolean atAll;

    /**
     * @UserCell phone number，Multiple commasminBreak
     */
    private String atMobiles;

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public boolean isAtAll() {
        return atAll;
    }

    public void setAtAll(boolean atAll) {
        this.atAll = atAll;
    }

    public String getAtMobiles() {
        return atMobiles;
    }

    public void setAtMobiles(String atMobiles) {
        this.atMobiles = atMobiles;
    }
}
