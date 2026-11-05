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

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.AbstractConfigModelProfile;
import org.realtimeetl.parser.GroupProfile;
import org.realtimeetl.parser.enums.CommandEnum;
import org.realtimeetl.parser.model.Group;
import org.realtimeetl.parser.util.ConfigModelUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.SortEnum;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.storage.StorageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link GroupProfile} Achieved.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-14 18:00
 */
@Component
public class GroupProfileImpl extends AbstractConfigModelProfile<Group> implements GroupProfile {

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    private StorageService storageService;

    @Override
    public Group getGroup(String id) {
        return getCache(id);
    }

    @Override
    public List<Group> listGroups() {
        Query query = new Query();
        query.setType(StorageEnum.GROUP);
        query.setPageSize(ConfigConstant.PAGE_SIZE);
        query.addOrderBy(ConfigConstant.CONFIG_MODEL_NAME, SortEnum.ASC);
        List<Group> result = new ArrayList<>();
        while (true) {
            Paging paging = storageService.query(query);
            if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
                break;
            }
            for (Object item : paging.getData()) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Group group = ConfigModelUtil.parseFromRow((Map) item, Group.class);
                if (group != null) {
                    result.add(group);
                }
            }
            query.setPageNum(query.getPageNum() + 1);
        }
        return result;
    }

    @Override
    public Group getByName(String name) {
        if (StringUtil.isBlank(name)) {
            return null;
        }
        Query query = new Query(1, 1);
        query.setType(StorageEnum.GROUP);
        query.addFilter(ConfigConstant.CONFIG_MODEL_NAME, name);
        Paging paging = storageService.query(query);
        if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
            return null;
        }
        Object first = paging.getData().iterator().next();
        if (!(first instanceof Map)) {
            return null;
        }
        return ConfigModelUtil.parseFromRow((Map) first, Group.class);
    }

    @Override
    public String addGroup(Group group) {
        String id = operationTemplate.execute(group, CommandEnum.OPR_ADD);
        removeCache(id);
        return id;
    }

    @Override
    public String updateGroup(Group group) {
        String id = operationTemplate.execute(group, CommandEnum.OPR_EDIT);
        removeCacheAndNotice(id);
        return id;
    }

    @Override
    public void removeGroup(String id) {
        if (StringUtil.isBlank(id)) {
            return;
        }
        storageService.remove(StorageEnum.GROUP, id);
        removeCacheAndNotice(id);
    }
    
}
