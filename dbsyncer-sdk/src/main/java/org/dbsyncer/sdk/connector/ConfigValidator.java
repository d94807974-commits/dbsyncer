/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.connector;

import org.dbsyncer.sdk.model.ConnectorConfig;
import org.dbsyncer.sdk.model.Table;
import org.dbsyncer.sdk.spi.ConnectorService;

import java.util.Map;

/**
 * ConnectorConfigureVerifyer
 *
 * @Author AE86
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
