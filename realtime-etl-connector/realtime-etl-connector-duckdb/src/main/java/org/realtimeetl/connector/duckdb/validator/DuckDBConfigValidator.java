/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.duckdb.validator;

import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.AbstractDataBaseConfigValidator;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;
import org.realtimeetl.sdk.util.PropertiesUtil;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;

/**
 * DuckDB ConnectConfigureVerifyerDocumentationLibrary，serviceName YesDatabase filePath)
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-16 10:00
 */
public final class DuckDBConfigValidator extends AbstractDataBaseConfigValidator {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        String properties = params.get("properties");
        String serviceName = params.get("serviceName");
        String driverClassName = params.get("driverClassName");
        int maxActive = NumberUtil.toInt(Objects.toString(params.get("maxActive")), connectorConfig.getMaxActive());
        long keepAlive = NumberUtil.toLong(Objects.toString(params.get("keepAlive")), connectorConfig.getKeepAlive());
        Assert.hasText(serviceName, "Database file path cannot be empty.");
        // DuckDB I'm not going to read it.ConnectNot too big.
        Assert.isTrue(maxActive >= 1 && maxActive <= 64, "Maximum number of connections allowed only 1 - 64");
        Assert.isTrue(keepAlive >= 10000 && keepAlive <= 120000, "TTL must be between 10000 and 120000.");

        connectorConfig.setServiceName(serviceName);
        connectorConfig.setProperties(PropertiesUtil.parse(properties));
        connectorConfig.setUrl(connectorService.buildJdbcUrl(connectorConfig, serviceName));
        connectorConfig.setDriverClassName(driverClassName);
        connectorConfig.setMaxActive(maxActive);
        connectorConfig.setKeepAlive(keepAlive);
    }
}
