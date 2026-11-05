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
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.NoticeStrategyService;
import org.realtimeetl.common.util.BooleanUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.NoticeStrategyProfile;
import org.realtimeetl.parser.message.NoticeStrategyConverter;
import org.realtimeetl.parser.message.NoticeStrategyRuntimeBridge;
import org.realtimeetl.parser.model.NoticeStrategyConfig;
import org.realtimeetl.sdk.enums.NoticeFrequencyEnum;
import org.realtimeetl.sdk.enums.NoticeTypeEnum;
import org.realtimeetl.sdk.model.NoticeStrategyDef;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * {@link NoticeStrategyService} Achieved.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Service
public class NoticeStrategyServiceImpl implements NoticeStrategyService {

    @Resource
    private NoticeStrategyProfile noticeStrategyProfile;

    @Resource
    private NoticeStrategyRuntimeBridge noticeStrategyRuntimeBridge;

    @Override
    public List<NoticeStrategyDef> listAll() {
        return NoticeStrategyConverter.toDefs(noticeStrategyProfile.listAll());
    }

    @Override
    public synchronized String save(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        NoticeStrategyDef def = parseOne(params);
        String id = StringUtil.trim(params.get("id"));
        assertNoticeTypeUnique(def.getNoticeTypes(), id);
        long now = System.currentTimeMillis();
        if (StringUtil.isNotBlank(id)) {
            NoticeStrategyConfig existing = noticeStrategyProfile.get(id);
            Assert.notNull(existing, "Strategy does not exist");
            existing.setName(def.getName());
            existing.setEnabled(def.isEnabled() ? 1 : 0);
            existing.setFrequency(def.getFrequency());
            existing.setCron(def.getCron());
            existing.setNoticeTypes(def.getNoticeTypes());
            existing.setRemark(def.getRemark());
            existing.setUpdateTime(now);
            String result = noticeStrategyProfile.update(existing);
            noticeStrategyRuntimeBridge.reloadFromStore();
            return result;
        }

        NoticeStrategyConfig config = NoticeStrategyConverter.toConfig(def);
        Assert.notNull(config, "Policy invalid");
        // InsertBy Storage LayerGenerate ID
        config.setId(null);
        config.setCreateTime(now);
        config.setUpdateTime(now);
        String result = noticeStrategyProfile.add(config);
        noticeStrategyRuntimeBridge.reloadFromStore();
        return result;
    }

    @Override
    public synchronized String remove(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String id = StringUtil.trim(params.get("id"));
        Assert.hasText(id, "Policy ID cannot be empty");
        NoticeStrategyConfig existing = noticeStrategyProfile.get(id);
        Assert.notNull(existing, "Strategy does not exist");
        noticeStrategyProfile.remove(id);
        noticeStrategyRuntimeBridge.reloadFromStore();
        return id;
    }

    private NoticeStrategyDef parseOne(Map<String, String> params) {
        String name = StringUtil.trim(params.get("name"));
        Assert.hasText(name, "Policy name cannot be empty");
        int frequencyCode = parseFrequency(params.get("frequency"));
        NoticeFrequencyEnum frequency = NoticeFrequencyEnum.getByCode(frequencyCode);
        Assert.notNull(frequency, "Policy frequency invalid");
        String cron = StringUtil.trim(params.get("cron"));
        if (frequency == NoticeFrequencyEnum.INTERVAL) {
            Assert.hasText(cron, "Periodic policy to fill Cron");
        } else {
            cron = null;
        }
        int noticeTypes = parseNoticeType(params.get("noticeTypes"));
        Assert.notNull(NoticeTypeEnum.getByCode(noticeTypes), "Invalid notice type");

        NoticeStrategyDef def = new NoticeStrategyDef();
        def.setName(name);
        def.setFrequency(frequencyCode);
        def.setCron(cron);
        def.setEnabled(params.containsKey("enabled") ? BooleanUtil.toBoolean(params.get("enabled")) : true);
        def.setNoticeTypes(noticeTypes);
        def.setRemark(StringUtil.trim(params.get("remark")));
        return def;
    }

    private void assertNoticeTypeUnique(int noticeTypes, String excludeId) {
        List<NoticeStrategyConfig> all = noticeStrategyProfile.listAll();
        if (all == null) {
            return;
        }
        for (NoticeStrategyConfig config : all) {
            if (config == null || config.getNoticeTypes() != noticeTypes) {
                continue;
            }
            if (StringUtil.isNotBlank(excludeId) && StringUtil.equals(excludeId, config.getId())) {
                continue;
            }
            throw new BizException("Only one policy can be configured for the same notification type");
        }
    }

    private static int parseFrequency(String raw) {
        String value = StringUtil.trim(raw);
        Assert.hasText(value, "Policy frequency cannot be empty");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignore) {
            // Compatible list of names
        }
        try {
            return NoticeFrequencyEnum.valueOf(value).getCode();
        } catch (Exception e) {
            throw new BizException("Policy frequency invalid: " + value);
        }
    }

    private static int parseNoticeType(String raw) {
        String value = StringUtil.trim(raw);
        Assert.hasText(value, "Policy has to specify the type of notification");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignore) {
            // Compatible list of names
        }
        try {
            return NoticeTypeEnum.valueOf(value).getCode();
        } catch (Exception e) {
            throw new BizException("Invalid notice type: " + value);
        }
    }
}
