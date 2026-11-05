/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.config;

import org.dbsyncer.sdk.model.ConnectorConfig;

/**
 * @author AE86
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
