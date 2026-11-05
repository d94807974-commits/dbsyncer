/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.impl;

import org.realtimeetl.common.config.AppConfig;
import org.realtimeetl.parser.ParserException;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.message.NoticeChannelDispatcher;
import org.realtimeetl.plugin.impl.AbstractNoticeService;
import org.realtimeetl.plugin.model.TestNoticeContent;
import org.realtimeetl.sdk.enums.NoticeChannelEnum;
import org.realtimeetl.sdk.enums.NoticeTypeEnum;
import org.realtimeetl.sdk.model.NoticeConfig;
import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.notice.MessageService;
import org.realtimeetl.sdk.notice.NoticeDecisionEngine;
import org.realtimeetl.sdk.notice.NoticeService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class MessageServiceImpl implements MessageService {

    @Resource
    private NoticeChannelDispatcher noticeChannelDispatcher;

    @Resource
    private NoticeDecisionEngine noticeDecisionEngine;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private AppConfig appConfig;

    @Override
    public void sendMessage(NoticeContent noticeContent) {
        NoticeConfig noticeConfig = systemConfigProfile.getSystemConfig().getNoticeConfig();
        if (noticeConfig == null || noticeContent == null) {
            return;
        }
        if (noticeContent.getNoticeType() == NoticeTypeEnum.TEST_MESSAGE) {
            noticeChannelDispatcher.deliver(noticeContent);
            return;
        }
        noticeDecisionEngine.dispatch(noticeContent);
    }

    @Override
    public String testSendMessage() {
        NoticeConfig noticeConfig = systemConfigProfile.getSystemConfig().getNoticeConfig();
        if (noticeConfig == null) {
            throw new ParserException("Please save the alarm configuration.");
        }
        TestNoticeContent noticeContent = new TestNoticeContent();
        noticeContent.setTitle("Manual Test Message");
        noticeContent.setContent("The alarm notice was configured.");
        sendMessage(noticeContent);
        return "success";
    }

    @Override
    public void registerNotifyService(NoticeChannelEnum noticeChannelEnum, NoticeService notificationService) {
        if (notificationService instanceof AbstractNoticeService) {
            AbstractNoticeService ans = (AbstractNoticeService) notificationService;
            ans.setAppConfig(appConfig);
        }
        noticeChannelDispatcher.registerNotifyService(noticeChannelEnum, notificationService);
    }

    @Override
    public void removeNotifyService(NoticeChannelEnum noticeChannelEnum) {
        noticeChannelDispatcher.removeNotifyService(noticeChannelEnum);
    }
}
