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
package org.realtimeetl.sdk.connector.database;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.database.ds.SimpleConnection;
import org.realtimeetl.sdk.connector.database.ds.SimpleDataSource;
import org.realtimeetl.sdk.util.ConnectionUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnectorInstance implements ConnectorInstance<DatabaseConfig, Connection> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DatabaseConfig config;
    private final SimpleDataSource dataSource;
    private final String catalog;
    private final String schema;

    public DatabaseConnectorInstance(DatabaseConfig config) {
        this(config, null, null);
    }

    public DatabaseConnectorInstance(DatabaseConfig config, String catalog, String schema) {
        this.config = config;
        this.catalog = catalog;
        this.schema = schema;
        Properties properties = new Properties();
        properties.putAll(config.getProperties());
        if (StringUtil.isNotBlank(config.getUsername())) {
            properties.put("user", config.getUsername());
        }
        if (StringUtil.isNotBlank(config.getPassword())) {
            properties.put("password", config.getPassword());
        }
        this.dataSource = new SimpleDataSource(config.getDriverClassName(), config.getUrl(), properties, config.getMaxActive(), config.getKeepAlive());
    }

    public <T> T execute(HandleCallback callback) {
        Connection connection = null;
        boolean discardConnection = false;
        try {
            connection = getConnection();
            return (T) callback.apply(new DatabaseTemplate((SimpleConnection) connection));
        } catch (EmptyResultDataAccessException e) {
            throw e;
        } catch (Exception e) {
            discardConnection = ConnectionUtil.isBrokenConnection(e);
            logger.error(e.getMessage());
            throw new SdkException(e.getMessage(), e.getCause());
        } finally {
            if (connection != null) {
                if (discardConnection) {
                    dataSource.discard(connection);
                } else {
                    dataSource.close(connection);
                }
            }
        }
    }

    @Override
    public String getServiceUrl() {
        return config.getUrl();
    }

    @Override
    public DatabaseConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(DatabaseConfig config) {
        this.config = config;
    }

    @Override
    public Connection getConnection() throws Exception {
        Connection connection = dataSource.getConnection();
        // DynamicShit.SettingsData FactLibraryand mode
        if (connection instanceof SimpleConnection) {
            SimpleConnection simpleConnection = (SimpleConnection) connection;
            try {
                if (StringUtil.isNotBlank(catalog)) {
                    simpleConnection.setCatalog(catalog);
                }
                if (StringUtil.isNotBlank(schema)) {
                    simpleConnection.setSchema(schema);
                }
            } catch (SQLException e) {
                logger.warn("Failed to set catalog/schema: {}", e.getMessage());
                // Do not throwError，verifieConnectContinue
            }
        }
        return connection;
    }

    @Override
    public void close() {
        dataSource.close();
    }

    public SimpleDataSource getDataSource() {
        return dataSource;
    }

    public String getCatalog() {
        return catalog;
    }
    public String getSchema() {
        return schema;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
