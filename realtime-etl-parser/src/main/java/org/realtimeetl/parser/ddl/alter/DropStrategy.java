/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.parser.ddl.alter;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.ddl.AlterStrategy;
import org.realtimeetl.sdk.config.DDLConfig;
import org.realtimeetl.sdk.enums.DDLOperationEnum;

import net.sf.jsqlparser.statement.alter.AlterExpression;

import net.sf.jsqlparser.statement.alter.AlterExpression;

/**
 * Parsingdrop
 * <code>
 *     ALTER TABLE `test`.`test_user`
 * DROP COLUMN `aaa`,
 * DROP COLUMN `bbb`
 * </code>
 *
 * @author life
 */
public class DropStrategy implements AlterStrategy {

    @Override
    public void parse(AlterExpression expression, DDLConfig ddlConfig) {
        if (expression.getColumnName() != null) {
            String columnName = StringUtil.replace(expression.getColumnName(), StringUtil.BACK_QUOTE, StringUtil.EMPTY);
            columnName = StringUtil.replace(columnName, StringUtil.DOUBLE_QUOTATION, StringUtil.EMPTY);
            ddlConfig.getDroppedFieldNames().add(columnName);
        }
        ddlConfig.setDdlOperationEnum(DDLOperationEnum.ALTER_DROP);
    }
}
