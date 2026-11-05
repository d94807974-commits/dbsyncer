/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.biz.impl;

import org.dbsyncer.common.enums.CommonTaskStatusEnum;
import org.dbsyncer.parser.TaskMetaProfile;
import org.dbsyncer.parser.model.Meta;
import org.dbsyncer.sdk.model.NoticeContent;
import org.dbsyncer.sdk.notice.MessageService;
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