/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.enums;

import org.realtimeetl.common.util.StringUtil;

import java.util.Locale;

/**
 * Resource ACL Actions (call Compensationer Usage; Securs Internal Map toPermissions(Pilot).
 *
 * @author wuji
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
