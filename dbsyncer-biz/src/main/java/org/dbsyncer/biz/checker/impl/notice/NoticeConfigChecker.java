/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.checker.impl.notice;

import org.dbsyncer.biz.BizException;
import org.dbsyncer.biz.checker.AbstractChecker;
import org.dbsyncer.common.model.DingTalkNoticeChannel;
import org.dbsyncer.common.model.HttpNoticeChannel;
import org.dbsyncer.common.model.MailNoticeChannel;
import org.dbsyncer.common.model.WeChatNoticeChannel;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.parser.SystemConfigProfile;
import org.dbsyncer.parser.model.SystemConfig;
import org.dbsyncer.sdk.model.NoticeConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Map;

/**
 * AnnouncementsConfigureVerifyer
 *
 * @author AE86
 * @version 1.0.0
 * @date 2026-03-04 19:00
 */
@Component
public class NoticeConfigChecker extends AbstractChecker {

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Override
    public SystemConfig checkAddConfigModel(Map<String, String> params) {
        throw new BizException("Unsupported method");
    }

    @Override
    public SystemConfig checkEditConfigModel(Map<String, String> params) {
        printParams(params);
        Assert.notEmpty(params, "Config check params is null.");
        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        Assert.notNull(systemConfig, "Profile is empty.");

        if (systemConfig.getNoticeConfig() == null) {
            systemConfig.setNoticeConfig(new NoticeConfig());
        }
        NoticeConfig noticeConfig = systemConfig.getNoticeConfig();

        // YesNoEnableCorporate WisdomPolice!
        boolean enableWechat = StringUtil.isNotBlank(params.get("enableWechat"));
        WeChatNoticeChannel weChat = noticeConfig.getWechat();
        weChat.setEnabled(enableWechat);
        if (enableWechat) {
            String webhookUrl = StringUtil.trim(params.get("wechatWebhookUrl"));
            Assert.hasText(webhookUrl, "The company's Twitter alert is not empty.");
            weChat.setWebhookUrl(webhookUrl);
        }

        // YesNoEnableNail.Police!
        boolean enableDingTalk = StringUtil.isNotBlank(params.get("enableDingTalk"));
        DingTalkNoticeChannel dingTalk = noticeConfig.getDingTalk();
        dingTalk.setEnabled(enableDingTalk);
        if (enableDingTalk) {
            String webhookUrl = StringUtil.trim(params.get("dingTalkWebhookUrl"));
            Assert.hasText(webhookUrl, "The Consultant's address cannot be empty.");
            dingTalk.setWebhookUrl(webhookUrl);
        }
        // YesNoEnableHTTPPolice!
        boolean enableHttp = StringUtil.isNotBlank(params.get("enableHttp"));
        HttpNoticeChannel http = noticeConfig.getHttp();
        http.setEnabled(enableHttp);
        if (enableHttp) {
            String url = StringUtil.trim(params.get("httpUrl"));
            Assert.hasText(url, "The HTTP address can't be empty.");
            http.setUrl(url);
        }

        // YesNoEnableEmailPolice!
        boolean enableMail = StringUtil.isNotBlank(params.get("enableMail"));
        MailNoticeChannel mail = noticeConfig.getMail();
        mail.setEnabled(enableMail);
        if (enableMail) {
            String account = params.get("mailAccount");
            String code = params.get("mailCode");
            Assert.hasText(account, "The account cannot be empty.");
            Assert.hasText(code, "Code can't be empty.");
            mail.setAccount(account);
            mail.setCode(code);
        }

        return systemConfig;
    }
}
