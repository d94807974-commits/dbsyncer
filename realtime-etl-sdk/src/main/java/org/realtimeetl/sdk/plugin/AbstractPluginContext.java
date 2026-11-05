package org.realtimeetl.sdk.plugin;

import org.realtimeetl.common.model.RsaConfig;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Plugin;
import org.realtimeetl.sdk.model.Table;

import java.util.List;
import java.util.Map;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2022/6/30 16:00
 */
public abstract class AbstractPluginContext extends AbstractBaseContext implements PluginContext, Cloneable {

    /**
     * YesNoTerminationTasks
     * <p>true：TargetCan not open messageSync data，Default valuefalse
     */
    private boolean terminated;

    /**
     * TargetConnectExamples
     */
    private ConnectorInstance targetConnectorInstance;

    /**
     * AccessTarget tableInfo
     */
    private Table targetTable;

    /**
     * SyncEvent（INSERT/UPDATE/DELETE）
     */
    private String event;

    /**
     * Target fields
     */
    private List<Field> targetFields;

    /**
     * Batch processingTasksNumber
     */
    private int batchSize;

    /**
     * Overwrite?Update
     */
    private boolean forceUpdate;

    /**
     * YesNoCan not open message
     */
    private boolean enablePrintTraceInfo;

    /**
     * Data FactSource data set
     */
    private List<Map> sourceList;

    /**
     * TargetSource data set
     */
    private List<Map> targetList;

    /**
     * Plugin
     */
    private Plugin plugin;

    /**
     * PluginParameter
     */
    private String pluginExtInfo;

    private String traceId;

    /**
     * AccessRSAIllicit Classes
     */
    private RsaManager rsaManager;

    /**
     * AccessRSAConfigure
     */
    private RsaConfig rsaConfig;

    @Override
    public boolean isTerminated() {
        return terminated;
    }

    @Override
    public void setTerminated(boolean terminated) {
        this.terminated = terminated;
    }

    @Override
    public ConnectorInstance getTargetConnectorInstance() {
        return targetConnectorInstance;
    }

    public void setTargetConnectorInstance(ConnectorInstance targetConnectorInstance) {
        this.targetConnectorInstance = targetConnectorInstance;
    }

    @Override
    public Table getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(Table targetTable) {
        this.targetTable = targetTable;
    }

    @Override
    public String getEvent() {
        return event;
    }

    @Override
    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public List<Field> getTargetFields() {
        return targetFields;
    }

    public void setTargetFields(List<Field> targetFields) {
        this.targetFields = targetFields;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    @Override
    public boolean isEnablePrintTraceInfo() {
        return enablePrintTraceInfo;
    }

    public void setEnablePrintTraceInfo(boolean enablePrintTraceInfo) {
        this.enablePrintTraceInfo = enablePrintTraceInfo;
    }

    @Override
    public List<Map> getSourceList() {
        return sourceList;
    }

    public void setSourceList(List<Map> sourceList) {
        this.sourceList = sourceList;
    }

    @Override
    public List<Map> getTargetList() {
        return targetList;
    }

    public void setTargetList(List<Map> targetList) {
        this.targetList = targetList;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getPluginExtInfo() {
        return pluginExtInfo;
    }

    public void setPluginExtInfo(String pluginExtInfo) {
        this.pluginExtInfo = pluginExtInfo;
    }

    @Override
    public String getTraceId() {
        return traceId == null ? StringUtil.EMPTY : traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public RsaManager getRsaManager() {
        return rsaManager;
    }

    public void setRsaManager(RsaManager rsaManager) {
        this.rsaManager = rsaManager;
    }

    @Override
    public RsaConfig getRsaConfig() {
        return rsaConfig;
    }

    public void setRsaConfig(RsaConfig rsaConfig) {
        this.rsaConfig = rsaConfig;
    }
}
