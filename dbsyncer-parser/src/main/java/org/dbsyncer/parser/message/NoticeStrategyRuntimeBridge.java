/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.message;

import org.dbsyncer.common.event.RemoveNoticeStrategyCacheEvent;
import org.dbsyncer.parser.NoticeStrategyProfile;
import org.dbsyncer.sdk.notice.NoticeDecisionEngine;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Policy Table→ Decision fulfillr bridge: independent{@link org.dbsyncer.sdk.notice.MessageService} The chain of dependence,
 * AvoidEngine Direct DependenceProfile Recommendations maintainStartPeriod cycle MISC.
 *
 * @author wuji
 * @version 1.0.0
 */
@Component
public class NoticeStrategyRuntimeBridge implements ApplicationListener<RemoveNoticeStrategyCacheEvent> {

    @Resource
    private NoticeStrategyProfile noticeStrategyProfile;

    @Resource
    private NoticeDecisionEngine noticeDecisionEngine;

    /**
     * Load from policy sheetRefreshEngine runs the state.
     */
    public void reloadFromStore() {
        noticeDecisionEngine.reload(
                NoticeStrategyConverter.toRuntime(NoticeStrategyConverter.toDefs(noticeStrategyProfile.listAll())));
    }

    @Override
    public void onApplicationEvent(RemoveNoticeStrategyCacheEvent event) {
        reloadFromStore();
    }
}
