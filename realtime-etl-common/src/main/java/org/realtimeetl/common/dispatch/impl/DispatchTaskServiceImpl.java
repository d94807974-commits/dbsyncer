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
package org.realtimeetl.common.dispatch.impl;

import org.realtimeetl.common.dispatch.DispatchTask;
import org.realtimeetl.common.dispatch.DispatchTaskService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TasksMovement control achieved
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-06-12 23:45
 */
@Component
public class DispatchTaskServiceImpl implements DispatchTaskService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ThreadPoolTaskExecutor dispatchTaskExecutor;

    private final Map<String, DispatchTask> active = new ConcurrentHashMap<>();

    @Override
    public void execute(DispatchTask task) {
        dispatchTaskExecutor.execute(active.compute(task.getUniqueId(), (k, t)-> {
            if (t != null) {
                t.destroy();
                logger.warn("The dispatch task was terminated, {}", k);
            }
            // Remove by Example, Avoid SameuniqueId ReplaceTasksLaterTasks finally Error NewTasks
            task.onDestroy(dispatchTask -> active.remove(task.getUniqueId(), dispatchTask));
            return task;
        }));
    }

    @Override
    public void stop(String uniqueId) {
        active.computeIfPresent(uniqueId, (k, t)-> {
            t.destroy();
            logger.info("The dispatch task has been stop, {}", k);
            return null;
        });
    }

    @Override
    public boolean isRunning(String uniqueId) {
        return active.containsKey(uniqueId);
    }
}