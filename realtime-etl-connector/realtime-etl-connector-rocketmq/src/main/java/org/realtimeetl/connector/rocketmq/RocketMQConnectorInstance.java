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
package org.realtimeetl.connector.rocketmq;

import org.realtimeetl.connector.rocketmq.config.RocketMQConfig;
import org.realtimeetl.connector.rocketmq.util.RocketMQUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 01:00
 */
public final class RocketMQConnectorInstance implements ConnectorInstance<RocketMQConfig, DefaultMQProducer> {

    private RocketMQConfig config;
    private DefaultMQProducer producer;

    public RocketMQConnectorInstance(RocketMQConfig config) {
        this.config = config;
        try {
            producer = RocketMQUtil.createProducer(config);
            ping();
        } catch (Exception e) {
            close();
            throw new RocketMQException("Failed to create RocketMQ Producer, URL: " + config.getUrl() + ", Error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getServiceUrl() {
        return config.getUrl();
    }

    @Override
    public RocketMQConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(RocketMQConfig config) {
        this.config = config;
    }

    @Override
    public DefaultMQProducer getConnection() {
        return producer;
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public DefaultMQPushConsumer createConsumer(String topic, String groupId, String tags) throws MQClientException {
        return RocketMQUtil.createConsumer(config, topic, groupId, tags);
    }

    public void ping() {
        if (producer == null) {
            throw new RocketMQException("RocketMQ Producer not initialized");
        }
    }

    @Override
    public void close() {
        if (producer != null) {
            producer.shutdown();
            producer = null;
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
