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
package org.realtimeetl.connector.oracle.logminer.parser.impl;

import org.realtimeetl.connector.oracle.logminer.parser.AbstractParser;
import org.realtimeetl.sdk.model.Field;

import net.sf.jsqlparser.statement.delete.Delete;

import net.sf.jsqlparser.statement.delete.Delete;

import java.util.List;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-12-14 14:58
 */
public class DeleteSql extends AbstractParser {

    private Delete delete;

    public DeleteSql(Delete delete, List<Field> fields) {
        this.delete = delete;
        setFields(fields);
    }

    @Override
    public List<Object> parseColumns() {
        findColumn(delete.getWhere());
        return columnMapToData();
    }
}
