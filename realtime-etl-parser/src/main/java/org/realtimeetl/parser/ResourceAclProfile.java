/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.ResourceAcl;
import org.realtimeetl.sdk.filter.Query;

import java.util.List;

/**
 * Resource ACL Read and write.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public interface ResourceAclProfile {

    ResourceAcl get(String id);

    List<ResourceAcl> listAll();

    /**
     * minPageQuestionResource authorization（ConditionBy{@link Query} Carry.
     */
    Paging query(Query query);

    List<ResourceAcl> listBySubject(int subjectType, String subjectId);

    ResourceAcl getByUnique(int subjectType, String subjectId, String resourceType, String resourceId);

    String add(ResourceAcl acl);

    String update(ResourceAcl acl);

    void remove(String id);

    /**
     * PressResource type + Resource ID BatchDelete（ResourceByDeleteEnd of story.
     */
    void removeByResource(String resourceType, String resourceId);

    /**
     * By SubjectType + SubjectID BatchDelete（User/roleByDeleteEnd of story.
     */
    void removeBySubject(int subjectType, String subjectId);
}
