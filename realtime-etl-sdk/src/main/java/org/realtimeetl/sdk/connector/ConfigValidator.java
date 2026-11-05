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
package org.realtimeetl.sdk.connector;

import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.spi.ConnectorService;

import java.util.Map;

/**
 * ConnectorConfigureVerifyer
 *
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2020-01-07 23:17
 */
public interface ConfigValidator<S extends ConnectorService, C extends ConnectorConfig> {

    /**
     * UpdateConfigure
     *
     * @param connectorService
     * @param connectorConfig
     * @param params
     * @return
     */
    void modify(S connectorService, C connectorConfig, Map<String, String> params);

    /**
     * UpdateExtension TableConfigure
     *
     * @param connectorService
     * @param params
     * @return
     */
    Table modifyExtendedTable(S connectorService, Map<String, String> params);
}
