/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.impl;

import org.dbsyncer.biz.NoticeRuleService;
import org.dbsyncer.common.util.BooleanUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.parser.GroupProfile;
import org.dbsyncer.parser.MappingProfile;
import org.dbsyncer.parser.NoticeRuleProfile;
import org.dbsyncer.parser.model.Group;
import org.dbsyncer.parser.model.Mapping;
import org.dbsyncer.parser.model.NoticeRule;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * {@link NoticeRuleService} Achieved.
 *
 * @author wuji
 * @version 1.0.0
 */
@Service
public class NoticeRuleServiceImpl implements NoticeRuleService {

    @Resource
    private NoticeRuleProfile noticeRuleProfile;

    @Resource
    private GroupProfile groupProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Override
    public List<NoticeRule> listAll() {
        return noticeRuleProfile.listAll();
    }

    @Override
    public synchronized String save(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String scopeType = StringUtil.trim(params.get("scopeType"));
        String scopeId = StringUtil.trim(params.get("scopeId"));
        Assert.hasText(scopeType, "Field type cannot be empty");
        Assert.hasText(scopeId, "Select Tasks to Group or Sync");
        Assert.isTrue(StringUtil.equals(ConfigConstant.GROUP, scopeType)
                        || StringUtil.equals(ConfigConstant.MAPPING, scopeType),
                "The field only supports grouping or synchronization tasks");
        assertScopeExists(scopeType, scopeId);

        boolean atAll = BooleanUtil.toBoolean(params.get("atAll"));
        String atPhones = StringUtil.trim(params.get("atPhones"));
        String emails = StringUtil.trim(params.get("emails"));
        if (!atAll) {
            Assert.isTrue(StringUtil.isNotBlank(atPhones) || StringUtil.isNotBlank(emails),
                    "Unopened@owner by filling in cell number or email");
        }

        String id = StringUtil.trim(params.get("id"));
        NoticeRule conflict = noticeRuleProfile.getByScope(scopeType, scopeId);
        if (StringUtil.isNotBlank(id)) {
            NoticeRule existing = noticeRuleProfile.get(id);
            Assert.notNull(existing, "Notification rule does not exist");
            Assert.isTrue(conflict == null || StringUtil.equals(conflict.getId(), id),
                    "Only one notification rule for the same subgroup/task");
            existing.setScopeType(scopeType);
            existing.setScopeId(scopeId);
            existing.setAtAll(atAll ? 1 : 0);
            existing.setAtPhones(atPhones);
            existing.setEmails(emails);
            existing.setUpdateTime(System.currentTimeMillis());
            return noticeRuleProfile.update(existing);
        }

        Assert.isTrue(conflict == null, "Only one notification rule for the same subgroup/task");
        long now = System.currentTimeMillis();
        NoticeRule rule = new NoticeRule();
        rule.setScopeType(scopeType);
        rule.setScopeId(scopeId);
        rule.setAtAll(atAll ? 1 : 0);
        rule.setAtPhones(atPhones);
        rule.setEmails(emails);
        rule.setCreateTime(now);
        rule.setUpdateTime(now);
        return noticeRuleProfile.add(rule);
    }

    @Override
    public synchronized String remove(Map<String, String> params) {
        Assert.notNull(params, "Parameter cannot be empty");
        String id = StringUtil.trim(params.get("id"));
        Assert.hasText(id, "Rule ID cannot be empty.");
        NoticeRule existing = noticeRuleProfile.get(id);
        Assert.notNull(existing, "Notification rule does not exist");
        noticeRuleProfile.remove(id);
        return id;
    }

    private void assertScopeExists(String scopeType, String scopeId) {
        if (StringUtil.equals(ConfigConstant.GROUP, scopeType)) {
            Group group = groupProfile.getGroup(scopeId);
            Assert.notNull(group, "Group does not exist");
            return;
        }
        Mapping mapping = mappingProfile.get(scopeId);
        Assert.notNull(mapping, "Sync task does not exist");
        Assert.isTrue(StringUtil.equals(ConfigConstant.MAPPING, mapping.getType()), "Sync task does not exist");
    }
}
