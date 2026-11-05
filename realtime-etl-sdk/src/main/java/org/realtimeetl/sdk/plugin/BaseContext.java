/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.sdk.plugin;

import org.realtimeetl.common.model.RsaConfig;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.model.Table;

import java.util.Map;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-12-05 00:59
 */
public interface BaseContext {

    /**
     * YesNoYesTarget ConnectionDevice
     */
    default boolean isTargetConnector() {
        return false;
    }

    /**
     * AccessSource tableInfo
     */
    Table getSourceTable();

    /**
     * Execute Command
     */
    Map<String, String> getCommand();

    void setCommand(Map<String, String> command);

    /**
     * Data FactSourceConnectExamples
     */
    ConnectorInstance getSourceConnectorInstance();

    void setSourceConnectorInstance(ConnectorInstance sourceConnectorInstance);

    /**
     * AccessRSAIllicit Classes（httpConnectorThe scene)
     */
    RsaManager getRsaManager();

    /**
     * AccessRSAConfigure（httpConnectorThe scene)
     */
    RsaConfig getRsaConfig();
}
