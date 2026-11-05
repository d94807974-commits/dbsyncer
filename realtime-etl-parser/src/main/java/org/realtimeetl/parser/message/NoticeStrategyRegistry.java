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
package org.realtimeetl.parser.message;

import org.realtimeetl.sdk.enums.NoticeTypeEnum;
import org.realtimeetl.sdk.model.NoticeStrategy;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * EventTypeandStrategy one-on-one registration form.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-06-16 12:00
 */
public final class NoticeStrategyRegistry {

    private final Map<NoticeTypeEnum, NoticeStrategy> strategyMap = new EnumMap<>(NoticeTypeEnum.class);

    public NoticeStrategyRegistry(List<NoticeStrategy> strategies) {
        if (strategies == null) {
            return;
        }
        for (NoticeStrategy strategy : strategies) {
            if (strategy == null || strategy.getNoticeType() == null) {
                continue;
            }
            strategyMap.putIfAbsent(strategy.getNoticeType(), strategy);
        }
    }

    /**
     * By NotificationTypematching only policy;noConfigureBack {@code null}。
     */
    public NoticeStrategy match(NoticeTypeEnum noticeType) {
        if (noticeType == null) {
            return null;
        }
        return strategyMap.get(noticeType);
    }
}
