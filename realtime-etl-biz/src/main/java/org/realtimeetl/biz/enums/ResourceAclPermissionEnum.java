/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.enums;

/**
 * Resource ACL PermissionsTranche:0-Read-only / 1-Read/Write（I'm sorry.
 *
 * @author wuji
 * @version 1.0.0
 */
public enum ResourceAclPermissionEnum {

    /**
     * Read-only
     */
    READ(0, "Read-only"),

    /**
     * Read/Write（I'm sorry.
     */
    READ_WRITE(1, "Read/Write");

    private final int code;
    private final String name;

    ResourceAclPermissionEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Parsing by code;UnknownBack {@code null}。
     */
    public static ResourceAclPermissionEnum get(int code) {
        for (ResourceAclPermissionEnum permission : values()) {
            if (permission.code == code) {
                return permission;
            }
        }
        return null;
    }

    /**
     * YesNoSupportedPermissionscode.
     */
    public static boolean isSupported(int code) {
        return get(code) != null;
    }

    /**
     * AlreadyAuthorizationSlotYesNoAddress action requirements:read (b) Any form required;write/operate YesRead/Write。
     */
    public static boolean satisfies(int grantedPermission, String actionCode) {
        ResourceAclActionEnum action = ResourceAclActionEnum.get(actionCode);
        if (action == null) {
            return false;
        }
        if (action == ResourceAclActionEnum.READ) {
            return grantedPermission == READ.code || grantedPermission == READ_WRITE.code;
        }
        return grantedPermission == READ_WRITE.code;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
