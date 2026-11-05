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

import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.enums.CommandEnum;
import org.realtimeetl.parser.model.UserConfig;
import org.realtimeetl.parser.util.ConfigModelUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.storage.StorageService;
import org.realtimeetl.storage.impl.SnowflakeIdWorker;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UniversalConfigureStorage Templates
 * User/Connector/Map/Meta See individualProfile）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/9/16 23:59
 */
@Component
public final class OperationTemplate {

    @Resource
    private StorageService storageService;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    /**
     * PressPrimary keyQuery LineConfigure。
     */
    public <T> T queryObject(Class<T> clazz, String id) {
        if (StringUtil.isBlank(id)) {
            return null;
        }
        StorageEnum type = ConfigModelUtil.getStorageEnum(clazz);
        Query query = new Query();
        query.setType(type);
        query.addFilter(ConfigConstant.CONFIG_MODEL_ID, id);
        Map row = storageService.queryObject(query);
        if (CollectionUtils.isEmpty(row)) {
            return null;
        }
        return parseRow(row, clazz);
    }

    public String execute(ConfigModel model, CommandEnum cmd) {
        Assert.isTrue(!(model instanceof UserConfig), "UserConfig must go through UserProfile.syncUserConfig");
        if (CommandEnum.OPR_ADD == cmd) {
            if (StringUtil.isBlank(model.getId())) {
                model.setId(String.valueOf(snowflakeIdWorker.nextId()));
            }
        }

        Map<String, Object> params = ConfigModelUtil.convertModelToMap(model);
        StorageEnum type = ConfigModelUtil.getStorageEnum(model.getType());
        if (CommandEnum.OPR_EDIT == cmd) {
            storageService.edit(type, params);
        } else {
            storageService.add(type, params);
        }
        return model.getId();
    }

    /**
     * Batch writingConfigure：Single storage batchADD / EDIT。
     */
    public List<String> executeBatch(List<? extends ConfigModel> models, CommandEnum commandEnum) {
        if (CollectionUtils.isEmpty(models)) {
            return Collections.emptyList();
        }
        Assert.notNull(commandEnum, "CommandEnum can not be null.");
        List<Map> paramsList = new ArrayList<>(models.size());
        for (ConfigModel model : models) {
            if (commandEnum == CommandEnum.OPR_ADD) {
                if (StringUtil.isBlank(model.getId())) {
                    model.setId(String.valueOf(snowflakeIdWorker.nextId()));
                }
            }
            paramsList.add(ConfigModelUtil.convertModelToMap(model));
        }
        StorageEnum type = ConfigModelUtil.getStorageEnum(models.get(0).getType());
        if (commandEnum.isEdit()) {
            storageService.editBatch(type, null, paramsList);
        } else {
            storageService.addBatch(type, null, paramsList);
        }
        return models.stream().map(ConfigModel::getId).collect(Collectors.toList());
    }

    /**
     * Press StorageTypeNumber of statistical lines (only)total，No, no, no.
     */
    public int count(StorageEnum type, Query condition) {
        Query query = new Query();
        query.setType(type);
        query.setQueryTotal(true);
        query.setPageNum(1);
        query.setPageSize(1);
        if (condition != null) {
            query.setBooleanFilter(condition.getBooleanFilter());
        }
        Paging paging = storageService.query(query);
        return paging == null ? 0 : (int) paging.getTotal();
    }

    /**
     * minPageQuery specified memory table, inverse sequencejson as a model.
     */
    public <T> List<T> queryList(StorageEnum type, Query condition, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        Query query = new Query();
        query.setType(type);
        query.setPageSize(ConfigConstant.PAGE_SIZE);
        if (condition != null) {
            query.setBooleanFilter(condition.getBooleanFilter());
        }
        while (true) {
            Paging paging = storageService.query(query);
            if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
                break;
            }
            List<Map> data = (List<Map>) paging.getData();
            for (Map row : data) {
                T model = parseRow(row, clazz);
                if (model != null) {
                    result.add(model);
                }
            }
            query.setPageNum(query.getPageNum() + 1);
        }
        return result;
    }

    private <T> T parseRow(Map row, Class<T> clazz) {
        return ConfigModelUtil.parseFromRow(row, clazz);
    }
}
