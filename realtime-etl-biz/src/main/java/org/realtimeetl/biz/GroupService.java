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
package org.realtimeetl.biz;

import org.realtimeetl.parser.model.Group;
import org.realtimeetl.parser.model.GroupRel;

import java.util.List;
import java.util.Map;

/**
 * Business groupServicesResourceandGroupMany pairs more.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-14 18:00
 */
public interface GroupService {

    /**
     * New group（OnlyName）。
     *
     * @param params Hamname
     * @return Group ID
     */
    String add(Map<String, String> params);

    /**
     * Edit Group（Change of name.
     *
     * @param params Hamid、name
     * @return Group ID
     */
    String edit(Map<String, String> params);

    /**
     * Delete groupConvergencerel。
     *
     * @param id Group ID
     * @return Group ID
     */
    String remove(String id);

    /**
     * AllGroupList。
     */
    List<Group> list();

    /**
     * Pressid Cha.Group。
     */
    Group get(String id);

    /**
     * GroupNext members (number deleted)ConfirmIt's in the text.
     */
    int countMembers(String id);

    /**
     * Hang to SpecifyGroup（)。, etc.; already exists and ignored).{@code groupId} EmptyClearTheResourceAllAttribution.
     *
     * @param groupId      Objective CombinedGroup ID，Empty or empty=ClearAllAttribution
     * @param resourceType connector / mapping / validateSync / databaseSync
     * @param resourceId   Resource ID
     */
    void bind(String groupId, String resourceType, String resourceId);

    /**
     * Batch-mounted groups.{@code groupId} Empty as batchClearThese.ResourceYes.AllAttribution.
     */
    void bindBatch(String groupId, String resourceType, List<String> resourceIds);

    /**
     * From SpecifyGroupOut (without prejudice to Each other group).
     */
    void unbind(String groupId, String resourceType, String resourceId);

    /**
     * Batch from SpecifiedGroupMove out.
     */
    void unbindBatch(String groupId, String resourceType, List<String> resourceIds);

    /**
     * QuestionResourceCurrentAllAttribution.
     */
    List<GroupRel> listByResource(String resourceType, String resourceId);

    /**
     * PressGroup+TypeRemoveResource ID List。
     */
    List<String> listResourceIds(String groupId, String resourceType);

    /**
     * ParsingListPage groupId Filter entry.
     *
     * @param groupIdParam Default/empty/null=All；Other=RealGroup ID
     * @param resourceType Resource type
     */
    GroupSearchFilter resolveSearchFilter(String groupIdParam, String resourceType);
}
