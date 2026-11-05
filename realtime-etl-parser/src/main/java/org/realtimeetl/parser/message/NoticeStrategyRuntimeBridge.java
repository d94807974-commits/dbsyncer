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

import org.realtimeetl.common.event.RemoveNoticeStrategyCacheEvent;
import org.realtimeetl.parser.NoticeStrategyProfile;
import org.realtimeetl.sdk.notice.NoticeDecisionEngine;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Policy Table→ Decision fulfillr bridge: independent{@link org.realtimeetl.sdk.notice.MessageService} The chain of dependence,
 * AvoidEngine Direct DependenceProfile Recommendations maintainStartPeriod cycle MISC.
 *
 * @author Realtime ETL
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
