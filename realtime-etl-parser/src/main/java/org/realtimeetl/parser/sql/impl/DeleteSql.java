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
package org.realtimeetl.parser.sql.impl;

import org.realtimeetl.parser.model.FieldMapping;
import org.realtimeetl.parser.sql.SqlParser;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.delete.Delete;

import java.util.List;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-12-10 00:43
 */
public final class DeleteSql implements SqlParser {

    private String sql;

    private String sourceTableName;

    private String targetTableName;

    private List<FieldMapping> fieldMappingList;

    public DeleteSql(String sql, String sourceTableName, String targetTableName, List<FieldMapping> fieldMappingList) {
        this.sql = sql;
        this.sourceTableName = sourceTableName;
        this.targetTableName = targetTableName;
        this.fieldMappingList = fieldMappingList;
    }

    @Override
    public String parse() {
        try {
            Delete delete = (Delete) CCJSqlParserUtil.parse(sql);
            // ReplaceTable name
            Table table = new Table();
            table.setName(targetTableName);
            delete.setTable(table);
            whereParse(delete.getWhere());
            return delete.toString();
        } catch (JSQLParserException e) {
            throw new RuntimeException(e);
        }
    }

    private void whereParse(Expression expression) {
        BinaryExpression binaryExpression = (BinaryExpression) expression;
        Expression left = binaryExpression.getLeftExpression();
        Expression right = binaryExpression.getRightExpression();
        findColumn((BinaryExpression) left);
        findColumn((BinaryExpression) right);
    }

    private void findColumn(BinaryExpression binaryExpression) {
        if (binaryExpression.getLeftExpression() instanceof Column) {
            Column column = (Column) binaryExpression.getLeftExpression();
            fieldMappingList.stream().filter(x->x.getSource().getName().equals(column.getColumnName().replaceAll("\"", ""))).findFirst()
                    .ifPresent(fieldMapping->column.setColumnName(fieldMapping.getTarget().getName()));
            return;
        }
        findColumn((BinaryExpression) binaryExpression.getLeftExpression());
        findColumn((BinaryExpression) binaryExpression.getRightExpression());
    }
}