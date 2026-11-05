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
package org.realtimeetl.connector.tidb.constant;

import org.realtimeetl.connector.mysql.constant.MySQLConstant;
import org.realtimeetl.sdk.config.DatabaseConfig;

import java.util.Properties;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 00:10
 */
public final class TiDBConstant {

    private TiDBConstant() {
    }

    public static void enrichJdbcProperties(DatabaseConfig config) {
        MySQLConstant.enrichJdbcProperties(config);
        Properties properties = config.getProperties();
        if (properties == null) {
            properties = new Properties();
            config.setProperties(properties);
        }
        putIfAbsent(properties, "rewriteBatchedStatements", "true");
        putIfAbsent(properties, "useUnicode", "true");
        putIfAbsent(properties, "characterEncoding", "UTF-8");
        putIfAbsent(properties, "useSSL", "false");
        putIfAbsent(properties, "allowMultiQueries", "true");
        putIfAbsent(properties, "cachePrepStmts", "true");
    }

    private static void putIfAbsent(Properties properties, String key, String value) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, value);
        }
    }
}
