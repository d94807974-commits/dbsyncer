package org.realtimeetl.biz.vo;

import org.realtimeetl.sdk.model.Plugin;

public class PluginVO extends Plugin {

    /**
     * In usePluginYes.MappingName，Multiple","Collapse
     */
    private String mappingName;

    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }
}
