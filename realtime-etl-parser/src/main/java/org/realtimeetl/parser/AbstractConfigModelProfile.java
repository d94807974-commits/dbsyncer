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

import org.realtimeetl.common.cache.CacheService;
import org.realtimeetl.common.message.impl.RemoveCacheMessage;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.impl.OperationTemplate;
import org.realtimeetl.sdk.spi.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;

/**
 * UniversalConfigure（CacheRefreshMaintenance)
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-09-22 21:01
 */
public abstract class AbstractConfigModelProfile<T extends ConfigModel> implements ConfigModelProfile {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Class<ConfigModel> responseClazz;
    private String configModelType;
    public static final long EXPIRED_1_HOURS = 60 * 60 * 1000;

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    protected CacheService cacheService;

    @Resource
    private ClusterService clusterService;

    public AbstractConfigModelProfile() {
        int level = 5;
        Class<?> aClass = getClass();
        while (level > 0) {
            if (aClass.getSuperclass() == AbstractConfigModelProfile.class) {
                responseClazz = (Class<ConfigModel>) ((ParameterizedType) aClass.getGenericSuperclass()).getActualTypeArguments()[0];
                break;
            }
            aClass = aClass.getSuperclass();
            level--;
        }
        Assert.notNull(responseClazz, String.format("%s' parent %s pane Response is empty.", getClass().getName(), AbstractConfigModelProfile.class.getName()));
        try {
            configModelType = responseClazz.newInstance().getType();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public T getCache(String id) {
        String cacheKey = buildCacheKey(id);
        T cached = (T) cacheService.get(cacheKey, responseClazz);
        if (cached != null) {
            return cached;
        }
        return cacheService.executeWithLock(buildLockKey(id), () -> {
            T again = (T) cacheService.get(cacheKey, responseClazz);
            if (again != null) {
                return again;
            }
            T config = (T) getConfigModel(id);
            if (config != null) {
                cacheService.put(cacheKey, config, EXPIRED_1_HOURS);
            }
            return config;
        });
    }

    @Override
    public void removeCacheAndNotice(String id) {
        removeCache(id);
        pushMessage(id);
    }

    @Override
    public void removeCache(String id) {
        cacheService.remove(buildCacheKey(id));
    }

    @Override
    public void pushMessage(String id) {
        RemoveCacheMessage message = new RemoveCacheMessage();
        message.setId(id);
        message.setConfigModelType(configModelType);
        clusterService.pushMessage(message);
    }

    protected ConfigModel getConfigModel(String id) {
        return operationTemplate.queryObject(responseClazz, id);
    }

    /**
     * Data FactC Facilitieskey：Yes Notably.id Usetype:id，None id（LikeSystemConfig）Onlytype。
     */
    protected String buildCacheKey(String id) {
        if (StringUtil.isBlank(id)) {
            return configModelType;
        }
        return configModelType + ":" + id;
    }

    protected String buildLockKey(String id) {
        String key = configModelType + ":lock";
        if (StringUtil.isBlank(id)) {
            return key;
        }
        return key + ":" + id;
    }

}
