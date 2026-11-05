/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.model;

import org.dbsyncer.sdk.constant.ConfigConstant;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta Count Atoms/29IncrementalParameter
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-24 10:42
 */
public class MetaIncrement {

    /**
     * MetadataID
     */
    private String taskId;
    /**
     * TotalIncremental
     */
    private long totalDelta;
    /**
     * SuccessesVolumeIncremental
     */
    private long successDelta;
    /**
     * FailuresVolumeIncremental
     */
    private long failDelta;
    /**
     * DiffsVolumeIncremental
     */
    private long diffDelta;
    /**
     * Number of repairsIncremental
     */
    private long fixedDelta;

    public static MetaIncrement of(String taskId) {
        MetaIncrement increment = new MetaIncrement();
        increment.taskId = taskId;
        return increment;
    }

    public MetaIncrement total(long totalDelta) {
        this.totalDelta = totalDelta;
        return this;
    }

    public MetaIncrement success(long successDelta) {
        this.successDelta = successDelta;
        return this;
    }

    public MetaIncrement fail(long failDelta) {
        this.failDelta = failDelta;
        return this;
    }

    public MetaIncrement diff(long diffDelta) {
        this.diffDelta = diffDelta;
        return this;
    }

    public MetaIncrement fixed(long fixedDelta) {
        this.fixedDelta = fixedDelta;
        return this;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getTotalDelta() {
        return totalDelta;
    }

    public void setTotalDelta(long totalDelta) {
        this.totalDelta = totalDelta;
    }

    public long getSuccessDelta() {
        return successDelta;
    }

    public void setSuccessDelta(long successDelta) {
        this.successDelta = successDelta;
    }

    public long getFailDelta() {
        return failDelta;
    }

    public void setFailDelta(long failDelta) {
        this.failDelta = failDelta;
    }

    public long getDiffDelta() {
        return diffDelta;
    }

    public void setDiffDelta(long diffDelta) {
        this.diffDelta = diffDelta;
    }

    public long getFixedDelta() {
        return fixedDelta;
    }

    public void setFixedDelta(long fixedDelta) {
        this.fixedDelta = fixedDelta;
    }

    /**
     * Convert to StorageIncremental Map：key and {@link ConfigConstant} Meta Unanimously listed (total/success/fail/diff/fixed），ValueYes0 item is not included in the table.
     *
     * @return Non-emptyIncremental；For All0 TimeBackEmptyMap
     */
    public Map<String, Long> toDeltaMap() {
        Map<String, Long> deltas = new HashMap<>(5);
        putIfNonZero(deltas, ConfigConstant.META_TOTAL, totalDelta);
        putIfNonZero(deltas, ConfigConstant.META_SUCCESS, successDelta);
        putIfNonZero(deltas, ConfigConstant.META_FAIL, failDelta);
        putIfNonZero(deltas, ConfigConstant.META_DIFF, diffDelta);
        putIfNonZero(deltas, ConfigConstant.META_FIXED, fixedDelta);
        return deltas.isEmpty() ? Collections.emptyMap() : deltas;
    }

    private static void putIfNonZero(Map<String, Long> deltas, String key, long value) {
        if (value != 0L) {
            deltas.put(key, value);
        }
    }
}
