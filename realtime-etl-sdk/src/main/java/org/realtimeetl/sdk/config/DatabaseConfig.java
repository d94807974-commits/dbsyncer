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
package org.realtimeetl.sdk.config;

import org.realtimeetl.sdk.model.ConnectorConfig;

/**
 * @author Realtime ETL
 * @ClassName: DatabaseConfig
 * @Description: Data FactLibraryConnectConfigure
 * @date: 2017Year7Intern20p.m.3:40:59
 */
public class DatabaseConfig extends ConnectorConfig {

    /**
     * Mapping
     */
    private String driverClassName;

    /**
     * Host
     */
    private String host;

    /**
     * Port
     */
    private int port;

    /**
     * Account number
     */
    private String username;

    /**
     * Password
     */
    private String password;

    /**
     * Max connections
     */
    private int maxActive = 64;

    /**
     * ConnectValid until(ms)
     */
    private long keepAlive = 60000;

    /**
     * Data FactLibraryFirst Name(PGSQL、MySQL)
     */
    private String database;

    /**
     * Name of service(Oracle)
     */
    private String serviceName;

    private String url;

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public long getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(long keepAlive) {
        this.keepAlive = keepAlive;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
