/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.parser.model;

import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.sdk.model.Filter;
import org.dbsyncer.sdk.model.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfigModel extends ConfigModel {

    // GlobalParameter
    private Map<String, String> params = new HashMap<>();

    // Filter Conditions
    private List<Filter> filter = new ArrayList<>();

    // Convert Configuration
    private List<Convert> convert = new ArrayList<>();

    // Plugin Configuration
    private Plugin plugin;

    // PluginParameter
    private String pluginExtInfo;

    public Map<String, String> getParams() {
        return params;
    }

    public AbstractConfigModel setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public List<Filter> getFilter() {
        return filter;
    }

    public void setFilter(List<Filter> filter) {
        this.filter = filter;
    }

    public List<Convert> getConvert() {
        return convert;
    }

    public void setConvert(List<Convert> convert) {
        this.convert = convert;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public String getPluginExtInfo() {
        return pluginExtInfo;
    }

    public void setPluginExtInfo(String pluginExtInfo) {
        this.pluginExtInfo = pluginExtInfo;
    }
}
