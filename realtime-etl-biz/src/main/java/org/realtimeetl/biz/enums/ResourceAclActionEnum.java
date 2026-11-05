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
 * Resource ACL Actions (call Compensationer Usage; Securs Internal Map toPermissions(Pilot).
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public enum ResourceAclActionEnum {

    /**
     * View
     */
    READ("read", "View"),

    /**
     * Edit
     */
    WRITE("write", "Edit"),

    /**
     * onActions
     */
    OPERATE("operate", "Actions");

    private final String code;
    private final String name;

    ResourceAclActionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Coding (case insensitive);UnknownBack {@code null}。
     */
    public static ResourceAclActionEnum get(String code) {
        if (StringUtil.isBlank(code)) {
            return null;
        }
        String key = code.trim().toLowerCase(Locale.ROOT);
        for (ResourceAclActionEnum action : values()) {
            if (StringUtil.equals(action.code, key)) {
                return action;
            }
        }
        return null;
    }

    /**
     * YesNoSupported action encoding.
     */
    public static boolean isSupported(String code) {
        return get(code) != null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
