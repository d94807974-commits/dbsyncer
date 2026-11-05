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
package org.realtimeetl.connector.elasticsearch;

import org.realtimeetl.connector.elasticsearch.api.EasyRestHighLevelClient;
import org.realtimeetl.connector.elasticsearch.config.ESConfig;
import org.realtimeetl.connector.elasticsearch.util.ESUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.Version;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.core.MainResponse;

/**
 * ESConnectorExamples
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2023-11-25 23:10
 */
public final class ESConnectorInstance implements ConnectorInstance<ESConfig, EasyRestHighLevelClient> {

    private ESConfig config;
    private EasyRestHighLevelClient client;

    public ESConnectorInstance(ESConfig config) {
        this.config = config;
        this.client = ESUtil.getConnection(config);
        try {
            MainResponse info = client.info(RequestOptions.DEFAULT);
            client.setVersion(Version.fromString(info.getVersion().getNumber()));
        } catch (Exception e) {
            throw new ElasticsearchException(String.format("Can not get message: %s %s", config.getUrl(), e.getMessage()));
        }
    }

    @Override
    public String getServiceUrl() {
        return config.getUrl();
    }

    @Override
    public ESConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(ESConfig config) {
        this.config = config;
    }

    @Override
    public EasyRestHighLevelClient getConnection() {
        return client;
    }

    public Version getVersion() {
        return client.getVersion();
    }

    @Override
    public void close() {
        ESUtil.close(client);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}