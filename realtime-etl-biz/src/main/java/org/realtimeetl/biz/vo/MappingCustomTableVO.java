/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * MappingCustom tableInfo
 *
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2026-01-04 21:41
 */
public class MappingCustomTableVO {

    /**
     * MappingID
     */
    private String id;

    /**
     * MappingName
     */
    private String name;

    /**
     * MetaInfo
     */
    private Meta meta;

    /**
     * ConnectConfigure
     */
    private ConnectorConfig connectorConfig;

    /**
     * ExpandableType {@link org.realtimeetl.sdk.enums.TableTypeEnum}
     */
    private String extendedType;

    /**
     * Main TableList
     */
    private List<Table> mainTables;

    /**
     * Custom tableList
     */
    private List<TableVO> customTables = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public ConnectorConfig getConnectorConfig() {
        return connectorConfig;
    }

    public void setConnectorConfig(ConnectorConfig connectorConfig) {
        this.connectorConfig = connectorConfig;
    }

    public String getExtendedType() {
        return extendedType;
    }

    public void setExtendedType(String extendedType) {
        this.extendedType = extendedType;
    }

    public List<Table> getMainTables() {
        return mainTables;
    }

    public void setMainTables(List<Table> mainTables) {
        this.mainTables = mainTables;
    }

    public List<TableVO> getCustomTables() {
        return customTables;
    }

    public void setCustomTables(List<TableVO> customTables) {
        this.customTables = customTables;
    }
}
