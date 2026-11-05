/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.parser.model;

import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.dbsyncer.sdk.model.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Map relation(dbsyncer_table_group)。
 * <p>AssociationInfoDropminColumns;Field mapping/command/filter Wait in.JSON。
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/15 23:56
 */
public class TableGroup extends AbstractConfigModel {

    public TableGroup() {
        super.setType(ConfigConstant.TABLE_GROUP);
        super.setName(ConfigConstant.TABLE_GROUP);
    }

    /**
     * Sort Number(SORT_INDEX)
     */
    private int index;

    /**
     * Task ID
     */
    private String taskId;

    private String sourceConnectorId;
    private String targetConnectorId;
    private String sourceDatabase;
    private String targetDatabase;
    private String sourceSchema;
    private String targetSchema;

    /**
     * Data FactSource table(ConfigureComplete stateTable；NameSync ActualSOURCE_TABLE Columns)
     */
    private Table sourceTable;

    /**
     * Target table(ConfigureComplete stateTable；NameSync ActualTARGET_TABLE Columns)
     */
    private Table targetTable;

    private long sourceTotal;
    private long targetTotal;

    private List<FieldMapping> fieldMapping = new ArrayList<>();

    private Map<String, String> command = new HashMap<>();

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getTaskId() {
        return taskId;
    }

    public TableGroup setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    /**
     * LibraryWidth 7-11 Key: Source/Target ConnectionDevice+ Library + schema（does not containTableName.
     */
    public String buildDatabaseMappingKey() {
        return String.join("|",
                StringUtil.getIfBlank(sourceConnectorId, ""),
                StringUtil.getIfBlank(targetConnectorId, ""),
                StringUtil.getIfBlank(sourceDatabase, ""),
                StringUtil.getIfBlank(targetDatabase, ""),
                StringUtil.getIfBlank(sourceSchema, ""),
                StringUtil.getIfBlank(targetSchema, ""));
    }

    public String getSourceConnectorId() {
        return sourceConnectorId;
    }

    public void setSourceConnectorId(String sourceConnectorId) {
        this.sourceConnectorId = sourceConnectorId;
    }

    public String getTargetConnectorId() {
        return targetConnectorId;
    }

    public void setTargetConnectorId(String targetConnectorId) {
        this.targetConnectorId = targetConnectorId;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public String getTargetDatabase() {
        return targetDatabase;
    }

    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public String getSourceSchema() {
        return sourceSchema;
    }

    public void setSourceSchema(String sourceSchema) {
        this.sourceSchema = sourceSchema;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
    }

    public Table getSourceTable() {
        return sourceTable;
    }

    public TableGroup setSourceTable(Table sourceTable) {
        this.sourceTable = sourceTable;
        return this;
    }

    public Table getTargetTable() {
        return targetTable;
    }

    public TableGroup setTargetTable(Table targetTable) {
        this.targetTable = targetTable;
        return this;
    }

    public long getSourceTotal() {
        return sourceTotal;
    }

    public void setSourceTotal(long sourceTotal) {
        this.sourceTotal = sourceTotal;
    }

    public long getTargetTotal() {
        return targetTotal;
    }

    public void setTargetTotal(long targetTotal) {
        this.targetTotal = targetTotal;
    }

    public List<FieldMapping> getFieldMapping() {
        return fieldMapping;
    }

    public void setFieldMapping(List<FieldMapping> fieldMapping) {
        this.fieldMapping = fieldMapping;
    }

    public Map<String, String> getCommand() {
        return command;
    }

    public TableGroup setCommand(Map<String, String> command) {
        this.command = command;
        return this;
    }
}
