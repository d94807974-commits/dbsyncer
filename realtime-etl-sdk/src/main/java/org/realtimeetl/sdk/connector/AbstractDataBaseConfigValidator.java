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
package org.realtimeetl.sdk.connector;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.util.PropertiesUtil;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Relationship DataLibraryConnectConfigureVerifyer
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2020-01-08 15:17
 */
public abstract class AbstractDataBaseConfigValidator implements ConfigValidator<AbstractDatabaseConnector, DatabaseConfig> {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        String host = params.get("host");
        int port = NumberUtil.toInt(Objects.toString(params.get("port")));
        String username = params.get("username");
        String password = params.get("password");
        String properties = params.get("properties");
        String extInfo = params.get("extInfo");
        String serviceName = params.get("serviceName");
        String database = params.get("database");
        String driverClassName = params.get("driverClassName");
        int maxActive = NumberUtil.toInt(Objects.toString(params.get("maxActive")), connectorConfig.getMaxActive());
        long keepAlive = NumberUtil.toLong(Objects.toString(params.get("keepAlive")), connectorConfig.getKeepAlive());
        Assert.hasText(host, "Host is empty.");
        Assert.isTrue(port > 0, "Port is invalid.");
        Assert.hasText(password, "Password is empty.");
        Assert.hasText(username, "Username is empty.");
        Assert.hasText(password, "Password is empty.");
        Assert.isTrue(maxActive >= 1 && maxActive <= 512, "Max connections must be between 1 and 512.");
        Assert.isTrue(keepAlive >= 10000 && keepAlive <= 120000, "TTL must be between 10000 and 120000.");

        connectorConfig.setHost(host);
        connectorConfig.setPort(port);
        connectorConfig.setUsername(username);
        connectorConfig.setPassword(password);
        connectorConfig.setServiceName(serviceName);
        connectorConfig.setDatabase(database);
        connectorConfig.setProperties(PropertiesUtil.parse(properties));
        Properties extInfoObj = JsonUtil.jsonToObj(extInfo, Properties.class);
        connectorConfig.setExtInfo(extInfoObj != null ? extInfoObj : new Properties());
        connectorConfig.setUrl(connectorService.buildJdbcUrl(connectorConfig, database));
        connectorConfig.setDriverClassName(driverClassName);
        connectorConfig.setMaxActive(maxActive);
        connectorConfig.setKeepAlive(keepAlive);
    }

    @Override
    public Table modifyExtendedTable(AbstractDatabaseConnector connectorService, Map<String, String> params) {
        Table table = new Table();
        String tableName = params.get("tableName");
        String mainTable = params.get("mainTable");
        String sql = params.get("sql");
        Assert.hasText(tableName, "TableName is empty.");
        Assert.hasText(mainTable, "MainTable is empty.");
        Assert.hasText(sql, "SQL is empty.");
        table.setName(tableName);
        table.getExtInfo().put(ConnectorConstant.CUSTOM_TABLE_SQL, sql);
        table.getExtInfo().put(ConnectorConstant.CUSTOM_TABLE_MAIN, mainTable);
        table.setType(connectorService.getExtendedTableType().getCode());
        return table;
    }
}
