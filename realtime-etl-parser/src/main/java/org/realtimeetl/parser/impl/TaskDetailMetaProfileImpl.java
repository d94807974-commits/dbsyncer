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

import org.realtimeetl.common.enums.TaskLevelEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.TaskDetailMetaProfile;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.FilterEnum;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.storage.StorageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-09-30 17:31
 */
@Component
public final class TaskDetailMetaProfileImpl implements TaskDetailMetaProfile {

    @Resource
    private StorageService storageService;

    @Resource
    private OperationTemplate operationTemplate;

    @Override
    public Meta getMeta(String tableGroupId) {
        return operationTemplate.queryObject(Meta.class, tableGroupId);
    }

    @Override
    public String add(Meta meta) {
        return "";
    }

    @Override
    public String update(Meta meta) {
        return "";
    }

    @Override
    public void remove(String tableGroupId) {

    }

    @Override
    public void clearData(String taskId) {
        storageService.clear(StorageEnum.TASK_DETAIL, taskId);
    }

    @Override
    public void dropTaskDetailTable(String taskId) {
        storageService.drop(StorageEnum.TASK_DETAIL, taskId);
    }

    @Override
    public void deleteMetaByTableGroupIds(List<String> tableGroupIds) {
        if (CollectionUtils.isEmpty(tableGroupIds)) {
            return;
        }
        Query query = new Query();
        query.setType(StorageEnum.META);
        query.addFilter(ConfigConstant.META_IS_TASK_DETAIL, TaskLevelEnum.TASK_DETAIL.getCode());
        query.addFilter(ConfigConstant.META_TASK_ID, FilterEnum.IN, StringUtil.join(tableGroupIds, StringUtil.COMMA));
        storageService.delete(query);
    }
}
