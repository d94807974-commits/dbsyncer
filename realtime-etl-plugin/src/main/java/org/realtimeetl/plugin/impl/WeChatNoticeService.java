/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.plugin.impl;

import com.alibaba.fastjson2.annotation.JSONField;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;

import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.model.NoticeMessage;
import org.realtimeetl.common.model.WeChatNoticeChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Corporate WisdomNotification of service realization
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026/03/05 22:14
 */
public final class WeChatNoticeService extends AbstractWebHookNoticeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void notify(NoticeMessage noticeMessage) {
        WeChatNoticeChannel wechat = noticeMessage.getNoticeConfig().getWechat();
        if (!wechat.isEnabled()) {
            return;
        }
        NoticeContent noticeContent = noticeMessage.getNoticeContent();
        String c = buildContent(noticeContent);
        if (StringUtil.isBlank(c)) {
            logger.warn("build content is blank, skip send, notice content:{}", noticeContent.getClass().getName());
            return;
        }
        MessageContent params = new MessageContent();
        params.setMsgType("text");
        TextContent content = new TextContent();
        content.setContent(c);

        // @everyone
        if (wechat.isAtAll()) {
            content.setMentionedList(StringUtil.split("@all", StringUtil.COMMA));
        } else {
            // @Someone.Cell phone number
            String atMobiles = wechat.getAtMobiles();
            if (StringUtil.isNotBlank(atMobiles)) {
                content.setMentionedMobileList(StringUtil.split(atMobiles, StringUtil.COMMA));
            }
        }
        params.setContent(content);
        send(wechat.getWebhookUrl(), JsonUtil.objToJson(params));
    }

    static final class MessageContent {
        @JSONField(name = "msgtype")
        private String msgType;

        @JSONField(name = "text")
        private Object content;

        public String getMsgType() {
            return msgType;
        }

        public void setMsgType(String msgType) {
            this.msgType = msgType;
        }

        public Object getContent() {
            return content;
        }

        public void setContent(Object content) {
            this.content = content;
        }
    }

    static final class TextContent {

        @JSONField(name = "content")
        private String content;

        /**
         * AtUserList
         */
        @JSONField(name = "mentioned_list")
        private String[] mentionedList;

        /**
         * AtCell phone numberList
         */
        @JSONField(name = "mentioned_mobile_list")
        private String[] mentionedMobileList;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String[] getMentionedList() {
            return mentionedList;
        }

        public void setMentionedList(String[] mentionedList) {
            this.mentionedList = mentionedList;
        }

        public String[] getMentionedMobileList() {
            return mentionedMobileList;
        }

        public void setMentionedMobileList(String[] mentionedMobileList) {
            this.mentionedMobileList = mentionedMobileList;
        }
    }

}
