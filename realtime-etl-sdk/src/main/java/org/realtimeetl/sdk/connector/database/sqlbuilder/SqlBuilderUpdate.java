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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/9/27 0:03
 */
public class SqlBuilderUpdate extends AbstractSqlBuilder {

    @Override
    public String buildSql(SqlBuilderConfig config) {
        Database database = config.getDatabase();

        StringBuilder sql = new StringBuilder(database.generateUniqueCode());
        sql.append("UPDATE ").append(config.getSchema());
        sql.append(database.buildWithQuotation(config.getTableName()));
        sql.append(" SET ");

        List<String> fs = new ArrayList<>();
        for (Field f : config.getFields()) {
            String fieldName = database.buildWithQuotation(f.getName());

            // Deal with specialTypeYes.ValueExpression
            List<String> vs = new ArrayList<>();
            if (database.buildCustomValue(vs, f)) {
                // UseCustomValueExpression
                fs.add(fieldName + "=" + vs.get(0));
            } else {
                // Use Default? Placeholder
                fs.add(fieldName + "=?");
            }
        }
        sql.append(StringUtil.join(fs, StringUtil.COMMA));

        // UPDATE "USER" SET "USERNAME"=?,"AGE"=? WHERE "ID"=? AND "UID" = ?
        sql.append(" WHERE ");
        database.appendPrimaryKeys(sql, config.getPrimaryKeys());
        return sql.toString();
    }
}
