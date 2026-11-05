/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.parser.model.Group;
import org.dbsyncer.parser.model.GroupRel;

import java.util.List;
import java.util.Map;

/**
 * Business groupServicesResourceandGroupMany pairs more.
 *
 * @author wuji
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
