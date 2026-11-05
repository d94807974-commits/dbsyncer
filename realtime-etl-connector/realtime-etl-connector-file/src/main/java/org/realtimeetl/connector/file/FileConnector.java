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
package org.realtimeetl.connector.file;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.file.cdc.FileListener;
import org.realtimeetl.connector.file.column.FileResolver;
import org.realtimeetl.connector.file.config.FileConfig;
import org.realtimeetl.connector.file.schema.FileSchemaResolver;
import org.realtimeetl.connector.file.validator.FileConfigValidator;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.connector.AbstractConnector;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.plugin.MetaContext;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.spi.ConnectorService;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * DocumentationConnectorAchieved
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2022-05-05 23:19
 */
public final class FileConnector extends AbstractConnector implements ConnectorService<FileConnectorInstance, FileConfig> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String FILE_SEPARATOR = "separator";
    private final String FILE_PATH = "filePath";
    private final FileResolver fileResolver = new FileResolver();
    private final FileConfigValidator configValidator = new FileConfigValidator();
    private final FileSchemaResolver schemaResolver = new FileSchemaResolver();

    @Override
    public String getConnectorType() {
        return "File";
    }

    @Override
    public TableTypeEnum getExtendedTableType() {
        return TableTypeEnum.SEMI;
    }

    @Override
    public Class<FileConfig> getConfigClass() {
        return FileConfig.class;
    }

    @Override
    public ConnectorInstance connect(FileConfig config, ConnectorServiceContext context) {
        return new FileConnectorInstance(config);
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public void disconnect(FileConnectorInstance connectorInstance) {

    }

    @Override
    public boolean isAlive(FileConnectorInstance connectorInstance) {
        String fileDir = connectorInstance.getConnection();
        boolean alive = new File(fileDir).exists();
        if (!alive) {
            logger.warn("can not find fileDir:{}", fileDir);
            return false;
        }
        return true;
    }

    @Override
    public List<Table> getTable(FileConnectorInstance connectorInstance, ConnectorServiceContext context) {
        return new ArrayList<>();
    }

    @Override
    public List<MetaInfo> getMetaInfo(FileConnectorInstance connectorInstance, ConnectorServiceContext context) {
        List<MetaInfo> metaInfos = new ArrayList<>();
        for (Table table : context.getTablePatterns()) {
            MetaInfo metaInfo = new MetaInfo();
            metaInfo.setTable(table.getName());
            metaInfo.setTableType(getExtendedTableType().getCode());
            metaInfo.setColumn(table.getColumn());
            metaInfo.setExtInfo(table.getExtInfo());
            metaInfos.add(metaInfo);
        }
        return metaInfos;
    }

    @Override
    public long getCount(FileConnectorInstance connectorInstance, MetaContext metaContext) {
        // TODO To be optimized, readtableExtensionInfo
        Map<String, String> command = metaContext.getCommand();
        AtomicLong count = new AtomicLong();
        FileReader reader = null;
        try {
            reader = new FileReader(command.get(FILE_PATH));
            LineIterator lineIterator = IOUtils.lineIterator(reader);
            while (lineIterator.hasNext()) {
                lineIterator.next();
                count.addAndGet(1);
            }
        } catch (IOException e) {
            throw new FileException(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return count.get();
    }

    @Override
    public Result reader(FileConnectorInstance connectorInstance, ReaderContext context) {
        List<Map<String, Object>> list = new ArrayList<>();
        FileReader reader = null;
        try {
            Map<String, String> command = context.getCommand();
            final List<Field> fields = context.getSourceTable().getColumn();
            Assert.notEmpty(fields, "The fields of file schema is empty.");
            final char separator = command.get(FILE_SEPARATOR).charAt(0);
            reader = new FileReader(command.get(FILE_PATH));
            LineIterator lineIterator = IOUtils.lineIterator(reader);
            int from = (context.getPageIndex() - 1) * context.getPageSize();
            int to = from + context.getPageSize();
            AtomicLong count = new AtomicLong();
            while (lineIterator.hasNext()) {
                if (count.get() >= from && count.get() < to) {
                    list.add(fileResolver.parseMap(fields, separator, lineIterator.next()));
                } else {
                    lineIterator.next();
                }
                count.addAndGet(1);
                if (count.get() >= to) {
                    break;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new FileException(e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return new Result(list);
    }

    @Override
    public Result writer(FileConnectorInstance connectorInstance, PluginContext context) {
        List<Map> data = context.getTargetList();
        if (CollectionUtils.isEmpty(data)) {
            logger.error("writer data can not be empty.");
            throw new FileException("writer data can not be empty.");
        }

        Result result = new Result();
        OutputStream output = null;
        try {
            Map<String, String> command = context.getCommand();
            final String separator = command.get(FILE_SEPARATOR);
            final String filePath = command.get(FILE_PATH);
            output = new FileOutputStream(filePath, true);
            List<String> lines = data.stream().map(row-> {
                List<String> array = new ArrayList<>();
                context.getTargetFields().forEach(field-> {
                    Object o = row.get(field.getName());
                    array.add(null != o ? String.valueOf(o) : "");
                });
                return StringUtil.join(array.toArray(), separator);
            }).collect(Collectors.toList());
            IOUtils.writeLines(lines, System.lineSeparator(), output, "UTF-8");
        } catch (Exception e) {
            result.addFailData(data);
            result.getError().append(e.getMessage()).append(System.lineSeparator());
            logger.error(e.getMessage());
        } finally {
            IOUtils.closeQuietly(output);
        }
        return result;
    }

    @Override
    public Map<String, String> getSourceCommand(CommandConfig commandConfig) {
        Map<String, String> command = getTargetCommand(commandConfig);
        String fieldList = JsonUtil.objToJson(commandConfig.getTable().getColumn());
        command.put(ConnectorConstant.OPERTION_QUERY, fieldList);
        return command;
    }

    @Override
    public Map<String, String> getTargetCommand(CommandConfig commandConfig) {
        Map<String, String> command = new HashMap<>();
        FileConfig fileConfig = (FileConfig) commandConfig.getConnectorConfig();
        final String fileDir = fileConfig.getFileDir();
        StringBuilder file = new StringBuilder(fileDir);
        if (!StringUtil.endsWith(fileDir, File.separator)) {
            file.append(File.separator);
        }
        Table table = commandConfig.getTable();
        file.append(table.getName());
        String separator = table.getExtInfo().getProperty(FILE_SEPARATOR, StringUtil.VERTICAL_LINE);
        command.put(FILE_PATH, file.toString());
        command.put(FILE_SEPARATOR, separator);
        return command;
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isLog(listenerType)) {
            return new FileListener();
        }
        return null;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }
}