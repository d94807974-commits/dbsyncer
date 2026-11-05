/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.parser.model.NoticeRule;

import java.util.List;

/**
 * Alert notificationRule read and write.
 *
 * @author wuji
 * @version 1.0.0
 */
public interface NoticeRuleProfile {

    /**
     * AllRules.
     */
    List<NoticeRule> listAll();

    /**
     * PressPrimary keyquery.
     */
    NoticeRule get(String id);

    /**
     * Press(ScopeType, ScopeID) Check one.
     */
    NoticeRule getByScope(String scopeType, String scopeId);

    /**
     * PressScopeTypeList.
     */
    List<NoticeRule> listByScopeType(String scopeType);

    /**
     * Insert。
     */
    String add(NoticeRule rule);

    /**
     * Update.
     */
    String update(NoticeRule rule);

    /**
     * PressPrimary keyDelete。
     */
    void remove(String id);

    /**
     * PressScopeBatchDelete（Group/TasksByDeleteEnd of story.
     */
    void removeByScopeId(String scopeType, String scopeId);
}
