/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * RolePresentation (included)PermissionsThe code.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
public class RoleVO extends Role {

    /**
     * boundPermissionsVolume
     */
    private int permissionCount;

    /**
     * boundPermissionsCode
     */
    private List<String> permissionCodes = new ArrayList<>();

    public int getPermissionCount() {
        return permissionCount;
    }

    public void setPermissionCount(int permissionCount) {
        this.permissionCount = permissionCount;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }
}
