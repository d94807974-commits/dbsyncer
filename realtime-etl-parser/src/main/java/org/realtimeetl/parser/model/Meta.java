package org.realtimeetl.parser.model;

import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.common.enums.TaskLevelEnum;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.constant.ConfigConstant;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mandate performance(realtime_etl_meta)。
 * <p>Primary key {@code id} To snowflake,andOperational entitiesID Untie.
 * <p>Association Keys{@code taskId}：TasksLevel{@code isTaskDetail=0}）YesTasks/Mapping ID；
 * Table level{@code isTaskDetail=1}）Yes{@code table_group.id}。
 *
 * <p>DetailsminTable{@code realtime_etl_task_detail_{taskId}} minSnippet UseTasksAdministrationMeta Yes.taskId，Oh, no.Yes Meta Primary key。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/04/21 16:19
 */
public class Meta extends ConfigModel {

    /**
     * AssociationID：TasksLevelTasks/Mapping ID；The scale istable_group.id
     */
    private String taskId;

    /**
     * {@link CommonTaskStatusEnum}
     */
    private int state;

    /**
     * YesNoTasksDetails:0-TasksAdministration1-Details
     */
    private int isTaskDetail;

    private AtomicLong total;
    private AtomicLong success;
    private AtomicLong fail;
    private AtomicLong diff;
    private AtomicLong fixed;
    private Map<String, String> snapshot;
    private long startTime;

    public Meta() {
        super.setType(ConfigConstant.META);
        super.setName(ConfigConstant.META);
        clear();
    }

    /**
     * ResetmetaStatus，No return.Total
     */
    public void reset() {
        init();
    }

    /**
     * RevertStatus
     */
    public void clear() {
        init();
        this.total = new AtomicLong(0);
    }

    private void init() {
        this.state = CommonTaskStatusEnum.READY.getCode();
        this.isTaskDetail = TaskLevelEnum.TASK.getCode();
        this.success = new AtomicLong(0);
        this.fail = new AtomicLong(0);
        this.diff = new AtomicLong(0);
        this.fixed = new AtomicLong(0);
        this.snapshot = new HashMap<>();
        this.startTime = 0L;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getIsTaskDetail() {
        return isTaskDetail;
    }

    public void setIsTaskDetail(int isTaskDetail) {
        this.isTaskDetail = isTaskDetail;
    }

    public boolean isTaskDetail() {
        return isTaskDetail == 1;
    }

    public AtomicLong getTotal() {
        return total;
    }

    public void setTotal(AtomicLong total) {
        this.total = total;
    }

    public AtomicLong getSuccess() {
        return success;
    }

    public void setSuccess(AtomicLong success) {
        this.success = success;
    }

    public AtomicLong getFail() {
        return fail;
    }

    public void setFail(AtomicLong fail) {
        this.fail = fail;
    }

    public AtomicLong getDiff() {
        return diff;
    }

    public void setDiff(AtomicLong diff) {
        this.diff = diff;
    }

    public AtomicLong getFixed() {
        return fixed;
    }

    public void setFixed(AtomicLong fixed) {
        this.fixed = fixed;
    }

    public Map<String, String> getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Map<String, String> snapshot) {
        this.snapshot = snapshot;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
}
