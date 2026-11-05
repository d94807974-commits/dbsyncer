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

import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.model.Table;

import java.util.List;
import java.util.Map;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2025-02-08 02:17
 */
public abstract class AbstractBaseContext implements ReaderContext {

    /**
     * Data FactSourceConnectExamples
     */
    private ConnectorInstance sourceConnectorInstance;

    /**
     * Execute Command
     */
    private Map<String, String> command;

    private Table sourceTable;

    private boolean supportedCursor;

    private List<Object> args;

    private Object[] cursors;

    private int pageIndex;

    private int pageSize;

    @Override
    public ConnectorInstance getSourceConnectorInstance() {
        return sourceConnectorInstance;
    }

    @Override
    public void setSourceConnectorInstance(ConnectorInstance sourceConnectorInstance) {
        this.sourceConnectorInstance = sourceConnectorInstance;
    }

    @Override
    public Map<String, String> getCommand() {
        return command;
    }

    @Override
    public void setCommand(Map<String, String> command) {
        this.command = command;
    }

    @Override
    public Table getSourceTable() {
        return sourceTable;
    }

    public void setSourceTable(Table sourceTable) {
        this.sourceTable = sourceTable;
    }

    @Override
    public boolean isSupportedCursor() {
        return supportedCursor;
    }

    public void setSupportedCursor(boolean supportedCursor) {
        this.supportedCursor = supportedCursor;
    }

    @Override
    public List<Object> getArgs() {
        return args;
    }

    public void setArgs(List<Object> args) {
        this.args = args;
    }

    @Override
    public Object[] getCursors() {
        return cursors;
    }

    public void setCursors(Object[] cursors) {
        this.cursors = cursors;
    }

    @Override
    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
