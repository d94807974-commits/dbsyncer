/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.Connector;

import java.util.List;

/**
 * ConnectorConfigureRead and writeandInverse sequence (abstract){@code ConnectorConfig} PressconnectorType Revert.
 *
 * @author wuji
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
