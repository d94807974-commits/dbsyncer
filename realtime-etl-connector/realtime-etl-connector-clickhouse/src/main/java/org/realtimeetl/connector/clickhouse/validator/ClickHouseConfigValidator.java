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
package org.realtimeetl.connector.clickhouse.validator;

import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.clickhouse.constant.ClickHouseConfigConstant;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.AbstractDataBaseConfigValidator;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;

import java.util.Map;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-05-29 23:50
 */
public final class ClickHouseConfigValidator extends AbstractDataBaseConfigValidator {

    private static final int DEFAULT_POLL_INTERVAL_SECONDS = 5;
    private static final int DEFAULT_POLL_BATCH_SIZE = 500;

    @Override
    public void modify(AbstractDatabaseConnector connectorService, DatabaseConfig connectorConfig, Map<String, String> params) {
        super.modify(connectorService, connectorConfig, params);
        int pollInterval = NumberUtil.toInt(params.get(ClickHouseConfigConstant.POLL_INTERVAL_SECONDS), DEFAULT_POLL_INTERVAL_SECONDS);
        if (pollInterval < 1) {
            pollInterval = DEFAULT_POLL_INTERVAL_SECONDS;
        }
        int pollBatchSize = NumberUtil.toInt(params.get(ClickHouseConfigConstant.POLL_BATCH_SIZE), DEFAULT_POLL_BATCH_SIZE);
        if (pollBatchSize < 1) {
            pollBatchSize = DEFAULT_POLL_BATCH_SIZE;
        }
        connectorConfig.getExtInfo().put(ClickHouseConfigConstant.POLL_INTERVAL_SECONDS, String.valueOf(pollInterval));
        connectorConfig.getExtInfo().put(ClickHouseConfigConstant.POLL_BATCH_SIZE, String.valueOf(pollBatchSize));
        if (StringUtil.isBlank(connectorConfig.getDriverClassName())) {
            connectorConfig.setDriverClassName("com.clickhouse.jdbc.ClickHouseDriver");
        }
    }
}
