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
