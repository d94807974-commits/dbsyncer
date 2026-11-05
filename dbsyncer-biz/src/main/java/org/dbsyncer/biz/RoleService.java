/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.biz.vo.PermissionVO;
import org.dbsyncer.biz.vo.RoleVO;

import java.util.List;
import java.util.Map;

/**
 * Role management（List、Self-building, kidnapping,Delete）。
 *
 * @author wuji
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
