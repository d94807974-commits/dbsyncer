/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.vo;

/**
 * Sync taskList：ConnectorType（Keep Frontend{@code config.connectorType} Path.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-29
 */
public class MappingListConnectorConfigVO {

    private String connectorType;

    public MappingListConnectorConfigVO(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }
}
