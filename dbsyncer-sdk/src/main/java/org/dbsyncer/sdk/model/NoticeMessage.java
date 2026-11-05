/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.model;



import java.util.List;

/**
 * Organisation
 *
 * @author AE86
 * @version 1.0.0
 * @date 2026/03/05 22:14
 */
public class NoticeMessage {

    /**
     * Contents of notice
     */
    private NoticeContent noticeContent;

    /**
     * Message Receiver
     */
    private List<String> receivers;

    /**
     * AnnouncementsConfigure
     */
    private NoticeConfig noticeConfig;

    public NoticeContent getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(NoticeContent noticeContent) {
        this.noticeContent = noticeContent;
    }

    public List<String> getReceivers() {
        return receivers;
    }

    public void setReceivers(List<String> receivers) {
        this.receivers = receivers;
    }

    public NoticeConfig getNoticeConfig() {
        return noticeConfig;
    }

    public void setNoticeConfig(NoticeConfig noticeConfig) {
        this.noticeConfig = noticeConfig;
    }
}
