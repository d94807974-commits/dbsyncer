/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser;

import org.dbsyncer.parser.model.GroupRel;

import java.util.List;

/**
 * Business groupRelationship reading and writing (multiple: one)Resourcecan be hung in multiple groups.
 *
 * @author wuji
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
     * PressGroupListing relationships;{@code resourceType} Other OrganiserType。
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
