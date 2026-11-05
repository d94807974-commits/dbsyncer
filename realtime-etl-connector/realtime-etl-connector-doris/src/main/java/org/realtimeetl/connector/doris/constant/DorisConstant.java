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
package org.realtimeetl.connector.doris.constant;

import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.mysql.constant.MySQLConstant;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.constant.ConnectorConstant;

import java.util.Properties;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 21:00
 */
public final class DorisConstant {

    public static final int DEFAULT_HTTP_PORT = 8030;

    public static final String HTTP_PORT = "httpPort";

    public static final String WRITE_MODE = "writeMode";

    public static final String WRITE_MODE_JDBC = "jdbc";

    public static final String WRITE_MODE_STREAM_LOAD = "stream_load";

    private DorisConstant() {
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
        // and StarRocks/MySQL Default Unanimous, AvoidDATETIME/TIMESTAMP Yes.JDBC He showed up when he was tied up.±8 hoursOffset
        putIfAbsent(properties, "serverTimezone", "Asia/Shanghai");
    }

    public static void enrichExtInfo(DatabaseConfig config, String httpPort, String writeMode) {
        Properties extInfo = config.getExtInfo();
        if (extInfo == null) {
            extInfo = new Properties();
            config.setExtInfo(extInfo);
        }
        int port = NumberUtil.toInt(httpPort, DEFAULT_HTTP_PORT);
        if (port <= 0) {
            port = DEFAULT_HTTP_PORT;
        }
        extInfo.setProperty(HTTP_PORT, String.valueOf(port));
        if (StringUtil.isBlank(writeMode)) {
            writeMode = WRITE_MODE_JDBC;
        }
        extInfo.setProperty(WRITE_MODE, writeMode.trim());
    }

    public static int getHttpPort(DatabaseConfig config) {
        if (config == null || config.getExtInfo() == null) {
            return DEFAULT_HTTP_PORT;
        }
        return NumberUtil.toInt(config.getExtInfo().getProperty(HTTP_PORT), DEFAULT_HTTP_PORT);
    }

    public static boolean isStreamLoadMode(DatabaseConfig config) {
        if (config == null || config.getExtInfo() == null) {
            return false;
        }
        return StringUtil.equalsIgnoreCase(WRITE_MODE_STREAM_LOAD, config.getExtInfo().getProperty(WRITE_MODE));
    }

    public static boolean isStreamLoadEvent(String event) {
        return StringUtil.equals(event, ConnectorConstant.OPERTION_INSERT)
                || StringUtil.equals(event, ConnectorConstant.OPERTION_UPSERT)
                || StringUtil.equals(event, ConnectorConstant.OPERTION_UPDATE);
    }

    private static void putIfAbsent(Properties properties, String key, String value) {
        if (!properties.containsKey(key)) {
            properties.setProperty(key, value);
        }
    }
}
