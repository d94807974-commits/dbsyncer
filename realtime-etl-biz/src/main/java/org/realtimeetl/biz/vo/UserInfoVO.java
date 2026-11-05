package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2022/11/17 0:14
 */
public class UserInfoVO extends UserInfo {

    /**
     * LordRole ID（realtime_etl_user_role）
     */
    private String roleId;

    /**
     * Role Name
     */
    private String roleName;

    /**
     * YesNoSuper Physicalusername=admin）
     */
    private boolean admin;

    /**
     * Functional PermissionsCodeList（Likemapping:operate）
     */
    private List<String> permissions = new ArrayList<>();

    /**
     * YesNoBecauseLogin failedTemporary locked (in process)Status，Oh, no.Enduring）
     */
    private boolean locked;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
