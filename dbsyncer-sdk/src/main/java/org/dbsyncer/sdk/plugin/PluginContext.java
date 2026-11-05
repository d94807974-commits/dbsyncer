package org.dbsyncer.sdk.plugin;

import org.dbsyncer.sdk.connector.ConnectorInstance;
import org.dbsyncer.sdk.enums.ModelEnum;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.model.Plugin;
import org.dbsyncer.sdk.model.Table;

import java.util.List;
import java.util.Map;

/**
 * PluginConvertContext
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022/10/28 20:26
 */
public interface PluginContext extends BaseContext {

    /**
     * Get Synchronization
     */
    ModelEnum getModelEnum();

    /**
     * YesNoTerminationSync dataPresent.TargetLibrary
     */
    boolean isTerminated();

    /**
     * YesNoTerminationSync dataPresent.TargetLibrary
     *
     * @param terminated true: It's over.Default valuefalse
     */
    void setTerminated(boolean terminated);

    /**
     * TargetConnectExamples
     */
    ConnectorInstance getTargetConnectorInstance();

    /**
     * AccessTarget tableInfo
     */
    Table getTargetTable();

    /**
     * Incremental sync，Event（INSERT/UPDATE/DELETE）
     */
    String getEvent();

    void setEvent(String event);

    /**
     * Target fields
     */
    List<Field> getTargetFields();

    /**
     * Batch processingTasksNumber
     */
    int getBatchSize();

    /**
     * Overwrite?Update
     */
    boolean isForceUpdate();

    /**
     * YesNoCan not open message
     */
    boolean isEnablePrintTraceInfo();

    /**
     * Data FactSource data set
     */
    List<Map> getSourceList();

    /**
     * TargetSource data set
     */
    List<Map> getTargetList();

    void setTargetList(List<Map> targetList);

    /**
     * AccessPlugin
     */
    Plugin getPlugin();

    /**
     * AccessPluginParameter
     */
    String getPluginExtInfo();

    /**
     * AccessTraceId
     */
    String getTraceId();

    /**
     * Light copy
     */
    Object clone() throws CloneNotSupportedException;

}
