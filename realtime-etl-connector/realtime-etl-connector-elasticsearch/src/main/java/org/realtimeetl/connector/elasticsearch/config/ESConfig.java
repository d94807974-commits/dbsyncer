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
package org.realtimeetl.connector.elasticsearch.config;

import org.realtimeetl.sdk.model.ConnectorConfig;

/**
 * ESConnectConfigure
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2021-08-23 20:00
 */
public class ESConfig extends ConnectorConfig {

    /**
     * Group Address, http(s)-9200, tcp-9300 http://192.168.1.100:9200,http://192.168.1.200:9200
     */
    private String url;

    /**
     * Account number
     */
    private String username;

    /**
     * Password
     */
    private String password;

    /**
     * QuestionRequest timeout (sec)
     */
    private int timeoutSeconds = 10;

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
