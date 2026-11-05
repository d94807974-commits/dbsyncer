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
package org.realtimeetl.common.model;

/**
 * The robot alert channel.Corporate Wisdom / Nail.Wait:webhook；From Sending@ ByRule on notificationOverwriteWrite.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-23
 */
public abstract class RobotNoticeChannel extends NoticeChannel {

    /**
     * Robot.webhook Address
     */
    private String webhookUrl;

    /**
     * YesNo @everyone
     */
    private boolean atAll;

    /**
     * @UserCell phone number，Multiple commasminBreak
     */
    private String atMobiles;

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public boolean isAtAll() {
        return atAll;
    }

    public void setAtAll(boolean atAll) {
        this.atAll = atAll;
    }

    public String getAtMobiles() {
        return atMobiles;
    }

    public void setAtMobiles(String atMobiles) {
        this.atMobiles = atMobiles;
    }
}
