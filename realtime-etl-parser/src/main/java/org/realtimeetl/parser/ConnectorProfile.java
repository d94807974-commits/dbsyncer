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
package org.realtimeetl.parser;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.Connector;

import java.util.List;

/**
 * ConnectorConfigureRead and writeandInverse sequence (abstract){@code ConnectorConfig} PressconnectorType Revert.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public interface ConnectorProfile {

    /**
     * Connector JSON → Models (with specificConfig Category of achievement).
     */
    Connector parseConnector(String json);

    /**
     * Pressid QuestionConnector。
     */
    Connector getConnector(String connectorId);

    /**
     * AllConnector。
     */
    List<Connector> getConnectorAll();

    /**
     * minPageQuestionConnector（PressNameFuzzy AdministerUpdated atRewind.
     *
     * @param pageNum   PageCode, from1 Start
     * @param pageSize  EveryPage/5/
     * @param searchKey NameKeyword，Empty
     * @param role      RoleFilter:{@code source} OnlyIS_SOURCE=1，{@code target} OnlyIS_TARGET=1，Other without filtering
     * @param ids Resource ID；Nothing.ID Condition
     */
    Paging<Connector> queryConnectors(int pageNum, int pageSize, String searchKey, String role, List<String> ids);

    /**
     * Add ConnectionThe device.
     */
    String addConnector(Connector connector);

    /**
     * BatchAdd ConnectionInstrument(s)ConfigurePackageImport）。
     */
    void addConnectorBatch(List<Connector> connectors);

    /**
     * UpdateConnector。
     */
    String updateConnector(Connector connector);

    /**
     * DeleteConnector。
     */
    void removeConnector(String id);

    /**
     * ConnectorTotal。
     */
    int countConnectors();

    /**
     * Fromconnector.json SeriesImport。
     */
    void importConnectorsFromJson(String json);
}
