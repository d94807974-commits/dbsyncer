/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.model;

import org.realtimeetl.common.enums.CommonTaskTriggerEnum;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.constant.ConfigConstant;

import java.util.ArrayList;
import java.util.List;

/**
 * Revised verification tasksConfigure（OnlyEnduringConfigurePresent.{@code realtime_etl_task.JSON}）。
 * <p>Map Storage{@code realtime_etl_table_group}；Run progressandCurrent roundFinishThe state{@code realtime_etl_meta}。</p>
 * @author wuji
 */
public class ValidateSyncTask extends ConfigModel {

    public ValidateSyncTask() {
        super.setType(ConfigConstant.VALIDATE_SYNC);
    }

    // SourceConnectorID
    private String sourceConnectorId;

    // SourceLibraryName
    private String sourceDatabase;

    // SourceLibraryFrame Name
    private String sourceSchema;

    // SourceLibraryTableList
    private List<Table> sourceTable;

    // TargetConnectorID
    private String targetConnectorId;

    // TargetLibraryName
    private String targetDatabase;

    // TargetLibraryFrame Name
    private String targetSchema;

    // TargetLibraryTableList
    private List<Table> targetTable;

    // Source fields(MaleTotalField)
    private List<Field> sourceColumn;

    // Filter Conditions
    private List<Filter> filter = new ArrayList<>();

    /**
     * Trigger Method
     */
    private String trigger = CommonTaskTriggerEnum.ONCE.getCode();

    /**
     * ScheduledExpression, Format: [seconds] [min] [hours] [Day] [Intern] [Week]
     */
    private String cron = "*/30 * * * * ?";

    /**
     * After verification of inconsistenciesYesNoSyncTarget database
     */
    private boolean enableSync = false;

    /**
     * YesNoReverse check
     */
    private boolean enableReverseScan = false;

    /**
     * TasksOn implementationYesNoAutoDeleteTarget databaseMediumSource tableNon-existent redundant lines
     */
    private boolean enableReverseSync = false;

    /**
     * TableStructureVerify INTRODUCTION
     */
    private boolean enableSchema;

    /**
     * Verification scope（Row data）
     */
    private boolean enableRowData = true;

    /**
     * Verification scope（Index）
     */
    private boolean enableIndex = false;

    /**
     * Verification scope（Trigger）
     */
    private boolean enableTrigger = false;

    /**
     * Verification scope（Functions）
     */
    private boolean enableFunction = false;

    /**
     * Batch readNumber of bar(s)andSync task Mapping In the same sense)
     */
    private int readNum = 10000;

    /**
     * Single writeNumber of bars
     */
    private int batchNum = 1000;

    /**
     * ImplementationThreads
     */
    private int threadNum = 10;

    public String getSourceConnectorId() {
        return sourceConnectorId;
    }

    public void setSourceConnectorId(String sourceConnectorId) {
        this.sourceConnectorId = sourceConnectorId;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public String getSourceSchema() {
        return sourceSchema;
    }

    public void setSourceSchema(String sourceSchema) {
        this.sourceSchema = sourceSchema;
    }

    public List<Table> getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(List<Table> sourceTable) {
        this.sourceTable = sourceTable;
    }

    public String getTargetConnectorId() {
        return targetConnectorId;
    }

    public void setTargetConnectorId(String targetConnectorId) {
        this.targetConnectorId = targetConnectorId;
    }

    public String getTargetDatabase() {
        return targetDatabase;
    }

    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
    }

    public List<Table> getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(List<Table> targetTable) {
        this.targetTable = targetTable;
    }

    public String getTrigger() {
        return trigger;
    }

    public List<Field> getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(List<Field> sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public List<Filter> getFilter() {
        return filter;
    }

    public void setFilter(List<Filter> filter) {
        this.filter = filter;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public boolean isEnableSync() {
        return enableSync;
    }

    public void setEnableSync(boolean enableSync) {
        this.enableSync = enableSync;
    }

    public boolean isEnableReverseScan() {
        return enableReverseScan;
    }

    public void setEnableReverseScan(boolean enableReverseScan) {
        this.enableReverseScan = enableReverseScan;
    }

    public boolean isEnableReverseSync() {
        return enableReverseSync;
    }

    public void setEnableReverseSync(boolean enableReverseSync) {
        this.enableReverseSync = enableReverseSync;
    }

    public boolean isEnableRowData() {
        return enableRowData;
    }

    public void setEnableRowData(boolean enableRowData) {
        this.enableRowData = enableRowData;
    }

    public boolean isEnableIndex() {
        return enableIndex;
    }

    public void setEnableIndex(boolean enableIndex) {
        this.enableIndex = enableIndex;
    }

    public boolean isEnableTrigger() {
        return enableTrigger;
    }

    public void setEnableTrigger(boolean enableTrigger) {
        this.enableTrigger = enableTrigger;
    }

    public boolean isEnableFunction() {
        return enableFunction;
    }

    public void setEnableFunction(boolean enableFunction) {
        this.enableFunction = enableFunction;
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

    public boolean isEnableSchema() {
        return enableSchema;
    }

    public void setEnableSchema(boolean enableSchema) {
        this.enableSchema = enableSchema;
    }
}
