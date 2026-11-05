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

import org.realtimeetl.common.scheduled.ScheduledTaskService;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.sdk.model.NoticeStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Periodic policyCron Schedule: to pointflush Buffer queue and send article by article.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-06-17 12:00
 */
@Component
public class NoticeStrategyFlushScheduler {

    private static final String TASK_KEY_PREFIX = "notice:strategy:";

    @Resource
    private ScheduledTaskService scheduledTaskService;

    @Resource
    private StrategyBucketManager strategyBucketManager;

    private final Set<String> registeredIds = new HashSet<>();

    /**
     * By PolicyListReconstruction cycleTasks。
     */
    public synchronized void reload(List<NoticeStrategy> strategies) {
        for (String id : registeredIds) {
            scheduledTaskService.stop(TASK_KEY_PREFIX + id);
        }
        registeredIds.clear();
        strategyBucketManager.clear();
        if (CollectionUtils.isEmpty(strategies)) {
            return;
        }
        for (NoticeStrategy strategy : strategies) {
            register(strategy);
        }
    }

    public void register(NoticeStrategy strategy) {
        if (strategy == null || !strategy.hasCron()) {
            return;
        }
        strategyBucketManager.register(strategy);
        String taskKey = TASK_KEY_PREFIX + strategy.getId();
        scheduledTaskService.start(taskKey, strategy.getCron(),
                () -> strategyBucketManager.flush(strategy.getId()));
        registeredIds.add(strategy.getId());
    }
}
