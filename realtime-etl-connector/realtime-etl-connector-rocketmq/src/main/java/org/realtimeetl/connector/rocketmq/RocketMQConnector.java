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
package org.realtimeetl.connector.rocketmq;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.rocketmq.cdc.RocketMQListener;
import org.realtimeetl.connector.rocketmq.config.RocketMQConfig;
import org.realtimeetl.connector.rocketmq.constant.RocketMQConstant;
import org.realtimeetl.connector.rocketmq.schema.RocketMQSchemaResolver;
import org.realtimeetl.connector.rocketmq.util.RocketMQMessageUtil;
import org.realtimeetl.connector.rocketmq.validator.RocketMQConfigValidator;
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
import org.realtimeetl.sdk.util.PrimaryKeyUtil;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RocketMQ ConnectorAchieved
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 01:00
 */
public class RocketMQConnector extends AbstractConnector implements ConnectorService<RocketMQConnectorInstance, RocketMQConfig> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RocketMQConfigValidator configValidator = new RocketMQConfigValidator();
    private final RocketMQSchemaResolver schemaResolver = new RocketMQSchemaResolver();

    @Override
    public String getConnectorType() {
        return "RocketMQ";
    }

    @Override
    public TableTypeEnum getExtendedTableType() {
        return TableTypeEnum.SEMI;
    }

    @Override
    public Class<RocketMQConfig> getConfigClass() {
        return RocketMQConfig.class;
    }

    @Override
    public ConnectorInstance connect(RocketMQConfig config, ConnectorServiceContext context) {
        return new RocketMQConnectorInstance(config);
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public void disconnect(RocketMQConnectorInstance connectorInstance) {
        connectorInstance.close();
    }

    @Override
    public boolean isAlive(RocketMQConnectorInstance connectorInstance) {
        try {
            connectorInstance.ping();
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new RocketMQException(e);
        }
    }

    @Override
    public List<String> getDatabases(RocketMQConnectorInstance connectorInstance) {
        List<String> databases = new ArrayList<>();
        String url = connectorInstance.getServiceUrl();
        if (StringUtil.isNotBlank(url)) {
            databases.add(url);
        }
        return databases;
    }

    @Override
    public List<Table> getTable(RocketMQConnectorInstance connectorInstance, ConnectorServiceContext context) {
        return new ArrayList<>();
    }

    @Override
    public List<MetaInfo> getMetaInfo(RocketMQConnectorInstance connectorInstance, ConnectorServiceContext context) {
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
    public long getCount(RocketMQConnectorInstance connectorInstance, MetaContext metaContext) {
        return 0;
    }

    @Override
    public Result reader(RocketMQConnectorInstance connectorInstance, ReaderContext context) {
        throw new RocketMQException("RocketMQ does not support full reading");
    }

    @Override
    public Result writer(RocketMQConnectorInstance connectorInstance, PluginContext context) {
        List<Map> data = context.getTargetList();
        if (CollectionUtils.isEmpty(data)) {
            logger.error("writer data can not be empty.");
            throw new RocketMQException("writer data can not be empty.");
        }

        Result result = new Result();
        List<Field> pkFields = PrimaryKeyUtil.findExistPrimaryKeyFields(context.getTargetFields());
        String topic = context.getCommand().get(RocketMQConstant.TOPIC);
        String tags = context.getCommand().get(RocketMQConstant.TAGS);
        if (StringUtil.isBlank(tags)) {
            tags = "*";
        }
        String sourceTableName = context.getSourceTable().getName();
        String event = StringUtil.isBlank(context.getEvent()) ? ConnectorConstant.OPERTION_INSERT : context.getEvent();
        try {
            DefaultMQProducer producer = connectorInstance.getProducer();
            for (Map row : data) {
                try {
                    Map<String, Object> message = RocketMQMessageUtil.buildMessage(sourceTableName, event, row);
                    String key = RocketMQMessageUtil.buildMessageKey(pkFields, row);
                    byte[] body = JsonUtil.objToJsonSafe(message).getBytes(StandardCharsets.UTF_8);
                    Message rocketMessage = new Message(topic, tags, key, body);
                    SendResult sendResult = producer.send(rocketMessage);
                    if (sendResult == null) {
                        throw new RocketMQException("RocketMQ sent as empty");
                    }
                    result.getSuccessData().add(row);
                } catch (Exception e) {
                    result.getFailData().add(row);
                    result.getError().append(e.getMessage()).append(System.lineSeparator());
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            result.addFailData(data);
            result.getError().append(e.getMessage()).append(System.lineSeparator());
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Map<String, String> getSourceCommand(CommandConfig commandConfig) {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getTargetCommand(CommandConfig commandConfig) {
        Map<String, String> cmd = new HashMap<>();
        Table table = commandConfig.getTable();
        cmd.put(RocketMQConstant.TOPIC, table.getName());
        String tags = table.getExtInfo().getProperty("tags");
        if (StringUtil.isNotBlank(tags)) {
            cmd.put(RocketMQConstant.TAGS, tags);
        }
        return cmd;
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isLog(listenerType)) {
            return new RocketMQListener();
        }
        return null;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }
}
