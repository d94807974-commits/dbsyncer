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
package org.realtimeetl.connector.rocketmq.validator;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.rocketmq.RocketMQConnector;
import org.realtimeetl.connector.rocketmq.config.RocketMQConfig;
import org.realtimeetl.connector.rocketmq.util.RocketMQUtil;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Table;

import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 01:00
 */
public class RocketMQConfigValidator implements ConfigValidator<RocketMQConnector, RocketMQConfig> {

    @Override
    public void modify(RocketMQConnector connectorService, RocketMQConfig connectorConfig, Map<String, String> params) {
        String url = params.get("url");
        String properties = params.get("properties");
        String producerProperties = params.get(RocketMQUtil.PRODUCER_PROPERTIES);
        String consumerProperties = params.get(RocketMQUtil.CONSUMER_PROPERTIES);
        if (producerProperties == null && consumerProperties == null) {
            String extInfo = params.get("extInfo");
            Assert.hasText(extInfo, "Extension parameters cannot be empty");
            Properties props = JsonUtil.jsonToObj(extInfo, Properties.class);
            connectorConfig.getExtInfo().putAll(props != null ? props : new Properties());
        } else {
            Assert.hasText(producerProperties, "Producer parameters cannot be empty");
            Assert.hasText(consumerProperties, "Consumer parameters cannot be empty");
            connectorConfig.getExtInfo().put(RocketMQUtil.PRODUCER_PROPERTIES, producerProperties);
            connectorConfig.getExtInfo().put(RocketMQUtil.CONSUMER_PROPERTIES, consumerProperties);
        }
        Assert.hasText(url, "url is empty.");
        if (StringUtil.isNotBlank(properties)) {
            connectorConfig.getProperties().putAll(RocketMQUtil.parse(properties));
        }
        connectorConfig.setUrl(url);
    }

    @Override
    public Table modifyExtendedTable(RocketMQConnector connectorService, Map<String, String> params) {
        Table table = new Table();
        String tableName = params.get("tableName");
        String columnList = params.get("columnList");
        String groupId = params.get("groupId");
        String tags = params.get("tags");
        Assert.hasText(tableName, "TableName is empty");
        Assert.hasText(columnList, "ColumnList is empty");
        List<Field> fields = JsonUtil.jsonToArray(columnList, Field.class);
        Assert.notEmpty(fields, "Field cannot be empty.");
        table.setName(tableName);
        table.setColumn(fields);
        table.setType(connectorService.getExtendedTableType().getCode());
        if (StringUtil.isNotBlank(groupId)) {
            table.getExtInfo().put("groupId", groupId);
        }
        if (StringUtil.isNotBlank(tags)) {
            table.getExtInfo().put("tags", tags);
        }
        return table;
    }
}
