/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.sdk.plugin;

import org.realtimeetl.common.model.RsaConfig;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.model.Table;

import java.util.Map;

/**
 * @Author Realtime ETL.
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
