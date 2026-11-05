/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz;

import org.realtimeetl.biz.vo.ResourceAclVO;
import org.realtimeetl.common.model.Paging;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Resource ACL（Tut.GroupThe Lord.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public interface ResourceAclService {

    List<ResourceAclVO> listAll();

    /**
     * minPageQuestionResource authorization。
     * <p>OptionalParameter：{@code subjectType}（0 User / 1 Role）、{@code subjectId}、{@code pageNum}、{@code pageSize}。
     */
    Paging<ResourceAclVO> query(Map<String, String> params);

    ResourceAclVO get(String id);

    /**
     * InsertorOverwrite（Same subject+ResourceOnly keys are updated.
     */
    String save(Map<String, String> params);

    String remove(Map<String, String> params);

    /**
     * GrantUserFunctionResourceOwner.
     * admin orNoneEffectUserSkip; existingOverwriteYesRead/Write。
     */
    void grantOwner(String username, String resourceType, String resourceId);

    /**
     * ResourceByDeleteEnd of period:ClearPoint to that.ResourceYes.All ACL。
     */
    void revokeAllForResource(String resourceType, String resourceId);

    /**
     * User/roleByDeleteEnd of period:ClearThe subject'sAll ACL。
     *
     * @param subjectType SubjectTypeCode, see.{@link org.realtimeetl.biz.enums.ResourceAclSubjectEnum}
     */
    void revokeAllForSubject(int subjectType, String subjectId);

    /**
     * UserYeah.ResourceYesNoAction availableadmin Skip.
     */
    boolean check(String username, String resourceType, String resourceId, String action);

    /**
     * I'm sure I do.ResourceMove,NoAnd throw the wrong.
     */
    void require(String username, String resourceType, String resourceId, String action);

    /**
     * ParsingUserFor certain categoriesResourceAccessibleID Gather.
     */
    Set<String> resolveAllowedIds(String username, String resourceType, String action);

    /**
     * WillACL ConstraintGroupFilterResult（ListQuery.
     */
    GroupSearchFilter applyToSearchFilter(GroupSearchFilter groupFilter, Set<String> allowedIds);

    /**
     * 过滤列表中无对应动作权限的项（admin 原样返回）。
     */
    <T> List<T> filter(String username, String resourceType, List<T> list, Function<T, String> idGetter, String action);
}
