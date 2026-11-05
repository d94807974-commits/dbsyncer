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
 * @date 2019/9/26 23:58
 */
public class SqlBuilderInsert extends AbstractSqlBuilder {

    @Override
    public String buildSql(SqlBuilderConfig config) {
        Database database = config.getDatabase();

        List<String> fs = new ArrayList<>();
        List<String> vs = new ArrayList<>();
        for (Field f : config.getFields()) {
            // AddField name
            fs.add(database.buildWithQuotation(f.getName()));

            // Deal with specialTypeYes.ValueExpression
            if (database.buildCustomValue(vs, f)) {
                continue;
            }
            vs.add("?");
        }
        // INSERT INTO "USER" ("USERNAME","AGE") VALUES (?,?)
        return String.format("%sINSERT INTO %s%s (%s) VALUES (%s)", database.generateUniqueCode(), config.getSchema(), database.buildWithQuotation(config.getTableName()), StringUtil
                .join(fs, StringUtil.COMMA), StringUtil.join(vs, StringUtil.COMMA));
    }

}