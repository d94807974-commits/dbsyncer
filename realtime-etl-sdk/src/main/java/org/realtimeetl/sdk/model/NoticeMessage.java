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



import java.util.List;

/**
 * Organisation
 *
 * @author Realtime ETL
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
