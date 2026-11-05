/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.file.validator;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.connector.file.FileConnector;
import org.realtimeetl.connector.file.config.FileConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Table;

import org.springframework.util.Assert;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * DocumentationConnectConfigureVerifyer Achieved
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2022-05-06 00:04
 */
public class FileConfigValidator implements ConfigValidator<FileConnector, FileConfig> {

    @Override
    public void modify(FileConnector connectorService, FileConfig fileConfig, Map<String, String> params) {
        String fileDir = params.get("fileDir");
        Assert.hasText(fileDir, "fileDir is empty.");
        Assert.isTrue(new File(fileDir).exists(), String.format("Path invalid '%s'", fileDir));
        fileConfig.setFileDir(fileDir);
    }

    @Override
    public Table modifyExtendedTable(FileConnector connectorService, Map<String, String> params) {
        Table table = new Table();
        String tableName = params.get("tableName");
        String columnList = params.get("columnList");
        String separator = params.get("separator");
        Assert.hasText(tableName, "TableName is empty");
        Assert.hasText(columnList, "ColumnList is empty");
        Assert.hasText(separator, "Separator cannot be empty");
        List<Field> fields = JsonUtil.jsonToArray(columnList, Field.class);
        Assert.notEmpty(fields, "Field cannot be empty.");
        table.setName(tableName);
        table.setColumn(fields);
        table.setType(connectorService.getExtendedTableType().getCode());
        table.getExtInfo().put(FileConnector.FILE_SEPARATOR, separator);
        return table;
    }
}
