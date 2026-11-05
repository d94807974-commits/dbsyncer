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
package org.realtimeetl.parser.model;

import org.realtimeetl.parser.flush.BufferRequest;

import java.util.Map;

/**
 * DetailsminThe table contains the request.
 * <p>{@code taskDetailShardId} YesTasks ID，Correspond{@code realtime_etl_task_detail_{taskId}}；
 * {@link #getTaskId()} YesBufferRequest It's a different name.BackThe same.minSnips.
 *
 * @author Realtime ETL
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
