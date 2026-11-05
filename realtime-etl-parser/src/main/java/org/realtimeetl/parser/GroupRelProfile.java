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

import org.realtimeetl.parser.model.GroupRel;

import java.util.List;

/**
 * Business groupRelationship reading and writing (multiple: one)Resourcecan be hung in multiple groups.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-14 18:00
 */
public interface GroupRelProfile {

    /**
     * Press(Group, Resource type, Resource ID) Check one for belonging.
     */
    GroupRel getByGroupAndResource(String groupId, String resourceType, String resourceId);

    /**
     * Press(Resource type, Resource ID) ListAllAttribution.
     */
    List<GroupRel> listByResource(String resourceType, String resourceId);

    /**
     * 按分组列出关系；{@code resourceType} 为空则不限类型。
     */
    List<GroupRel> listByGroup(String groupId, String resourceType);

    /**
     * PressGroup + Resource typeListRESOURCE_ID。
     */
    List<String> listResourceIds(String groupId, String resourceType);

    /**
     * StatisticsGroupNumber of members.
     */
    int countByGroup(String groupId);

    /**
     * InsertRelationship.
     */
    String add(GroupRel rel);

    /**
     * Update relationship.
     */
    String update(GroupRel rel);

    /**
     * PressPrimary keyDelete。
     */
    void remove(String id);

    /**
     * DeleteSomeResourceAssignGroupUnder the relationship.
     */
    void removeByGroupAndResource(String groupId, String resourceType, String resourceId);

    /**
     * DeleteSomeResourceAllAttribution.
     */
    void removeByResource(String resourceType, String resourceId);

    /**
     * DeleteSomeGroupconcludeAllRelationship.
     */
    void removeByGroup(String groupId);
}
