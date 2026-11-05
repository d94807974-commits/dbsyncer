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

import java.util.Set;

/**
 * Functional PermissionsQuery (first part)RBAC）。
 *
 * @author Realtime ETL
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
