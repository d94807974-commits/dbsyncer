/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz;

import java.util.Set;

/**
 * Functional PermissionsQuery (first part)RBAC）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public interface PermissionService {

    /**
     * Write on Superbar (20%)Authority the mark.
     */
    String AUTHORITY_ROLE_ADMIN = "role_admin";

    /**
     * YesNoSuperbarrel (only)username As DefaultAccount admin）。
     */
    boolean isAdmin(String username);

    /**
     * YesNoPossession of specifiedPermissionsCode{@code mapping:operate}）。
     */
    boolean hasPermission(String username, String permissionCode);

    /**
     * UserAllPermissionsCode (More)RoleAnd that's it.
     */
    Set<String> listPermissionCodes(String username);

    /**
     * Login/SSO With:PermissionsCode+ Optional{@link #AUTHORITY_ROLE_ADMIN}，Comma spell.
     */
    String buildAuthorityString(String username);

    /**
     * YesUserTieRole（PressRole ID，I'm sorry.
     */
    void bindUserRole(String userId, String roleId);

    /**
     * OverwriteUserRoleTie (first)ClearOne more.
     */
    void replaceUserRole(String userId, String roleId);

    /**
     * DeleteUserAllRoleTie.
     */
    void unbindAllUserRoles(String userId);

    /**
     * UserLordRole ID（Upper 1user_role）。
     */
    String resolvePrimaryRoleId(String userId);

    /**
     * UserLordRole Name。
     */
    String resolvePrimaryRoleName(String userId);
}
