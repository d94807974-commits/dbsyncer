/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.parser.model.SystemConfig;

/**
 * System settings（{@code realtime_etl_config}）Read and write.
 *
 * @author wuji
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
