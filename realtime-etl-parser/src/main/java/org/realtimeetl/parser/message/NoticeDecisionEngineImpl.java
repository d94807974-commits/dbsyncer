/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.message;

import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.model.NoticeStrategy;
import org.realtimeetl.sdk.notice.NoticeDecisionEngine;
import org.realtimeetl.sdk.notice.NoticeDeliveryHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * Message Notification Decision Engine: Strategicly matched, immediately sent or queued for waiting periodsflush。
 * <p>
 * No reliance on strategyProfile；Load Substantive{@link NoticeStrategyRuntimeBridge} / OperationsService / PreloadFinish。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-06-16 12:00
 */
@Component
public class NoticeDecisionEngineImpl implements NoticeDecisionEngine {

    private volatile NoticeStrategyRegistry strategyRegistry;

    @Resource
    private StrategyBucketManager strategyBucketManager;

    @Resource
    private NoticeDeliveryHandler noticeDeliveryHandler;

    @Resource
    private NoticeStrategyFlushScheduler noticeStrategyFlushScheduler;

    @Override
    public void dispatch(NoticeContent noticeContent) {
        if (noticeContent == null || noticeContent.getNoticeType() == null) {
            return;
        }
        NoticeStrategyRegistry registry = this.strategyRegistry;
        if (registry == null) {
            return;
        }
        NoticeStrategy strategy = registry.match(noticeContent.getNoticeType());
        if (strategy == null) {
            return;
        }
        if (strategy.isImmediate()) {
            noticeDeliveryHandler.deliver(noticeContent);
        } else {
            strategyBucketManager.offer(strategy, noticeContent);
        }
    }

    @Override
    public synchronized void reload(List<NoticeStrategy> strategies) {
        List<NoticeStrategy> list = strategies == null ? Collections.emptyList() : strategies;
        this.strategyRegistry = new NoticeStrategyRegistry(list);
        noticeStrategyFlushScheduler.reload(list);
    }
}
