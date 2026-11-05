/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.model;

import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.sdk.constant.ConfigConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Library Migration TaskConfigure（OnlyEnduringConfigurePresent.{@code dbsyncer_task.JSON}）。
 * <p>LibraryMap LightListWritingtask.JSON（excluding tables;form map storage{@code dbsyncer_table_group}。
 * Run progressandCurrent roundFinishIt's always there.{@code dbsyncer_meta}（TasksAdministrationSTATE/Time/LibrarySummary+ DetailsSNAPSHOT）。</p>
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-05-22 11:19
 */
public class DatabaseSyncTask extends ConfigModel {

    public DatabaseSyncTask() {
        super.setType(ConfigConstant.DATABASE_SYNC);
    }

    /**
     * YesNoCopy table schema
     */
    private boolean enableCopySchema;
    /**
     * TableStructureOverwrite?（Objective Combined(when existing)
     */
    private boolean overwriteSchema;
    /**
     * YesNoSync data
     */
    private boolean enableCopyData;
    /**
     * Data FactOverwrite?（Objective Combined(when existing)
     */
    private boolean overwriteData;

    /**
     * LibraryLevel MapEnduringPresent.task.JSON，does not containtableMappings）
     */
    private List<DatabaseMapping> databaseMappings = new ArrayList<>();

    /**
     * minPageRead the number of bars
     */
    private int readNum = 10000;

    /**
     * Single writeNumber of bars
     */
    private int batchNum = 1000;

    /**
     * TableLevelParallelThreads（set aside,andData validation threadNum Unanimously)
     */
    private int threadNum = 5;

    public boolean isEnableCopySchema() {
        return enableCopySchema;
    }

    public void setEnableCopySchema(boolean enableCopySchema) {
        this.enableCopySchema = enableCopySchema;
    }

    public boolean isOverwriteSchema() {
        return overwriteSchema;
    }

    public void setOverwriteSchema(boolean overwriteSchema) {
        this.overwriteSchema = overwriteSchema;
    }

    public boolean isEnableCopyData() {
        return enableCopyData;
    }

    public void setEnableCopyData(boolean enableCopyData) {
        this.enableCopyData = enableCopyData;
    }

    public boolean isOverwriteData() {
        return overwriteData;
    }

    public void setOverwriteData(boolean overwriteData) {
        this.overwriteData = overwriteData;
    }

    public List<DatabaseMapping> getDatabaseMappings() {
        return databaseMappings;
    }

    public void setDatabaseMappings(List<DatabaseMapping> databaseMappings) {
        this.databaseMappings = databaseMappings == null ? new ArrayList<>() : databaseMappings;
    }

    public int getReadNum() {
        return readNum;
    }

    public void setReadNum(int readNum) {
        this.readNum = readNum;
    }

    public int getBatchNum() {
        return batchNum;
    }

    public void setBatchNum(int batchNum) {
        this.batchNum = batchNum;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }
}
