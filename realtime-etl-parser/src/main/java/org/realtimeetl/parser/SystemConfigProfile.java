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
package org.realtimeetl.parser;

import org.realtimeetl.parser.model.SystemConfig;

/**
 * System settings（{@code realtime_etl_config}）Read and write.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public interface SystemConfigProfile {

    /**
     * AccessSystem settings。
     */
    SystemConfig getSystemConfig();

    /**
     * SaveSystem settings（Insertor update).
     */
    void saveSystemConfig(SystemConfig config);

    /**
     * System settingsLine number.
     */
    int countSystemConfigs();

    /**
     * Fromsystem.json ArrayImport。
     */
    void importFromJson(String json);

    /**
     * GenerateandSave API KeyOpenAPI Login)
     * BackExpress key, show it only once. Keep it safe.
     *
     * @return NewGenerateYes.API Key
     */
    String generateApiSecret();
}
