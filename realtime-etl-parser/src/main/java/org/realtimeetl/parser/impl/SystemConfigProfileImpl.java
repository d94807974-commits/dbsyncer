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
package org.realtimeetl.parser.impl;

import org.realtimeetl.common.event.RemoveSystemConfigCacheEvent;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.AbstractConfigModelProfile;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.util.ConfigModelUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.storage.StorageService;
import org.realtimeetl.storage.impl.SnowflakeIdWorker;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

/**
 * {@link SystemConfigProfile} Achieved.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Component
public final class SystemConfigProfileImpl extends AbstractConfigModelProfile<SystemConfig> implements SystemConfigProfile, ApplicationListener<RemoveSystemConfigCacheEvent> {

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    private StorageService storageService;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Override
    public SystemConfig getSystemConfig() {
        return getCache(null);
    }

    @Override
    public void saveSystemConfig(SystemConfig config) {
        Assert.notNull(config, "SystemConfig can not be null.");
        long now = System.currentTimeMillis();
        if (config.getCreateTime() == null) {
            config.setCreateTime(now);
        }
        if (config.getUpdateTime() == null) {
            config.setUpdateTime(now);
        }
        if (StringUtil.isBlank(config.getId())) {
            config.setId(String.valueOf(snowflakeIdWorker.nextId()));
            storageService.add(StorageEnum.CONFIG, ConfigModelUtil.convertModelToMap(config));
        } else {
            storageService.edit(StorageEnum.CONFIG, ConfigModelUtil.convertModelToMap(config));
        }
        pushMessage(null);
        removeCache(null);
    }

    @Override
    public int countSystemConfigs() {
        return operationTemplate.count(StorageEnum.CONFIG, null);
    }

    @Override
    public void importFromJson(String json) {
        if (StringUtil.isBlank(json)) {
            return;
        }
        List<SystemConfig> configs = JsonUtil.jsonToArray(json, SystemConfig.class);
        if (CollectionUtils.isEmpty(configs)) {
            return;
        }
        for (SystemConfig config : configs) {
            saveSystemConfig(config);
        }
    }

    @Override
    protected ConfigModel getConfigModel(String id) {
        Query condition = new Query();
        condition.addFilter(ConfigConstant.CONFIG_MODEL_TYPE, ConfigConstant.SYSTEM);
        List<SystemConfig> list = operationTemplate.queryList(StorageEnum.CONFIG, condition, SystemConfig.class);
        return CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    @Override
    public void onApplicationEvent(RemoveSystemConfigCacheEvent event) {
        removeCache(null);
    }

    @Override
    public String generateApiSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
