/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.common.model.Paging;
import org.dbsyncer.parser.model.Connector;

import java.util.List;
import java.util.Map;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/17 23:18
 */
public interface ConnectorConfigService {

    /**
     * InsertConnector
     */
    String add(Map<String, String> params);

    /**
     * CopyConnector
     *
     * @param id       SourceConnector ID
     * @param username ActionsWhoUsername（For awarding newResource ACL）
     */
    String copy(String id, String username);

    /**
     * Modify ConnectionDevice
     */
    String edit(Map<String, String> params);

    /**
     * DeleteConnector
     */
    String remove(String id);

    /**
     * AccessConnector
     */
    Connector getConnector(String id);

    /**
     * Getting dataLibraryInfo
     */
    List<String> getDatabase(String id);

    /**
     * AccessSchemaInfo
     */
    List<String> getSchema(String id, String catalog);

    /**
     * Get AllConnector
     */
    List<Connector> getConnectorAll();

    /**
     * SearchConnector
     */
    Paging<Connector> search(Map<String, String> params);

    /**
     * Get All SupportedConnectorType
     */
    List<String> getConnectorTypeAll();

    /**
     * InspectionConnectorStatus
     */
    void refreshHealth();

    /**
     * ConnectorYesNoAvailable Lawyers
     */
    boolean isAlive(String id);

    /**
     * Get BitsInfo
     */
    Object getPosition(String params);
}
