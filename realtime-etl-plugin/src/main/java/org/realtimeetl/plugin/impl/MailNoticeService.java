package org.realtimeetl.plugin.impl;

import com.sun.mail.util.MailSSLSocketFactory;
import org.realtimeetl.common.config.AppConfig;
import org.realtimeetl.common.model.MailNoticeChannel;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.plugin.model.ConnectorOfflineContent;
import org.realtimeetl.plugin.model.GeneralNoticeContent;
import org.realtimeetl.plugin.model.MappingErrorContent;
import org.realtimeetl.plugin.model.MappingFullCompleteContent;
import org.realtimeetl.plugin.model.MappingStopContent;
import org.realtimeetl.plugin.model.TestNoticeContent;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.model.NoticeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Properties;

/**
 * EmailNotification of service realization
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2022/11/13 22:20
 */
public final class MailNoticeService extends AbstractNoticeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * MailboxAccount
     */
    private String username;

    /**
     * AuthorizationCode
     */
    private String password;

    /**
     * MailboxSession
     */
    private Session session;

    public void build() throws GeneralSecurityException {
        final Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.example.com");
        props.put("mail.user", username);
        props.put("mail.password", password);
        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.socketFactory", sf);
        // ICALLYAuthorizationInfo，For implementationSMTPOrganisation
        session = Session.getInstance(props, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public void notify(NoticeMessage notice) {
        MailNoticeChannel mail = notice.getNoticeConfig().getMail();
        if (!mail.isEnabled()) {
            return;
        }
        try {
            checkMail(notice);
            // Uniform application title
            AppConfig appConfig = getAppConfig();
            NoticeContent noticeContent = notice.getNoticeContent();
            String title = String.format("[%s] %s", appConfig.getName(), noticeContent.getTitle());
            String content = createTemplate(appConfig.getName(), buildContent(noticeContent));
            if (StringUtil.isBlank(content)) {
                logger.warn("simple mail send content is blank, skip send, notice content:{}", noticeContent.getClass().getName());
                return;
            }

            // CreateEmailMessage
            MimeMessage message = new MimeMessage(session);
            // SettingsFrom
            message.setFrom(new InternetAddress(username));

            // Recipient
            List<String> messageReceivers = notice.getReceivers();
            int size = messageReceivers.size();
            InternetAddress[] addresses = new InternetAddress[size];
            for (int i = 0; i < size; i++) {
                addresses[i] = new InternetAddress(messageReceivers.get(i));
            }
            message.setRecipients(Message.RecipientType.TO, addresses);

            // SettingsEmailTitle
            message.setSubject(title);

            // SettingsEmailContents Body
            message.setContent(content, "text/html;charset=UTF-8");
            // SendEmail
            Transport.send(message);
            logger.info("simple mail send success");
        } catch (Exception e) {
            logger.error("simple mail send error!", e);
        }
    }

    private String buildContent(NoticeContent noticeContent) {
        // RunError
        if (noticeContent instanceof MappingErrorContent) {
            return getMappingErrorContent((MappingErrorContent) noticeContent);
        }

        // StopMapping
        if (noticeContent instanceof MappingStopContent) {
            MappingStopContent meta = (MappingStopContent) noticeContent;
            return String.format("Driver: %s (%s)", meta.getName(), meta.getModel().getName());
        }

        // Full Sync
        if (noticeContent instanceof MappingFullCompleteContent) {
            MappingFullCompleteContent meta = (MappingFullCompleteContent) noticeContent;
            return String.format("Driver: %s (%s), succeeded: %s, failed: %s, total: %s",
                    meta.getName(), meta.getModel().getName(), meta.getSuccess(), meta.getFail(), meta.getTotal());
        }

        // Connect Offline
        if (noticeContent instanceof ConnectorOfflineContent) {
            return getConnectorOfflineContent((ConnectorOfflineContent) noticeContent);
        }

        // General announcement
        if (noticeContent instanceof GeneralNoticeContent) {
            GeneralNoticeContent meta = (GeneralNoticeContent) noticeContent;
            return meta.getContent();
        }

        // TestAnnouncements
        if (noticeContent instanceof TestNoticeContent) {
            TestNoticeContent meta = (TestNoticeContent) noticeContent;
            return meta.getContent();
        }
        return null;
    }

    private String getConnectorOfflineContent(ConnectorOfflineContent noticeContent) {
        StringBuilder c = new StringBuilder();
        for (int i = 0; i < noticeContent.getErrorItems().size(); i++) {
            ConnectorOfflineContent.ErrorItem item = noticeContent.getErrorItems().get(i);
            c.append("<p>");
            c.append(String.format("%d. %s(%s), %s", i+1, item.getName(), item.getType(), item.getUrl()));
            c.append("<p>");
        }
        return c.toString();
    }

    private static String getMappingErrorContent(MappingErrorContent noticeContent) {
        StringBuilder c = new StringBuilder();
        for (int i = 0; i < noticeContent.getErrorItems().size(); i++) {
            MappingErrorContent.ErrorItem item = noticeContent.getErrorItems().get(i);
            c.append("<p>");
            c.append(String.format("%d. %s (%s) failed: %s, success: %s", i+1, item.getName(), item.getModel().getName(), item.getFail(), item.getSuccess()));
            if (ModelEnum.FULL == item.getModel()) {
                c.append(String.format(", Total: %s", item.getTotal()));
            }
            c.append("<p>");
        }
        return c.toString();
    }

    private String createTemplate(String appName, String content) {
        String temp = "<!DOCTYPE html>\n" + "<html lang=\"en\">\n" + "<meta charset=\"UTF-8\">\n" + "<title>${appName} Notification</title>\n" + "</head>\n" + "<body>\n" + "${content}\n" + "\n" + "</body>\n" + "</html>";
        String replace = StringUtil.replace(temp, "${appName}", appName);
        replace = StringUtil.replace(replace, "${content}", content);
        return replace;
    }

    private void checkMail(NoticeMessage notice) {
        Assert.notNull(notice, "Notification request cannot be empty");
        Assert.notNull(notice.getNoticeContent(), "Notification request information cannot be empty");
        Assert.notNull(notice.getNoticeContent().getTitle(), "Mail theme cannot be empty");
        Assert.notEmpty(notice.getReceivers(), "Mail recipient cannot be empty");
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}