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
package org.realtimeetl.connector.mongodb.config;

import org.realtimeetl.connector.mongodb.util.MongoUtil;
import org.realtimeetl.sdk.model.ConnectorConfig;

/**
 * MongoDB ConnectConfigure
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 20:00
 */
public class MongoDBConfig extends ConnectorConfig {

    private String host;

    private int port = 27017;

    private String username;

    private String password;

    private String database;

    /**
     * Authentication Library，Defaultadmin
     */
    private String authDatabase = "admin";

    private int maxPoolSize = 64;

    private String url;

    @Override
    public String getUrl() {
        if (url == null) {
            url = MongoUtil.buildConnectionString(this);
        }
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getAuthDatabase() {
        return authDatabase;
    }

    public void setAuthDatabase(String authDatabase) {
        this.authDatabase = authDatabase;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }
}
