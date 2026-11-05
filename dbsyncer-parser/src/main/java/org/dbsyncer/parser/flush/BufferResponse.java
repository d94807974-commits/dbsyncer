package org.dbsyncer.parser.flush;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2022/3/27 18:11
 */
public interface BufferResponse {

    /**
     * Acquisition of batches
     */
    int getTaskSize();

    /**
     * Fetch SuffixName
     */
    String getSuffixName();

    /**
     * ClearBatch processing data
     */
    void clearDataList();
}
