/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.plugin.model;


import org.realtimeetl.sdk.enums.NoticeTypeEnum;
import org.realtimeetl.sdk.model.NoticeContent;

/**
 * TestOrganisation
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2026-03-04 19:00
 */
public class TestNoticeContent extends NoticeContent {

    private String content;

    public TestNoticeContent() {
        setNoticeType(NoticeTypeEnum.TEST_MESSAGE);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
