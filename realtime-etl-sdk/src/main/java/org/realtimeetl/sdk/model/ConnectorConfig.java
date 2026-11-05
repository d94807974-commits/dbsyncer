package org.realtimeetl.sdk.model;

import org.realtimeetl.sdk.util.PropertiesUtil;

import java.util.Properties;

/**
 * ConnectorConfigure
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/9/21 0:01
 */
public abstract class ConnectorConfig {

    /**
     * ConnectorType
     */
    private String connectorType;

    /**
     * Connection parameters
     */
    private Properties properties = new Properties();

    /**
     * Extension Configuration
     */
    private Properties extInfo = new Properties();

    public abstract String getUrl();

    public String getConnectorType() {
        return connectorType;
    }

    public ConnectorConfig setConnectorType(String connectorType) {
        this.connectorType = connectorType;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getPropertiesText() {
        return PropertiesUtil.toString(properties);
    }

    public Properties getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(Properties extInfo) {
        this.extInfo = extInfo;
    }
}
