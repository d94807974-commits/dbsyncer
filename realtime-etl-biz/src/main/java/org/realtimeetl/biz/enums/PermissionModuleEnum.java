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

import org.realtimeetl.common.util.StringUtil;

import java.util.Locale;

/**
 * Functional PermissionsModule（and {@code realtime_etl_permission.MODULE} The code is consistent).
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-23
 */
public enum PermissionModuleEnum {

    /**
     * User
     */
    USER("user", "User"),

    /**
     * Connector
     */
    CONNECTOR("connector", "Connector"),

    /**
     * Sync task
     */
    MAPPING("mapping", "Sync task"),

    /**
     * Alert notification
     */
    NOTICE("notice", "Alert notification"),

    /**
     * License
     */
    LICENSE("license", "License"),

    /**
     * System settings
     */
    SYSTEM("system", "System settings"),

    /**
     * Resource authorization
     */
    ACL("acl", "Resource");

    private final String code;
    private final String name;

    PermissionModuleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Coding (case insensitive);UnknownBack {@code null}。
     */
    public static PermissionModuleEnum get(String code) {
        if (StringUtil.isBlank(code)) {
            return null;
        }
        String key = code.trim().toLowerCase(Locale.ROOT);
        for (PermissionModuleEnum module : values()) {
            if (StringUtil.equals(module.code, key)) {
                return module;
            }
        }
        return null;
    }

    /**
     * ParsingModule(a) Presentation name;UnknownEncoding OriginalBack，Empty「Other」。
     */
    public static String resolveName(String code) {
        if (StringUtil.isBlank(code)) {
            return "Other";
        }
        PermissionModuleEnum module = get(code);
        return module != null ? module.name : code;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
