/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.model;

/**
 * FullSync taskRun the state.
 * Father.TasksIn charge of the suspension;Tasks（By Table) Carrying Independent Curves,Delegated fatherTasks。
 */
public class Task {

    private String id;

    private StateEnum state;

    private int tableGroupIndex;

    private int pageIndex;

    private Object[] cursors;

    /**
     * End cursor (include); empty means read toNoneData to date
     */
    private Object[] endCursors;

    /**
     * YesNoSkip Table ProgressRefreshEvent
     */
    private boolean skipTableProgressEvent;

    /**
     * Father.Tasks（FormTasksNon-empty)
     */
    private Task parent;

    /**
     * Current MapID（FormTasksNon-empty)
     */
    private String tableGroupId;

    public Task(String id) {
        this.id = id;
        this.state = StateEnum.RUNNING;
    }

    /**
     * Create TableTasks：TotalGet out of here, stand alone.
     *
     * @param tableGroupId MapID
     * @return SonTasks
     */
    public Task createTableTask(String tableGroupId) {
        Task child = new Task(this.id);
        child.parent = this;
        child.tableGroupId = tableGroupId;
        child.pageIndex = 1;
        return child;
    }

    public void stop() {
        this.state = StateEnum.STOP;
    }

    public boolean isRunning() {
        if (parent != null) {
            return parent.isRunning();
        }
        return StateEnum.RUNNING == state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getTableGroupIndex() {
        return tableGroupIndex;
    }

    public void setTableGroupIndex(int tableGroupIndex) {
        this.tableGroupIndex = tableGroupIndex;
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Object[] getCursors() {
        return cursors;
    }

    public void setCursors(Object[] cursors) {
        this.cursors = cursors;
    }

    public Object[] getEndCursors() {
        return endCursors;
    }

    public void setEndCursors(Object[] endCursors) {
        this.endCursors = endCursors;
    }

    public boolean isSkipTableProgressEvent() {
        return skipTableProgressEvent;
    }

    public void setSkipTableProgressEvent(boolean skipTableProgressEvent) {
        this.skipTableProgressEvent = skipTableProgressEvent;
    }

    public Task getParent() {
        return parent;
    }

    public String getTableGroupId() {
        return tableGroupId;
    }

    public void setTableGroupId(String tableGroupId) {
        this.tableGroupId = tableGroupId;
    }

    public enum StateEnum {
        /**
         * Run
         */
        RUNNING,
        /**
         * Stop
         */
        STOP;
    }
}
