/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.mysql.constant;

import org.realtimeetl.sdk.config.DatabaseConfig;

import java.util.Properties;

/**
 * MySQL ConnectorConstant
 */
public final class MySQLConstant {

    /**
     * CompletionJDBC Connection parameters，Remove AbandonedautoReconnect
     */
    public static void enrichJdbcProperties(DatabaseConfig config) {
        Properties properties = config.getProperties();
        if (properties == null) {
            properties = new Properties();
            config.setProperties(properties);
        }
        putIfAbsent(properties, "connectTimeout", "10000");
        putIfAbsent(properties, "socketTimeout", "600000");
        putIfAbsent(properties, "tcpKeepAlive", "true");
        properties.remove("autoReconnect");
        properties.remove("failOverReadOnly");
    }

    private static void putIfAbsent(Properties properties, String key, String value) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, value);
        }
    }
}
