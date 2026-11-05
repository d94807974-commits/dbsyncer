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
import org.realtimeetl.parser.NoticeRuleProfile;
import org.realtimeetl.parser.enums.CommandEnum;
import org.realtimeetl.parser.model.NoticeRule;
import org.realtimeetl.parser.util.ConfigModelUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.storage.StorageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link NoticeRuleProfile} Achieved.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Component
public class NoticeRuleProfileImpl implements NoticeRuleProfile {

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    private StorageService storageService;

    @Override
    public List<NoticeRule> listAll() {
        return operationTemplate.queryList(StorageEnum.NOTICE_RULE, new Query(), NoticeRule.class);
    }

    @Override
    public NoticeRule get(String id) {
        if (StringUtil.isBlank(id)) {
            return null;
        }
        return operationTemplate.queryObject(NoticeRule.class, id);
    }

    @Override
    public NoticeRule getByScope(String scopeType, String scopeId) {
        if (StringUtil.isBlank(scopeType) || StringUtil.isBlank(scopeId)) {
            return null;
        }
        Query query = new Query(1, 1);
        query.setType(StorageEnum.NOTICE_RULE);
        query.addFilter(ConfigConstant.NOTICE_RULE_SCOPE_TYPE, scopeType);
        query.addFilter(ConfigConstant.NOTICE_RULE_SCOPE_ID, scopeId);
        Paging paging = storageService.query(query);
        if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
            return null;
        }
        Object first = paging.getData().iterator().next();
        if (!(first instanceof Map)) {
            return null;
        }
        return ConfigModelUtil.parseFromRow((Map) first, NoticeRule.class);
    }

    @Override
    public List<NoticeRule> listByScopeType(String scopeType) {
        if (StringUtil.isBlank(scopeType)) {
            return Collections.emptyList();
        }
        Query condition = new Query();
        condition.addFilter(ConfigConstant.NOTICE_RULE_SCOPE_TYPE, scopeType);
        return operationTemplate.queryList(StorageEnum.NOTICE_RULE, condition, NoticeRule.class);
    }

    @Override
    public String add(NoticeRule rule) {
        return operationTemplate.execute(rule, CommandEnum.OPR_ADD);
    }

    @Override
    public String update(NoticeRule rule) {
        return operationTemplate.execute(rule, CommandEnum.OPR_EDIT);
    }

    @Override
    public void remove(String id) {
        if (StringUtil.isBlank(id)) {
            return;
        }
        storageService.remove(StorageEnum.NOTICE_RULE, id);
    }

    @Override
    public void removeByScopeId(String scopeType, String scopeId) {
        if (StringUtil.isBlank(scopeType) || StringUtil.isBlank(scopeId)) {
            return;
        }
        Query query = new Query();
        query.setType(StorageEnum.NOTICE_RULE);
        query.addFilter(ConfigConstant.NOTICE_RULE_SCOPE_TYPE, scopeType);
        query.addFilter(ConfigConstant.NOTICE_RULE_SCOPE_ID, scopeId);
        storageService.delete(query);
    }
}
