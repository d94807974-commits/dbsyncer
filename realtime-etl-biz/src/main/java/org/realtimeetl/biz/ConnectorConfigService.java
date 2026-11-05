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
package org.realtimeetl.biz;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.Connector;

import java.util.List;
import java.util.Map;

/**
 * @author Realtime ETL
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
