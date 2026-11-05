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
package org.realtimeetl.connector.mongodb;

import com.mongodb.client.MongoClient;
import org.realtimeetl.connector.mongodb.config.MongoDBConfig;
import org.realtimeetl.connector.mongodb.util.MongoUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 20:00
 */
public final class MongoConnectorInstance implements ConnectorInstance<MongoDBConfig, MongoClient> {

    private MongoDBConfig config;
    private final MongoClient client;

    public MongoConnectorInstance(MongoDBConfig config) {
        this.config = config;
        this.client = MongoUtil.createClient(config);
    }

    @Override
    public String getServiceUrl() {
        return config.getUrl();
    }

    @Override
    public MongoDBConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(MongoDBConfig config) {
        this.config = config;
    }

    @Override
    public MongoClient getConnection() {
        return client;
    }

    @Override
    public void close() {
        MongoUtil.close(client);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
