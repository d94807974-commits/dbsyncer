/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.mongodb;

import com.mongodb.client.MongoClient;
import org.realtimeetl.connector.mongodb.config.MongoDBConfig;
import org.realtimeetl.connector.mongodb.util.MongoUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;

/**
 * @author The clouds.
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
