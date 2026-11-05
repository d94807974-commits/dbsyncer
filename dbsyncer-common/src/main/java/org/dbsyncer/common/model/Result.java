package org.dbsyncer.common.model;

import java.util.LinkedList;
import java.util.List;

public class Result<T> {

    /**
     * SuccessesAccording to
     */
    private final List<T> successData = new LinkedList<>();

    /**
     * ErrorData Fact
     */
    private final List<T> failData = new LinkedList<>();

    /**
     * ErrorLog
     */
    private final StringBuffer error = new StringBuffer();

    /**
     * MappingMap relationID
     */
    private String tableGroupId;

    /**
     * Target list name
     */
    private String targetTableGroupName;

    /**
     * Sync taskID
     */
    private String taskId;

    /**
     * Event
     */
    private String event;

    private final Object LOCK = new Object();

    public Result() {
    }

    public Result(List<T> data) {
        this.successData.addAll(data);
    }

    public List<T> getSuccessData() {
        return successData;
    }

    public List<T> getFailData() {
        return failData;
    }

    public StringBuffer getError() {
        return error;
    }

    /**
     * ThreadsClear.AddGather!
     */
    public void addFailData(List failData) {
        synchronized (LOCK) {
            this.failData.addAll(failData);
        }
    }

    /**
     * ThreadsClear.AddGather!
     */
    public void addSuccessData(List successData) {
        synchronized (LOCK) {
            this.successData.addAll(successData);
        }
    }

    public String getTableGroupId() {
        return tableGroupId;
    }

    public void setTableGroupId(String tableGroupId) {
        this.tableGroupId = tableGroupId;
    }

    public String getTargetTableGroupName() {
        return targetTableGroupName;
    }

    public void setTargetTableGroupName(String targetTableGroupName) {
        this.targetTableGroupName = targetTableGroupName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
