/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.kafka.config;

import org.realtimeetl.connector.kafka.util.KafkaUtil;
import org.realtimeetl.sdk.model.ConnectorConfig;

/**
 * KafkaConnectConfigure
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2021-11-04 20:10
 */
public class KafkaConfig extends ConnectorConfig {

    private String url;

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getPropertiesText() {
        // Supports line break display
        return KafkaUtil.toString(getProperties());
    }
}
