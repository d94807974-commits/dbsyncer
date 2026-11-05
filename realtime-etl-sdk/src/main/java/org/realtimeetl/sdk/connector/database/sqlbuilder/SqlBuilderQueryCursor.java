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
package org.realtimeetl.sdk.connector.database.sqlbuilder;

import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.PageSql;

import java.util.List;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026/01/17 0:03
 */
public class SqlBuilderQueryCursor extends SqlBuilderQuery {

    @Override
    public String buildSql(SqlBuilderConfig config) {
        String queryFilter = config.getQueryFilter();
        List<String> primaryKeys = config.getPrimaryKeys();
        List<Field> fields = config.getFields();
        String querySql = buildQuerySql(config);
        PageSql pageSql = new PageSql(querySql, queryFilter, primaryKeys, fields);
        return config.getDatabase().getPageCursorSql(pageSql);
    }
}
