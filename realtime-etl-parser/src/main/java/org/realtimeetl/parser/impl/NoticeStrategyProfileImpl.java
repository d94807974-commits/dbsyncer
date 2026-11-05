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

import org.realtimeetl.common.event.RemoveNoticeStrategyCacheEvent;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.AbstractConfigModelProfile;
import org.realtimeetl.parser.NoticeStrategyProfile;
import org.realtimeetl.parser.enums.CommandEnum;
import org.realtimeetl.parser.model.NoticeStrategyConfig;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.storage.StorageService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * {@link NoticeStrategyProfile} Achieved Inclusive: single reading{@link AbstractConfigModelProfile} Cache,
 * Contents Locally invalid and notify other clustersNodes。
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Component
public class NoticeStrategyProfileImpl extends AbstractConfigModelProfile<NoticeStrategyConfig>
        implements NoticeStrategyProfile, ApplicationListener<RemoveNoticeStrategyCacheEvent> {

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    private StorageService storageService;

    @Override
    public List<NoticeStrategyConfig> listAll() {
        return operationTemplate.queryList(StorageEnum.NOTICE_STRATEGY, new Query(), NoticeStrategyConfig.class);
    }

    @Override
    public NoticeStrategyConfig get(String id) {
        if (StringUtil.isBlank(id)) {
            return null;
        }
        return getCache(id);
    }

    @Override
    public String add(NoticeStrategyConfig config) {
        String id = operationTemplate.execute(config, CommandEnum.OPR_ADD);
        // InsertWe need to notify the cluster.NodesRebuild policy running state
        removeCacheAndNotice(id);
        return id;
    }

    @Override
    public String update(NoticeStrategyConfig config) {
        String id = operationTemplate.execute(config, CommandEnum.OPR_EDIT);
        removeCacheAndNotice(id);
        return id;
    }

    @Override
    public void remove(String id) {
        if (StringUtil.isBlank(id)) {
            return;
        }
        storageService.remove(StorageEnum.NOTICE_STRATEGY, id);
        removeCacheAndNotice(id);
    }

    @Override
    public void onApplicationEvent(RemoveNoticeStrategyCacheEvent event) {
        removeCache(event.getCommonMessage().getId());
    }
}
