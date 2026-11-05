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
package org.realtimeetl.biz.impl;

import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.notice.MessageService;
import org.springframework.util.Assert;

import javax.annotation.Resource;

public class BaseServiceImpl {

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private MessageService messageService;

    /**
     * MappingTurn off the lock.
     */
    protected static final Object LOCK = new Object();

    protected boolean isRunning(String taskId) {
        Meta meta = taskMetaProfile.getMeta(taskId);
        if (null != meta) {
            int state = meta.getState();
            return CommonTaskStatusEnum.isRunning(state);
        }
        return false;
    }

    protected void assertRunning(String taskId) {
        synchronized (LOCK) {
            Assert.isTrue(!isRunning(taskId), "The task is running, please stop.");
        }
    }

    /**
     * Organisation
     */
    protected void sendNotifyMessage(NoticeContent content) {
        messageService.sendMessage(content);
    }

}