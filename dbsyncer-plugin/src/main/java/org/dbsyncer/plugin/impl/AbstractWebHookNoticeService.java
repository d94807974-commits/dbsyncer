/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.plugin.impl;

import org.dbsyncer.common.util.DateFormatUtil;
import org.dbsyncer.plugin.model.ConnectorOfflineContent;
import org.dbsyncer.plugin.model.GeneralNoticeContent;
import org.dbsyncer.plugin.model.MappingErrorContent;
import org.dbsyncer.plugin.model.MappingFullCompleteContent;
import org.dbsyncer.plugin.model.MappingStopContent;
import org.dbsyncer.plugin.model.TestNoticeContent;
import org.dbsyncer.sdk.enums.ModelEnum;
import org.dbsyncer.sdk.model.NoticeContent;

/**
 * WebHookNotification service realization base
 *
 * @author AE86
 * @version 1.0.0
 * @date 2026/03/05 22:14
 */
public abstract class AbstractWebHookNoticeService extends AbstractNoticeService {

    protected String buildContent(NoticeContent noticeContent) {
        // RunError
        if (noticeContent instanceof MappingErrorContent) {
            MappingErrorContent meta = (MappingErrorContent) noticeContent;
            StringBuilder c = new StringBuilder();
            c.append(String.format("[% Facilitated]",
                    getAppConfig().getName(), noticeContent.getTitle(), meta.getErrorItems().size()));
            c.append(String.format("%s\n\n", DateFormatUtil.now()));

            for (int i = 0; i < meta.getErrorItems().size(); i++) {
                MappingErrorContent.ErrorItem item = meta.getErrorItems().get(i);
                c.append(String.format("%d. %s (%s) failed: %s, success: %s", i + 1, item.getName(), item.getModel().getName(), item.getFail(), item.getSuccess()));
                if (ModelEnum.FULL == item.getModel()) {
                    c.append(String.format("number", item.getTotal()));
                }
                c.append("\n");
            }
            return c.toString();
        }

        // StopMapping
        if (noticeContent instanceof MappingStopContent) {
            MappingStopContent meta = (MappingStopContent) noticeContent;
            return String.format("[%s] %s %s(%s)", getAppConfig().getName(),
                    noticeContent.getTitle(), meta.getName(), meta.getModel().getName());
        }

        // Full Sync
        if (noticeContent instanceof MappingFullCompleteContent) {
            MappingFullCompleteContent meta = (MappingFullCompleteContent) noticeContent;
            return String.format("[%s] %s (%s) succeeded: %s, failed: %s, total: %s", getAppConfig().getName(),
                    noticeContent.getTitle(), meta.getName(), meta.getModel().getName(),
                    meta.getSuccess(), meta.getFail(), meta.getTotal());
        }

        // Connect Offline
        if (noticeContent instanceof ConnectorOfflineContent) {
            ConnectorOfflineContent meta = (ConnectorOfflineContent) noticeContent;
            StringBuilder c = new StringBuilder();
            c.append(String.format("[%s] %s, connect: %s\n",
                    getAppConfig().getName(), noticeContent.getTitle(), meta.getErrorItems().size()));
            c.append(String.format("%s\n\n", DateFormatUtil.now()));

            for (int i = 0; i < meta.getErrorItems().size(); i++) {
                ConnectorOfflineContent.ErrorItem item = meta.getErrorItems().get(i);
                c.append(String.format("%d. %s(%s), %s", i + 1, item.getName(), item.getType(), item.getUrl()));
                c.append("\n");
            }
            return c.toString();
        }

        // General announcement
        if (noticeContent instanceof GeneralNoticeContent) {
            GeneralNoticeContent meta = (GeneralNoticeContent) noticeContent;
            return String.format("[%s] %s %s", getAppConfig().getName(), noticeContent.getTitle(), meta.getContent());
        }

        // TestAnnouncements
        if (noticeContent instanceof TestNoticeContent) {
            TestNoticeContent meta = (TestNoticeContent) noticeContent;
            return String.format("[%s] %s %s", getAppConfig().getName(), noticeContent.getTitle(), meta.getContent());
        }
        return null;
    }
}
