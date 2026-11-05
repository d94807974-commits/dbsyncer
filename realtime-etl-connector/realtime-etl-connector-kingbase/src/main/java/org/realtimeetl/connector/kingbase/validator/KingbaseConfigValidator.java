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
package org.realtimeetl.connector.kingbase.validator;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.kingbase.constant.KingbaseConfigConstant;
import org.realtimeetl.connector.postgresql.validator.PostgreSQLConfigValidator;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 18:30
 */
public final class KingbaseConfigValidator extends PostgreSQLConfigValidator {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName("com.kingbase8.Driver");
        }
        String pluginName = params.get(KingbaseConfigConstant.PLUGIN_NAME);
        if (StringUtil.isBlank(pluginName) || "pgoutput".equalsIgnoreCase(pluginName)) {
            connectorConfig.getExtInfo().put(KingbaseConfigConstant.PLUGIN_NAME, KingbaseConfigConstant.DEFAULT_PLUGIN);
        }
    }
}