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
package org.realtimeetl.connector.http.util;

import org.realtimeetl.sdk.util.PropertiesUtil;

import java.util.Properties;

/**
 * Http ConnectorTool class
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-02-12 01:04
 */
public abstract class HttpUtil {

    /**
     * ParsingConnection parameters（Support line breaks or& minamend
     */
    public static Properties parse(String properties) {
        if (properties == null || properties.trim().isEmpty()) {
            return new Properties();
        }
        String normalized = properties.replaceAll("\r\n", "&").replaceAll("\n", "&");
        return PropertiesUtil.parse(normalized);
    }

    /**
     * WillProperties Convert to String (line break)minIt's easy.Page)
     */
    public static String toString(Properties properties) {
        if (properties == null || properties.isEmpty()) {
            return "";
        }
        String text = PropertiesUtil.toString(properties);
        return text.replaceAll("&", "\n");
    }
}
