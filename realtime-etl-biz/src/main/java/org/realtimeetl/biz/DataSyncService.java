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
package org.realtimeetl.biz;

import com.google.protobuf.InvalidProtocolBufferException;
import org.realtimeetl.biz.model.DataSyncRequest;
import org.realtimeetl.biz.vo.MessageVO;

import java.util.Map;

public interface DataSyncService {

    /**
     * AccessSync data
     */
    MessageVO getMessageVo(String taskId, String messageId);

    /**
     * AccessBinlog
     */
    Map getBinlogData(Map row, boolean prettyBytes) throws InvalidProtocolBufferException;

    /**
     * ManualSync data
     */
    String sync(Map<String, String> params) throws InvalidProtocolBufferException;

    /**
     * BatchSync data
     */
    void syncBatch(DataSyncRequest request);

}