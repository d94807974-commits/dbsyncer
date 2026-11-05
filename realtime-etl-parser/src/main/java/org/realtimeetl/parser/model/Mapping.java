package org.realtimeetl.parser.model;

import org.realtimeetl.sdk.config.ListenerConfig;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Table;

import java.util.List;

/**
 * MappingMapping
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/10/1 13:19
 */
public class Mapping extends AbstractConfigModel {

    public Mapping() {
        super.setType(ConfigConstant.MAPPING);
    }

    // SourceConnectorID
    private String sourceConnectorId;

    // SourceLibraryName
    private String sourceDatabase;

    // SourceLibraryFrame Name
    private String sourceSchema;

    // SourceLibraryTableList
    private List<Table> sourceTable;

    // Source fields(MaleTotalField)
    private List<Field> sourceColumn;

    // TargetConnectorID
    private String targetConnectorId;

    // TargetLibraryName
    private String targetDatabase;

    // TargetLibraryFrame Name
    private String targetSchema;

    // TargetLibraryTableList
    private List<Table> targetTable;

    // Target source field(MaleTotalField)
    private List<Field> targetColumn;

    /**
     * Synchronisation Method
     *
     * @see ModelEnum
     */
    private String model;

    // ListenConfigure
    private ListenerConfig listener;

    // Batch read
    private int readNum = 10000;

    // Single write
    private int batchNum = 1000;

    // Table and distributionThreads（FullMultiple tables parallel; line in single table; please click source/Target ConnectionResize pool)
    private int threadNum = 10;

    /**
     * Executors
     */
    private int channelSize = 8;

    // OverwriteWriting
    private boolean forceUpdate = true;

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

    public List<Field> getSourceColumn() {
        return sourceColumn;
    }

    public void setSourceColumn(List<Field> sourceColumn) {
        this.sourceColumn = sourceColumn;
    }

    public List<Field> getTargetColumn() {
        return targetColumn;
    }

    public void setTargetColumn(List<Field> targetColumn) {
        this.targetColumn = targetColumn;
    }

    public String getModel() {
        return model;
    }

    public Mapping setModel(String model) {
        this.model = model;
        return this;
    }

    public ListenerConfig getListener() {
        return listener;
    }

    public Mapping setListener(ListenerConfig listener) {
        this.listener = listener;
        return this;
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

    public int getChannelSize() {
        return channelSize;
    }

    public void setChannelSize(int channelSize) {
        this.channelSize = channelSize;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}
