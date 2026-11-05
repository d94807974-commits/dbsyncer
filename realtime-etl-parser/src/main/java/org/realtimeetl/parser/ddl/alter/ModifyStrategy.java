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
package org.realtimeetl.parser.ddl.alter;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.ddl.AlterStrategy;
import org.realtimeetl.sdk.config.DDLConfig;
import org.realtimeetl.sdk.enums.DDLOperationEnum;

import net.sf.jsqlparser.statement.alter.AlterExpression;

import net.sf.jsqlparser.statement.alter.AlterExpression;

/**
 * FieldProperties Change
 * <code>
 * ALTER TABLE `test`.`test_user`
 * MODIFY COLUMN `name` varchar(203) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL AFTER `id`,
 * MODIFY COLUMN `remark` varchar(204) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL AFTER `name`
 * </code>
 *
 * @author Realtime ETL
 */
public class ModifyStrategy implements AlterStrategy {

    @Override
    public void parse(AlterExpression expression, DDLConfig ddlConfig) {
        if (expression.getColDataTypeList() != null) {
            for (AlterExpression.ColumnDataType columnDataType : expression.getColDataTypeList()) {
                String columnName = StringUtil.replace(columnDataType.getColumnName(), StringUtil.BACK_QUOTE, StringUtil.EMPTY);
                columnName = StringUtil.replace(columnName, StringUtil.DOUBLE_QUOTATION, StringUtil.EMPTY);
                ddlConfig.getModifiedFieldNames().add(columnName);
            }
        }
        ddlConfig.setDdlOperationEnum(DDLOperationEnum.ALTER_MODIFY);
    }
}
