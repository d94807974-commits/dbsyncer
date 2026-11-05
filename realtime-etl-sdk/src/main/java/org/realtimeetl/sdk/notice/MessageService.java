package org.realtimeetl.sdk.notice;

import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.enums.NoticeChannelEnum;

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
