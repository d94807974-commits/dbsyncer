/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.parser.util;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-12-01 22:56
 */
public abstract class ConnectorInstanceUtil {

    /**
     * Data FactSourceConnectExample Postfix
     */
    public static final String SOURCE_SUFFIX = "S";

    /**
     * TargetConnectExample Postfix
     */
    public static final String TARGET_SUFFIX = "T";

    public static String buildConnectorInstanceId(String mappingId, String connectorId, String suffix) {
        return String.format("%s@%s@%s", mappingId, connectorId, suffix);
    }

    public static String buildConnectorInstanceId(String mappingId, String connectorId, String database,String schema, String suffix) {
        return String.format("%s@%s@%s@%s@%s", mappingId, connectorId, database,schema, suffix);
    }
}
