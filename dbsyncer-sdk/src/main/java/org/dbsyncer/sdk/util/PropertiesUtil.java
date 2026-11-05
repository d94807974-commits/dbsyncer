/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.sdk.util;

import java.util.Map;
import java.util.Properties;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-12-28 23:21
 */
public abstract class PropertiesUtil {

    /**
     * Resolve string asPropertiesObject
     *
     * @param params Mostly in thekey1=value1&key2=value2
     * @return PropertiesObject
     */
    public static Properties parse(String params) {
        Properties properties = new Properties();
        if (params == null || params.trim().isEmpty()) {
            return properties;
        }
        // Press&minCutParameter
        String[] paramArray = params.split("&");
        for (String param : paramArray) {
            // Press=minCutValueYeah.
            String[] keyValue = param.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if (!key.isEmpty()) {
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }

    /**
     * WillPropertiesObjectConvertAs String
     *
     * @param properties PropertiesObject
     * @return Mostly in thekey1=value1&key2=value2，IfpropertiesYesnullOr empty,BackEmpty String
     */
    public static String toString(Properties properties) {
        if (properties == null || properties.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            String key = entry.getKey() != null ? entry.getKey().toString() : "";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            sb.append(key).append("=").append(value);
            first = false;
        }
        return sb.toString();
    }
}
