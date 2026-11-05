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
package org.realtimeetl.sdk.model;

import org.realtimeetl.common.model.DingTalkNoticeChannel;
import org.realtimeetl.common.model.HttpNoticeChannel;
import org.realtimeetl.common.model.MailNoticeChannel;
import org.realtimeetl.common.model.WeChatNoticeChannel;

/**
 * AnnouncementsConfigure
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2026-03-04 19:00
 */
public class NoticeConfig {

    private WeChatNoticeChannel wechat = new WeChatNoticeChannel();
    private DingTalkNoticeChannel dingTalk = new DingTalkNoticeChannel();
    private HttpNoticeChannel http = new HttpNoticeChannel();
    private MailNoticeChannel mail = new MailNoticeChannel();

    /**
     * Sync taskYes.FailuresAccording to
     */
    private boolean enableMappingError = true;
    /**
     * StopMapping
     */
    private boolean enableMappingStop = true;
    /**
     * Connect Offline
     */
    private boolean enableConnectorOffline = true;
    /**
     * Revision verification failed
     */
    private boolean enableValidateSyncFail = true;
    /**
     * System Message
     */
    private boolean enableSystemMessage = true;

    public org.realtimeetl.common.model.WeChatNoticeChannel getWechat() {
        return wechat;
    }

    public void setWechat(org.realtimeetl.common.model.WeChatNoticeChannel wechat) {
        this.wechat = wechat;
    }

    public DingTalkNoticeChannel getDingTalk() {
        return dingTalk;
    }

    public void setDingTalk(DingTalkNoticeChannel dingTalk) {
        this.dingTalk = dingTalk;
    }

    public HttpNoticeChannel getHttp() {
        return http;
    }

    public void setHttp(HttpNoticeChannel http) {
        this.http = http;
    }

    public MailNoticeChannel getMail() {
        return mail;
    }

    public void setMail(MailNoticeChannel mail) {
        this.mail = mail;
    }

    public boolean isEnableMappingError() {
        return enableMappingError;
    }

    public void setEnableMappingError(boolean enableMappingError) {
        this.enableMappingError = enableMappingError;
    }

    public boolean isEnableMappingStop() {
        return enableMappingStop;
    }

    public void setEnableMappingStop(boolean enableMappingStop) {
        this.enableMappingStop = enableMappingStop;
    }

    public boolean isEnableConnectorOffline() {
        return enableConnectorOffline;
    }

    public void setEnableConnectorOffline(boolean enableConnectorOffline) {
        this.enableConnectorOffline = enableConnectorOffline;
    }

    public boolean isEnableValidateSyncFail() {
        return enableValidateSyncFail;
    }

    public void setEnableValidateSyncFail(boolean enableValidateSyncFail) {
        this.enableValidateSyncFail = enableValidateSyncFail;
    }

    public boolean isEnableSystemMessage() {
        return enableSystemMessage;
    }

    public void setEnableSystemMessage(boolean enableSystemMessage) {
        this.enableSystemMessage = enableSystemMessage;
    }
}
