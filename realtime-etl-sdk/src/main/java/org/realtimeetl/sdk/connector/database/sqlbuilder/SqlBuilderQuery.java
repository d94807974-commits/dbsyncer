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

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.config.SqlBuilderConfig;
import org.realtimeetl.sdk.connector.database.AbstractSqlBuilder;
import org.realtimeetl.sdk.connector.database.Database;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.PageSql;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/9/27 0:03
 */
public class SqlBuilderQuery extends AbstractSqlBuilder {

    @Override
    public String buildSql(SqlBuilderConfig config) {
        // minPageStatement
        List<String> primaryKeys = config.getPrimaryKeys();
        String queryFilter = config.getQueryFilter();
        List<Field> fields = config.getFields();
        PageSql pageSql = new PageSql(buildQuerySql(config), queryFilter, primaryKeys, fields);
        return config.getDatabase().getPageSql(pageSql);
    }

    @Override
    public String buildQuerySql(SqlBuilderConfig config) {
        Database database = config.getDatabase();
        List<String> fs = new ArrayList<>();
        for (Field f : config.getFields()) {
            // An alias was used.
            if (StringUtil.isNotBlank(f.getLabelName())) {
                fs.add(database.buildWithQuotation(f.getName()) + " AS " + f.getLabelName());
                continue;
            }
            // Deal with specialType
            if (database.buildCustom(fs, f)) {
                continue;
            }
            fs.add(database.buildWithQuotation(f.getName()));
        }

        // SELECT "ID","NAME" FROM "TEST"."MY_USER" WHERE "ID" > 100
        return String.format("SELECT %s FROM %s%s%s", StringUtil.join(fs, StringUtil.COMMA), config.getSchema(), database.buildWithQuotation(config.getTableName()), config.getQueryFilter());
    }

}