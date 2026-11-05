/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.vo;

/**
 * Sync taskList：Source/Target Connectioninstrument summary.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-29
 */
public class MappingListConnectorVO {

    private String name;
    private MappingListConnectorConfigVO config;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MappingListConnectorConfigVO getConfig() {
        return config;
    }

    public void setConfig(MappingListConnectorConfigVO config) {
        this.config = config;
    }
}
