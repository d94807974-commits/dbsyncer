/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.kingbase;

import org.realtimeetl.connector.kingbase.cdc.KingbaseListener;
import org.realtimeetl.connector.kingbase.schema.KingbaseSchemaResolver;
import org.realtimeetl.connector.kingbase.validator.KingbaseConfigValidator;
import org.realtimeetl.connector.postgresql.PostgreSQLConnector;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.schema.SchemaResolver;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-06 18:30
 */
public final class KingbaseConnector extends PostgreSQLConnector {

    private final KingbaseConfigValidator configValidator = new KingbaseConfigValidator();
    private final KingbaseSchemaResolver schemaResolver = new KingbaseSchemaResolver();

    @Override
    public String getConnectorType() {
        return "Kingbase";
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isTiming(listenerType)) {
            return new DatabaseQuartzListener();
        }
        if (ListenerTypeEnum.isLog(listenerType)) {
            return new KingbaseListener();
        }
        return null;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    @Override
    public String buildJdbcUrl(DatabaseConfig config, String database) {
        StringBuilder url = new StringBuilder();
        url.append("jdbc:kingbase8://").append(config.getHost()).append(":").append(config.getPort()).append("/");
        if (org.realtimeetl.common.util.StringUtil.isNotBlank(database)) {
            url.append(database);
        }
        return url.toString();
    }
}
