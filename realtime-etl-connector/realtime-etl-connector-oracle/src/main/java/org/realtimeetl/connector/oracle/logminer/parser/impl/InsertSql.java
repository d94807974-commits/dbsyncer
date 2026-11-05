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

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.oracle.logminer.parser.AbstractParser;
import org.realtimeetl.sdk.model.Field;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;

import java.util.List;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-12-14 14:58
 */
public class InsertSql extends AbstractParser {

    private Insert insert;

    public InsertSql(Insert insert, List<Field> fields) {
        this.insert = insert;
        setFields(fields);
    }

    @Override
    public List<Object> parseColumns() {
        List<Column> columns = insert.getColumns();
        ExpressionList<Expression> values = (ExpressionList<Expression>) insert.getSelect().getValues().getExpressions();
        for (int i = 0; i < columns.size(); i++) {
            columnMap.put(StringUtil.replace(columns.get(i).getColumnName(), StringUtil.DOUBLE_QUOTATION, StringUtil.EMPTY), values.get(i));
        }
        return columnMapToData();
    }

}