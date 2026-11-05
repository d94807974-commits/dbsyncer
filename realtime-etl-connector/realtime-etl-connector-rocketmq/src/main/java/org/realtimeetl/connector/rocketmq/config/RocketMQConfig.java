/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.rocketmq.config;

import org.realtimeetl.connector.rocketmq.util.RocketMQUtil;
import org.realtimeetl.sdk.model.ConnectorConfig;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-07 01:00
 */
public class RocketMQConfig extends ConnectorConfig {

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
        return RocketMQUtil.toString(getProperties());
    }
}
