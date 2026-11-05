/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.message;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.enums.NoticeTypeEnum;
import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.model.NoticeStrategy;
import org.realtimeetl.sdk.notice.NoticeDeliveryHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Periodical policy to be sent in queue: weight with the message in the policy barrel.flush .
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-06-17 12:00
 */
@Component
public class StrategyBucketManager {

    private final Map<String, Map<String, NoticeContent>> buckets = new ConcurrentHashMap<>();

    @Resource
    private NoticeDeliveryHandler noticeDeliveryHandler;

    public void register(NoticeStrategy strategy) {
        if (strategy != null && strategy.isInterval()) {
            buckets.computeIfAbsent(strategy.getId(),
                    key -> Collections.synchronizedMap(new LinkedHashMap<>()));
        }
    }

    /**
     * ClearAllPolicy barrel(s)Configure reload .
     */
    public void clear() {
        buckets.clear();
    }

    public void offer(NoticeStrategy strategy, NoticeContent content) {
        if (strategy == null || content == null || !strategy.isInterval()) {
            return;
        }
        String dedupeKey = resolve(content);
        if (StringUtil.isBlank(dedupeKey)) {
            return;
        }
        Map<String, NoticeContent> buffer = buckets.get(strategy.getId());
        if (buffer == null) {
            return;
        }
        synchronized (buffer) {
            buffer.putIfAbsent(dedupeKey, content);
        }
    }

    public void flush(String strategyId) {
        List<NoticeContent> snapshot = drain(strategyId);
        for (NoticeContent content : snapshot) {
            noticeDeliveryHandler.deliver(content);
        }
    }

    private List<NoticeContent> drain(String strategyId) {
        Map<String, NoticeContent> buffer = buckets.get(strategyId);
        if (buffer == null) {
            return Collections.emptyList();
        }
        synchronized (buffer) {
            if (buffer.isEmpty()) {
                return Collections.emptyList();
            }
            List<NoticeContent> snapshot = new ArrayList<>(buffer.values());
            buffer.clear();
            return snapshot;
        }
    }

    public static String resolve(NoticeContent content) {
        if (content == null) {
            return StringUtil.EMPTY;
        }
        NoticeTypeEnum noticeType = content.getNoticeType();
        String type = noticeType == null ? StringUtil.EMPTY : noticeType.name();
        String title = content.getTitle() == null ? StringUtil.EMPTY : content.getTitle();
        String resourceId = content.getResourceId() == null ? StringUtil.EMPTY : content.getResourceId();
        return type + '：' + title + '：' + resourceId;
    }
}
