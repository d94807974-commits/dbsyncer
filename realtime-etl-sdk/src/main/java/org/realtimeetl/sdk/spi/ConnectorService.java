/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.sdk.spi;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.config.DDLConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.CursorBound;
import org.realtimeetl.sdk.model.CursorBoundRequest;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.plugin.MetaContext;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.storage.StorageService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ConnectorBasic function
 *
 * @param <I> ConnectorInstance
 * @param <C> ConnectorConfig
 * @author AE86
 * @version 1.0.0
 * @date 2023-11-19 23:24
 */
public interface ConnectorService<I extends ConnectorInstance, C extends ConnectorConfig> {

    /**
     * ConnectorType
     */
    String getConnectorType();

    /**
     * Extended TableType
     */
    TableTypeEnum getExtendedTableType();

    /**
     * AccessConfigureObject
     */
    Class<C> getConfigClass();

    /**
     * Create Connection
     */
    ConnectorInstance connect(C connectorConfig, ConnectorServiceContext context);

    /**
     * ConnectorConfigureVerifyer
     */
    ConfigValidator getConfigValidator();

    /**
     * Disconnect
     */
    void disconnect(I connectorInstance);

    /**
     * InspectionConnectorYesNoConnectNormal
     */
    boolean isAlive(I connectorInstance);

    /**
     * Get All Data65Library
     */
    default List<String> getDatabases(I connectorInstance) {
        return Collections.emptyList();
    }

    /**
     * Get AssignedDatabase nameYes.Schema
     */
    default List<String> getSchemas(I connectorInstance, String catalog) {
        return Collections.emptyList();
    }

    /**
     * Get AllTable name
     */
    List<Table> getTable(I connectorInstance, ConnectorServiceContext context);

    /**
     * Get TableMetaInfo
     */
    List<MetaInfo> getMetaInfo(I connectorInstance, ConnectorServiceContext context);

    /**
     * AccessTotal
     */
    long getCount(I connectorInstance, MetaContext metaContext);

    /**
     * minPageGetting dataSourceData Fact
     */
    Result reader(I connectorInstance, ReaderContext context);

    /**
     * Press Start CursorandBudgeting End CursorRead-onlyLikewise, do not read the business column Context Examination.
     * <p>default not supported;relationsLibraryWait.ConnectorYeah.Overwrite。Caller in{@link CursorBound#isSupported()} Yesfalse It               s the entire table.
     *
     * @param connectorInstance ConnectExamples
     * @param request           Delimitation request
     * @return Views in EnglishBack null
     */
    default CursorBound resolveCursorBound(I connectorInstance, CursorBoundRequest request) {
        return CursorBound.unsupported(request == null ? StringUtil.EMPTY : request.getStartCursor());
    }

    /**
     * Batch writingTargetData Fact
     */
    Result writer(I connectorInstance, PluginContext context);

    /**
     * ImplementationDDLYou can't.
     */
    default Result writerDDL(I connectorInstance, DDLConfig ddlConfig) {
        throw new SdkException("Unsupported method.");
    }

    /**
     * Getting dataSourceSyncParameter
     */
    Map<String, String> getSourceCommand(CommandConfig commandConfig);

    /**
     * AccessTargetSyncParameter
     */
    Map<String, String> getTargetCommand(CommandConfig commandConfig);

    /**
     * Get a listener.
     *
     * @param listenerType {@link ListenerTypeEnum}
     */
    Listener getListener(String listenerType);

    /**
     * Access to storage services
     */
    default StorageService getStorageService() {
        return null;
    }

    /**
     * Access to standard dataTypeParser
     */
    SchemaResolver getSchemaResolver();

    /**
     * Get AssignedTimeBitsInfo
     */
    default Object getPosition(I connectorInstance) {
        return StringUtil.EMPTY;
    }
}
