/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.plugin.model;

import org.realtimeetl.sdk.model.NoticeContent;

/**
 * General Notification Message
 *
 * @Author wuji
 * @Version 1.0.0
 * @Date 2026-06-08 00:00
 */
public class GeneralNoticeContent extends NoticeContent {

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
