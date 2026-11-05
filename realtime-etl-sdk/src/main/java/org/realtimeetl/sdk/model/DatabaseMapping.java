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
package org.realtimeetl.sdk.model;

import org.realtimeetl.common.util.StringUtil;

/**
 * LibraryLevel MapConfigure（Enduringontask.JSON；Does not contain Transparency Maps.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-05-22 11:14
 */
public class DatabaseMapping {

    /**
     * No.（From childhood to age,Tasksin this order)
     */
    private int index;

    /**
     * Source databaseFirst Name
     */
    private String sourceDatabase;

    /**
     * Source schema
     */
    private String sourceSchema;

    /**
     * SourceConnector ID
     */
    private String sourceConnectorId;

    /**
     * Objective CombinedEndConnector ID
     */
    private String targetConnectorId;

    /**
     * Target database name
     */
    private String targetDatabase;

    /**
     * Target schema（Optional)
     */
    private String targetSchema;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSourceDatabase() {
        return sourceDatabase;
    }

    public void setSourceDatabase(String sourceDatabase) {
        this.sourceDatabase = sourceDatabase;
    }

    public String getSourceSchema() {
        return sourceSchema;
    }

    public void setSourceSchema(String sourceSchema) {
        this.sourceSchema = sourceSchema;
    }

    public String getSourceConnectorId() {
        return sourceConnectorId;
    }

    public void setSourceConnectorId(String sourceConnectorId) {
        this.sourceConnectorId = sourceConnectorId;
    }

    public String getTargetConnectorId() {
        return targetConnectorId;
    }

    public void setTargetConnectorId(String targetConnectorId) {
        this.targetConnectorId = targetConnectorId;
    }

    public String getTargetDatabase() {
        return targetDatabase;
    }

    public void setTargetDatabase(String targetDatabase) {
        this.targetDatabase = targetDatabase;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
    }

    /**
     * LibraryWidth 7-11 Key: Source/Target ConnectionDevice+ Library + schema（does not containTable name），and TableGroup#buildDatabaseMappingKey The same rule.
     */
    public String buildDatabaseMappingKey() {
        return String.join("|",
                StringUtil.getIfBlank(sourceConnectorId, ""),
                StringUtil.getIfBlank(targetConnectorId, ""),
                StringUtil.getIfBlank(sourceDatabase, ""),
                StringUtil.getIfBlank(targetDatabase, ""),
                StringUtil.getIfBlank(sourceSchema, ""),
                StringUtil.getIfBlank(targetSchema, ""));
    }
}
