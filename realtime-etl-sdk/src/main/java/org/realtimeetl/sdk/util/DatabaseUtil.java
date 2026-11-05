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
package org.realtimeetl.sdk.util;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.SdkException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;

import static java.util.regex.Pattern.compile;

public abstract class DatabaseUtil {

    public static Connection getConnection(String driverClassName, String url, String username, String password) throws SQLException {
        if (StringUtil.isNotBlank(driverClassName)) {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new SdkException(e.getCause());
            }
        }
        return DriverManager.getConnection(url, username, password);
    }

    public static Connection getConnection(String driverClassName, String url, Properties properties) throws SQLException {
        if (StringUtil.isNotBlank(driverClassName)) {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new SdkException(e.getCause());
            }
        }
        return DriverManager.getConnection(url, properties);
    }

    public static void close(AutoCloseable rs) {
        if (null != rs) {
            try {
                rs.close();
            } catch (Exception e) {
                throw new SdkException(e);
            }
        }
    }

    public static String getDatabaseName(String url) {
        Matcher matcher = compile("(//)(?!(\\?)).+?(\\?)").matcher(url);
        if (matcher.find()) {
            url = matcher.group(0);
        }
        int s = url.lastIndexOf("/");
        int e = url.lastIndexOf("?");
        if (s > 0 && e > 0) {
            return StringUtil.substring(url, s + 1, e);
        }

        throw new SdkException("database is invalid");
    }
}
