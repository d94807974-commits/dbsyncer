/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz.checker;

import org.realtimeetl.biz.enums.SafeInfoEnum;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.model.AbstractConfigModel;
import org.realtimeetl.parser.model.Convert;
import org.realtimeetl.plugin.PluginFactory;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.model.Filter;
import org.realtimeetl.sdk.model.Plugin;
import org.realtimeetl.storage.impl.SnowflakeIdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020/1/8 15:17
 */
public abstract class AbstractChecker implements Checker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SYMBOL = "***";

    @Resource
    private PluginFactory pluginFactory;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    /**
     * UpdateBasicConfigure
     * <p>id,type,name,createTime,updateTime</p>
     */
    protected void modifyConfigModel(ConfigModel model, Map<String, String> params) {
        Assert.notNull(model, "ConfigModel can not be null.");
        Assert.hasText(model.getType(), "ConfigModel type can not be empty.");
        Assert.hasText(model.getName(), "ConfigModel name can not be empty.");
        // Name
        String name = params.get(ConfigConstant.CONFIG_MODEL_NAME);
        if (StringUtil.isNotBlank(name)) {
            model.setName(name);
        }
        model.setId(StringUtil.isBlank(model.getId()) ? String.valueOf(snowflakeIdWorker.nextId()) : model.getId());
        long now = Instant.now().toEpochMilli();
        model.setCreateTime(null == model.getCreateTime() ? now : model.getCreateTime());
        model.setUpdateTime(now);
    }

    /**
     * UpdateAdvanced settings：Filter Conditions/Convert Configuration/Plugin Configuration
     */
    protected void modifySuperConfigModel(AbstractConfigModel model, Map<String, String> params) {
        // GlobalParameter
        String mappingParams = params.get("params");
        model.setParams(StringUtil.isNotBlank(mappingParams) ? JsonUtil.jsonToObj(mappingParams, Map.class) : new ConcurrentHashMap<>());

        // Filter Conditions
        String filterJson = params.get("filter");
        if (StringUtil.isNotBlank(filterJson)) {
            List<Filter> list = JsonUtil.jsonToArray(filterJson, Filter.class);
            model.setFilter(list);
        }

        // Convert Configuration
        String convertJson = params.get("convert");
        if (StringUtil.isNotBlank(convertJson)) {
            List<Convert> convert = JsonUtil.jsonToArray(convertJson, Convert.class);
            model.setConvert(convert);
        }

        // Plugin Configuration
        String pluginId = params.get("pluginId");
        Plugin plugin = null;
        if (StringUtil.isNotBlank(pluginId)) {
            List<Plugin> plugins = pluginFactory.getPluginAll();
            if (!CollectionUtils.isEmpty(plugins)) {
                for (Plugin p : plugins) {
                    if (StringUtil.equals(pluginFactory.createPluginId(p.getClassName(), p.getVersion()), pluginId)) {
                        plugin = p;
                        break;
                    }
                }
            }
        }
        model.setPlugin(plugin);
        // PluginParameter
        model.setPluginExtInfo(plugin != null ? params.get("pluginExtInfo") : null);
    }

    /**
     * PrintParameter（Data desensitization)
     */
    protected void printParams(Map<String, String> params) {
        Map<Object, Object> checkParams = new HashMap<>(params);
        for (SafeInfoEnum s : SafeInfoEnum.values()) {
            if (params.containsKey(s.getCode())) {
                checkParams.put(s.getCode(), SYMBOL);
            }
        }
        logger.info("params:{}", checkParams);
    }
}
