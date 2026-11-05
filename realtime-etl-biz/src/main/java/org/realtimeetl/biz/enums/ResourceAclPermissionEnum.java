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
package org.realtimeetl.biz.enums;

/**
 * Resource ACL PermissionsTranche:0-Read-only / 1-Read/Write（I'm sorry.
 *
 * @author Realtime ETL
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
