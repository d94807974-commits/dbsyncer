/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.model;


import org.realtimeetl.sdk.enums.NoticeTypeEnum;

/**
 * Organisation
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2026-03-04 19:00
 */
public abstract class NoticeContent {

    private NoticeTypeEnum noticeType;

    private String title;

    /**
     * Resource type（Likemapping），For parsing@ OverwriteRule
     */
    private String resourceType;

    /**
     * Resource ID（LikeSync task ID）
     */
    private String resourceId;

    public NoticeTypeEnum getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(NoticeTypeEnum noticeType) {
        this.noticeType = noticeType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
}
