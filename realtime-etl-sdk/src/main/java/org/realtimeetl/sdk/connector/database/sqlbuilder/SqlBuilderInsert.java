/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
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
 * @author AE86
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