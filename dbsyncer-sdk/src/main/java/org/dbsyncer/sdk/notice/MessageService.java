package org.dbsyncer.sdk.notice;

import org.dbsyncer.sdk.model.NoticeContent;
import org.dbsyncer.sdk.enums.NoticeChannelEnum;

public interface MessageService {

    /**
     * Send Message
     *
     * @param noticeContent
     */
    void sendMessage(NoticeContent noticeContent);

    /**
     * TestSend Message
     */
    String testSendMessage();

    /**
     * Registration notification service
     *
     * @param noticeChannelEnum
     * @param notificationService
     */
    void registerNotifyService(NoticeChannelEnum noticeChannelEnum, NoticeService notificationService);

    /**
     * Remove Notification Service
     *
     * @param noticeChannelEnum
     */
    void removeNotifyService(NoticeChannelEnum noticeChannelEnum);
}
