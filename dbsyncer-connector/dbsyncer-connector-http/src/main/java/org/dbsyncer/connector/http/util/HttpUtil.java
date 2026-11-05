/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.http.util;

import org.dbsyncer.sdk.util.PropertiesUtil;

import java.util.Properties;

/**
 * Http ConnectorTool class
 *
 * @author The clouds.
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
