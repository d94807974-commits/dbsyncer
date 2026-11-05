/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.model;

import org.dbsyncer.parser.flush.BufferRequest;

import java.util.Map;

/**
 * DetailsminThe table contains the request.
 * <p>{@code taskDetailShardId} YesTasks ID，Correspond{@code dbsyncer_task_detail_{taskId}}；
 * {@link #getTaskId()} YesBufferRequest It's a different name.BackThe same.minSnips.
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022/3/27 16:57
 */
public class StorageRequest implements BufferRequest {

    /**
     * DetailsminTableminSnippetsTasks ID）
     */
    private final String taskDetailShardId;

    private final Map row;

    public StorageRequest(String taskDetailShardId, Map row) {
        this.taskDetailShardId = taskDetailShardId;
        this.row = row;
    }

    /**
     * DetailsminTableminSnippetsTasks ID）。
     */
    public String getTaskDetailShardId() {
        return taskDetailShardId;
    }

    public Map getRow() {
        return row;
    }

    @Override
    public String getTaskId() {
        return taskDetailShardId;
    }
}
