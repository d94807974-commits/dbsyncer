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
package org.realtimeetl.connector.doris.validator;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.doris.constant.DorisConstant;
import org.realtimeetl.connector.mysql.validator.MySQLConfigValidator;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 21:00
 */
public final class DorisConfigValidator extends MySQLConfigValidator {

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        DorisConstant.enrichJdbcProperties(connectorConfig);
        DorisConstant.enrichExtInfo(connectorConfig, params.get(DorisConstant.HTTP_PORT), params.get(DorisConstant.WRITE_MODE));
    }
}
