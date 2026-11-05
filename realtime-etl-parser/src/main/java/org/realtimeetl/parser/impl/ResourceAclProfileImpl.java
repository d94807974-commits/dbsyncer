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
import org.realtimeetl.parser.ResourceAclProfile;
import org.realtimeetl.parser.enums.CommandEnum;
import org.realtimeetl.parser.model.ResourceAcl;
import org.realtimeetl.parser.util.ConfigModelUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.storage.StorageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link ResourceAclProfile} Achieved.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-21
 */
@Component
public class ResourceAclProfileImpl implements ResourceAclProfile {

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    private StorageService storageService;

    @Override
    public ResourceAcl get(String id) {
        return operationTemplate.queryObject(ResourceAcl.class, id);
    }

    @Override
    public List<ResourceAcl> listAll() {
        return operationTemplate.queryList(StorageEnum.RESOURCE_ACL, new Query(), ResourceAcl.class);
    }

    @Override
    public Paging query(Query query) {
        if (query == null) {
            query = new Query();
        }
        query.setType(StorageEnum.RESOURCE_ACL);
        Paging paging = storageService.query(query);
        if (paging == null) {
            return new Paging(query.getPageNum(), query.getPageSize());
        }
        List<Map> rows = (List<Map>) paging.getData();
        if (CollectionUtils.isEmpty(rows)) {
            paging.setData(Collections.emptyList());
            return paging;
        }
        List<ResourceAcl> list = new ArrayList<>(rows.size());
        for (Map row : rows) {
            ResourceAcl model = ConfigModelUtil.parseFromRow(row, ResourceAcl.class);
            if (model != null) {
                model.setType(ConfigConstant.RESOURCE_ACL);
                list.add(model);
            }
        }
        paging.setData(list);
        return paging;
    }

    @Override
    public List<ResourceAcl> listBySubject(int subjectType, String subjectId) {
        if (StringUtil.isBlank(subjectId)) {
            return Collections.emptyList();
        }
        Query condition = new Query();
        condition.addFilter(ConfigConstant.RESOURCE_ACL_SUBJECT_TYPE, subjectType);
        condition.addFilter(ConfigConstant.RESOURCE_ACL_SUBJECT_ID, subjectId);
        return operationTemplate.queryList(StorageEnum.RESOURCE_ACL, condition, ResourceAcl.class);
    }

    @Override
    public ResourceAcl getByUnique(int subjectType, String subjectId, String resourceType, String resourceId) {
        if (StringUtil.isBlank(subjectId)
                || StringUtil.isBlank(resourceType) || StringUtil.isBlank(resourceId)) {
            return null;
        }
        Query query = new Query(1, 1);
        query.setType(StorageEnum.RESOURCE_ACL);
        query.addFilter(ConfigConstant.RESOURCE_ACL_SUBJECT_TYPE, subjectType);
        query.addFilter(ConfigConstant.RESOURCE_ACL_SUBJECT_ID, subjectId);
        query.addFilter(ConfigConstant.GROUP_REL_RESOURCE_TYPE, resourceType);
        query.addFilter(ConfigConstant.GROUP_REL_RESOURCE_ID, resourceId);
        Paging paging = storageService.query(query);
        if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
            return null;
        }
        Object first = paging.getData().iterator().next();
        if (!(first instanceof Map)) {
            return null;
        }
        ResourceAcl model = ConfigModelUtil.parseFromRow((Map) first, ResourceAcl.class);
        if (model != null) {
            model.setType(ConfigConstant.RESOURCE_ACL);
        }
        return model;
    }

    @Override
    public String add(ResourceAcl acl) {
        return operationTemplate.execute(acl, CommandEnum.OPR_ADD);
    }

    @Override
    public String update(ResourceAcl acl) {
        return operationTemplate.execute(acl, CommandEnum.OPR_EDIT);
    }

    @Override
    public void remove(String id) {
        if (StringUtil.isBlank(id)) {
            return;
        }
        storageService.remove(StorageEnum.RESOURCE_ACL, id);
    }

    @Override
    public void removeByResource(String resourceType, String resourceId) {
        if (StringUtil.isBlank(resourceType) || StringUtil.isBlank(resourceId)) {
            return;
        }
        Query query = new Query();
        query.setType(StorageEnum.RESOURCE_ACL);
        query.addFilter(ConfigConstant.GROUP_REL_RESOURCE_TYPE, resourceType);
        query.addFilter(ConfigConstant.GROUP_REL_RESOURCE_ID, resourceId);
        storageService.delete(query);
    }

    @Override
    public void removeBySubject(int subjectType, String subjectId) {
        if (StringUtil.isBlank(subjectId)) {
            return;
        }
        Query query = new Query();
        query.setType(StorageEnum.RESOURCE_ACL);
        query.addFilter(ConfigConstant.RESOURCE_ACL_SUBJECT_TYPE, subjectType);
        query.addFilter(ConfigConstant.RESOURCE_ACL_SUBJECT_ID, subjectId);
        storageService.delete(query);
    }
}
