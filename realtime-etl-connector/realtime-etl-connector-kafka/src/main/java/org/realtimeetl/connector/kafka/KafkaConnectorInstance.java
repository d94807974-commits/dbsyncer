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
package org.realtimeetl.connector.kafka;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.connector.kafka.config.KafkaConfig;
import org.realtimeetl.connector.kafka.util.KafkaUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * KafkaConnectorExamples
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2021-11-22 23:55
 */
public final class KafkaConnectorInstance implements ConnectorInstance<KafkaConfig, AdminClient> {

    private KafkaConfig config;
    private final AdminClient client;
    private final Map<String, KafkaProducer<String, Object>> producers = new ConcurrentHashMap<>();
    private final List<InetSocketAddress> clusterNodes = new ArrayList<>();

    public KafkaConnectorInstance(KafkaConfig config) {
        this.config = config;
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getUrl());
        props.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 60000); // ConnectFreeTimeout
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000); // Request Timeout
        props.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, 1000); // Try againInterval
        props.put(AdminClientConfig.METADATA_MAX_AGE_CONFIG, 300000); // Maximum cache of metadataTime（5minutes）
        props.putAll(config.getProperties());
        props.remove(KafkaUtil.PRODUCER_PROPERTIES);
        props.remove(KafkaUtil.CONSUMER_PROPERTIES);
        try {
            client = AdminClient.create(props);
            getClusterNodes();
            ping();
        } catch (Exception e) {
            close();
            throw new RuntimeException("Could not close temporary folder: %s " + config.getUrl() + ", Error: " + e.getMessage(), e);
        }
    }

    @Override
    public String getServiceUrl() {
        return config.getUrl();
    }

    @Override
    public KafkaConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(KafkaConfig config) {
        this.config = config;
    }

    @Override
    public AdminClient getConnection() {
        return client;
    }

    public void ping() {
        try {
            getClusterNodes();
        } catch (Exception e) {
            // nothing to do
        }
        clusterNodes.forEach(node-> {
            try (Socket socket = new Socket()) {
                socket.connect(node, 5000);
                socket.isConnected();
            } catch (IOException e) {
                throw new KafkaException(String.format("DNS resolution failed for url in %s %s:%s", CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, node.getHostName(), node.getPort()));
            }
        });
    }

    private void getClusterNodes() throws ExecutionException, InterruptedException {
        if (client instanceof KafkaAdminClient) {
            KafkaAdminClient kafkaAdminClient = (KafkaAdminClient) client;
            DescribeClusterResult describeClusterResult = kafkaAdminClient.describeCluster();
            KafkaFuture<Collection<Node>> nodes = describeClusterResult.nodes();
            Collection<Node> nodeList = nodes.get();
            if (!CollectionUtils.isEmpty(nodeList)) {
                clusterNodes.clear();
                nodeList.forEach(node->clusterNodes.add(new InetSocketAddress(node.host(), node.port())));
            }
        }
    }

    @Override
    public void close() {
        if (!CollectionUtils.isEmpty(producers)) {
            producers.values().forEach(KafkaProducer::close);
        }
        if (client != null) {
            client.close();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getClusterId() throws ExecutionException, InterruptedException, TimeoutException {
        return client.describeCluster().clusterId().get(10, TimeUnit.SECONDS);
    }

    public KafkaProducer<String, Object> getProducer(String topic) {
        return producers.computeIfAbsent(topic, k->KafkaUtil.createProducer(config));
    }

    public KafkaConsumer<String, Object> getConsumer(String topic, String groupId) {
        return KafkaUtil.createConsumer(config, topic, groupId);
    }
}