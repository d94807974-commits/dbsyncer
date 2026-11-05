/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.biz;

import com.google.protobuf.InvalidProtocolBufferException;
import org.dbsyncer.biz.model.DataSyncRequest;
import org.dbsyncer.biz.vo.MessageVO;

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