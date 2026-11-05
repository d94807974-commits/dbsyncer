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
package org.realtimeetl.sdk.config;

import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.Filter;
import org.realtimeetl.sdk.model.Table;

import java.util.List;

/**
 * Query SyncParameterTemplates
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/9/16 23:59
 */
public class CommandConfig {

    private String connectorType;

    private Table table;

    private List<Filter> filter;

    /**
     * Data FactLibrary/ContentsMongoDB database、JDBC catalog）
     */
    private String catalog;

    private String schema;

    private ConnectorInstance connectorInstance;

    // OverwriteWriting
    private boolean forceUpdate;

    public CommandConfig(String connectorType, String schema, Table table, ConnectorInstance connectorInstance, List<Filter> filter) {
        this.connectorType = connectorType;
        this.schema = schema;
        this.table = table;
        this.connectorInstance = connectorInstance;
        this.filter = filter;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public Table getTable() {
        return table;
    }

    public List<Filter> getFilter() {
        return filter;
    }

    public ConnectorConfig getConnectorConfig() {
        return connectorInstance.getConfig();
    }

    public ConnectorInstance getConnectorInstance() {
        return connectorInstance;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}
