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
package org.realtimeetl.connector.h2.validator;

import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.AbstractDataBaseConfigValidator;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;
import org.realtimeetl.sdk.util.PropertiesUtil;

import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

/**
 * H2ConnectConfigureVerifyer
 */
public final class H2ConfigValidator extends AbstractDataBaseConfigValidator {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        String properties = params.get("properties");
        String serviceName = params.get("serviceName");
        String username = params.get("username");
        String password = params.get("password");
        String driverClassName = params.get("driverClassName");
        int maxActive = NumberUtil.toInt(Objects.toString(params.get("maxActive")), connectorConfig.getMaxActive());
        long keepAlive = NumberUtil.toLong(Objects.toString(params.get("keepAlive")), connectorConfig.getKeepAlive());
        Assert.hasText(serviceName, "Database file path cannot be empty.");
        Assert.isTrue(maxActive >= 1 && maxActive <= 512, "Max connections must be between 1 and 512.");
        Assert.isTrue(keepAlive >= 10000 && keepAlive <= 120000, "TTL must be between 10000 and 120000.");

        connectorConfig.setServiceName(serviceName);
        connectorConfig.setUsername(StringUtil.isNotBlank(username) ? username.trim() : "sa");
        connectorConfig.setPassword(password != null ? password : StringUtil.EMPTY);
        connectorConfig.setProperties(PropertiesUtil.parse(properties));
        connectorConfig.setUrl(connectorService.buildJdbcUrl(connectorConfig, serviceName));
        connectorConfig.setDriverClassName(StringUtil.isNotBlank(driverClassName) ? driverClassName : "org.h2.Driver");
        connectorConfig.setMaxActive(maxActive);
        connectorConfig.setKeepAlive(keepAlive);
    }
}
