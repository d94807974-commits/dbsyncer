package org.dbsyncer.biz.model;

public class MappingReportMetric {

    /**
     * ProcessedSuccesses
     */
    private long success;

    /**
     * ProcessedFailures
     */
    private long fail;

    /**
     * InsertEvent
     */
    private long insert;

    /**
     * UpdateEvent
     */
    private long update;

    /**
     * DeleteEvent
     */
    private long delete;

    /**
     * ddlEvent
     */
    private long ddl;

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getFail() {
        return fail;
    }

    public void setFail(long fail) {
        this.fail = fail;
    }

    public long getInsert() {
        return insert;
    }

    public void setInsert(long insert) {
        this.insert = insert;
    }

    public long getUpdate() {
        return update;
    }

    public void setUpdate(long update) {
        this.update = update;
    }

    public long getDelete() {
        return delete;
    }

    public void setDelete(long delete) {
        this.delete = delete;
    }

    public long getDdl() {
        return ddl;
    }

    public void setDdl(long ddl) {
        this.ddl = ddl;
    }

    public void reset() {
        this.success = 0;
        this.fail = 0;
        this.insert = 0;
        this.update = 0;
        this.delete = 0;
        this.ddl = 0;
    }
}
