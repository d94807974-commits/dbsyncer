/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.notice;

import org.realtimeetl.sdk.model.NoticeContent;

/**
 * Can not open messageEnabledThe global channel.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-06-16 12:00
 */
public interface NoticeDeliveryHandler {

    /**
     * Send Preventive Notification to GlobalEnabledChannels.
     *
     * @param noticeContent Contents of notice
     */
    void deliver(NoticeContent noticeContent);
}
