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
package org.realtimeetl.connector.kafka.util;

import org.realtimeetl.connector.kafka.config.KafkaConfig;
import org.realtimeetl.sdk.util.PropertiesUtil;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Collections;
import java.util.Properties;

/**
 * KafkaConnectorTool class
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2021-12-16 23:09
 */
public abstract class KafkaUtil {

    public static final String PRODUCER_PROPERTIES = "producerProperties";
    public static final String CONSUMER_PROPERTIES = "consumerProperties";

    public static KafkaProducer<String, Object> createProducer(KafkaConfig config) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getUrl());
        props.putAll(parse(config.getExtInfo().getProperty(PRODUCER_PROPERTIES)));
        return new KafkaProducer<>(props);
    }

    public static KafkaConsumer<String, Object> createConsumer(KafkaConfig config, String topic, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getUrl());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.putAll(parse(config.getExtInfo().getProperty(CONSUMER_PROPERTIES)));

        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }

    public static Properties parse(String properties) {
        properties = properties.replaceAll("\r\n", "&");
        properties = properties.replaceAll("\n", "&");
        return PropertiesUtil.parse(properties);
    }

    public static String toString(Properties properties) {
        String propertiesText = PropertiesUtil.toString(properties);
        propertiesText = propertiesText.replaceAll("&", "\r\n");
        return propertiesText;
    }
}
