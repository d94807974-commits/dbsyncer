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

import java.util.List;
import java.util.Properties;

/**
 * Table basicInfo
 *
 * @author Realtime ETL
 * @ClassName: MetaInfo
 * @Description: IncludingFieldInfo、Total number of articles
 * @date: 2017Year7Intern20p.m.3:37:59
 */
public class MetaInfo {

    /**
     * Table
     */
    private String table;

    /**
     * TableType
     */
    private String tableType;

    /**
     * PropertiesField
     * Mostly in the[{"name":"ID","typeName":"INT","type":"4"},{"name":"NAME","typeName":"VARCHAR","type":"12"}]
     */
    private List<Field> column;

    /**
     * Extension Configuration
     */
    private Properties extInfo = new Properties();

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public List<Field> getColumn() {
        return column;
    }

    public void setColumn(List<Field> column) {
        this.column = column;
    }

    public Properties getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(Properties extInfo) {
        this.extInfo = extInfo;
    }
}
