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

import org.realtimeetl.parser.model.NoticeRule;

import java.util.List;

/**
 * Alert notificationRule read and write.
 *
 * @author Realtime ETL
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
