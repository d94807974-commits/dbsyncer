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
import org.realtimeetl.common.model.DingTalkNoticeChannel;
import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.model.NoticeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Nail.Notification of service realization
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026/03/05 22:14
 */
public final class DingTalkNoticeService extends AbstractWebHookNoticeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void notify(NoticeMessage noticeMessage) {
        DingTalkNoticeChannel dingTalk = noticeMessage.getNoticeConfig().getDingTalk();
        if (!dingTalk.isEnabled()) {
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
        At at = new At();
        // @everyone
        if (dingTalk.isAtAll()) {
            at.setAtAll(true);
        } else {
            // @Someone.Cell phone number
            String atMobiles = dingTalk.getAtMobiles();
            if (StringUtil.isNotBlank(atMobiles)) {
                at.setAtMobiles(StringUtil.split(atMobiles, StringUtil.COMMA));
            }
        }
        params.setAt(at);
        TextContent content = new TextContent();
        content.setContent(c);
        params.setContent(content);
        send(dingTalk.getWebhookUrl(), JsonUtil.objToJson(params));
    }

    static final class MessageContent {
        @JSONField(name = "msgtype")
        private String msgType;

        @JSONField(name = "text")
        private Object content;

        /**
         * @User Configuration
         */
        private At at;

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

        public At getAt() {
            return at;
        }

        public void setAt(At at) {
            this.at = at;
        }
    }

    static final class At {

        /**
         * AtCell phone numberList
         */
        @JSONField(name = "atMobiles")
        private String[] atMobiles;

        /**
         * AtEveryone.
         */
        @JSONField(name = "isAtAll")
        private boolean isAtAll;

        public String[] getAtMobiles() {
            return atMobiles;
        }

        public void setAtMobiles(String[] atMobiles) {
            this.atMobiles = atMobiles;
        }

        public boolean isAtAll() {
            return isAtAll;
        }

        public void setAtAll(boolean atAll) {
            isAtAll = atAll;
        }
    }

    static final class TextContent {

        @JSONField(name = "content")
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

    }

}
