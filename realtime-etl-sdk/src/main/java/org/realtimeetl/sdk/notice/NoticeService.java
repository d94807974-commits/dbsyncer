/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.notice;


import org.realtimeetl.sdk.model.NoticeMessage;

public interface NoticeService {

    /**
     * Organisation
     */
    void notify(NoticeMessage notificationMessage);

}
