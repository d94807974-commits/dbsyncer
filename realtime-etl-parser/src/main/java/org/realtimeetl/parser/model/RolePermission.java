/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.model;

import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.constant.ConfigConstant;

/**
 * Role-PermissionsRelationship.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public class RolePermission extends ConfigModel {

    private String roleId;
    private String permissionId;

    public RolePermission() {
        super.setName("");
        super.setType(ConfigConstant.ROLE_PERMISSION);
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(String permissionId) {
        this.permissionId = permissionId;
    }
}
