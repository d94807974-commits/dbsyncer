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
package org.realtimeetl.connector.mongodb.validator;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.mongodb.MongoDBConnector;
import org.realtimeetl.connector.mongodb.config.MongoDBConfig;
import org.realtimeetl.connector.mongodb.util.MongoUtil;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.util.PropertiesUtil;

import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 20:00
 */
public final class MongoDBConfigValidator implements ConfigValidator<MongoDBConnector, MongoDBConfig> {

    @Override
    public void modify(MongoDBConnector connectorService, MongoDBConfig connectorConfig, Map<String, String> params) {
        String host = params.get("host");
        int port = NumberUtil.toInt(Objects.toString(params.get("port")));
        String username = params.get("username");
        String password = params.get("password");
        String database = params.get("database");
        String authDatabase = params.get("authDatabase");
        String properties = params.get("properties");
        int maxPoolSize = NumberUtil.toInt(Objects.toString(params.get("maxPoolSize")), connectorConfig.getMaxPoolSize());
        Assert.hasText(host, "Host is empty.");
        Assert.isTrue(port > 0, "Port is invalid.");
        Assert.hasText(database, "Database is empty.");
        Assert.isTrue(maxPoolSize >= 1 && maxPoolSize <= 512, "Max connections must be between 1 and 512.");

        connectorConfig.setHost(host);
        connectorConfig.setPort(port);
        connectorConfig.setUsername(username);
        connectorConfig.setPassword(password);
        connectorConfig.setDatabase(database);
        connectorConfig.setAuthDatabase(StringUtil.isNotBlank(authDatabase) ? authDatabase : "admin");
        connectorConfig.setMaxPoolSize(maxPoolSize);
        connectorConfig.setProperties(PropertiesUtil.parse(properties));
        connectorConfig.setUrl(MongoUtil.buildConnectionString(connectorConfig));
    }

    @Override
    public Table modifyExtendedTable(MongoDBConnector connectorService, Map<String, String> params) {
        Table table = new Table();
        String tableName = params.get("tableName");
        String columnList = params.get("columnList");
        Assert.hasText(tableName, "TableName is empty");
        Assert.hasText(columnList, "ColumnList is empty");
        List<Field> fields = JsonUtil.jsonToArray(columnList, Field.class);
        Assert.notEmpty(fields, "Field cannot be empty.");
        table.setName(tableName);
        table.setColumn(fields);
        table.setType(connectorService.getExtendedTableType().getCode());
        return table;
    }
}
