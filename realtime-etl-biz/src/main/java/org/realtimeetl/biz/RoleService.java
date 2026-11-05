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

import org.realtimeetl.biz.vo.PermissionVO;
import org.realtimeetl.biz.vo.RoleVO;

import java.util.List;
import java.util.Map;

/**
 * Role management（List、Self-building, kidnapping,Delete）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-21
 */
public interface RoleService {

    /**
     * RoleList（HamPermissionsThe volume.
     */
    List<RoleVO> listRoles();

    /**
     * RoleDetails（It's tied.PermissionsThe code.
     */
    RoleVO getRole(String roleId);

    /**
     * AllPermissionsPointRoleTaggranted）。
     *
     * @param roleId empty; non-empty time markRoleYesNoAlreadyAuthorization
     */
    List<PermissionVO> listPermissions(String roleId);

    /**
     * Add RoleAnd bind.Permissionscode.
     *
     * @param params name、permissionCodes（Commaminamend
     */
    String add(Map<String, String> params);

    /**
     * UpdateRole Name，andOverwriteTiePermissionscode.
     *
     * @param params roleId、name、permissionCodes（Commaminamend
     */
    String edit(Map<String, String> params);

    /**
     * Remove Role（Still.UserTieScheduledI can't delete it.
     *
     * @param params roleId
     */
    String remove(Map<String, String> params);
}
