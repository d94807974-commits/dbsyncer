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
 * InsertField
 * <code>
 *     ALTER TABLE `test`.`test_user`
 * ADD COLUMN `aaa` varchar(255) NULL AFTER `create_date`,
 * ADD COLUMN `bbb` varchar(255) NULL AFTER `aaa`
 * </code>
 *
 * @author Realtime ETL
 */
public class AddStrategy implements AlterStrategy {

    @Override
    public void parse(AlterExpression expression, DDLConfig ddlConfig) {
        if (expression.getColDataTypeList() != null) {
            for (AlterExpression.ColumnDataType columnDataType : expression.getColDataTypeList()) {
                String columName = columnDataType.getColumnName();
                columName = StringUtil.replace(columName, StringUtil.BACK_QUOTE, StringUtil.EMPTY);
                columName = StringUtil.replace(columName, StringUtil.DOUBLE_QUOTATION, StringUtil.EMPTY);
                ddlConfig.getAddedFieldNames().add(columName);
            }
        }
        ddlConfig.setDdlOperationEnum(DDLOperationEnum.ALTER_ADD);
    }
}
