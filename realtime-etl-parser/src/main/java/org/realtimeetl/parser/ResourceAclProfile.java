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

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.model.ResourceAcl;
import org.realtimeetl.sdk.filter.Query;

import java.util.List;

/**
 * Resource ACL Read and write.
 *
 * @author Realtime ETL
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
