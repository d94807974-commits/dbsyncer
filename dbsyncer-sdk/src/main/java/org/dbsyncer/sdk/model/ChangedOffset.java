package org.dbsyncer.sdk.model;

/**
 * IncrementalOffset
 *
 * @version 1.0.0
 * @author AE86
 * @date 2023-08-23 20:00
 */
public final class ChangedOffset {

    /**
     * MappingID
     */
    private String taskId;

    /**
     * IncrementalFilenameClaims
     */
    private String nextFileName;

    /**
     * IncrementalOffset
     */
    private Object position;

    /**
     * SynchronizationNo.
     */
    private long seq;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getNextFileName() {
        return nextFileName;
    }

    public void setNextFileName(String nextFileName) {
        this.nextFileName = nextFileName;
    }

    public Object getPosition() {
        return position;
    }

    public void setPosition(Object position) {
        this.position = position;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }
}
