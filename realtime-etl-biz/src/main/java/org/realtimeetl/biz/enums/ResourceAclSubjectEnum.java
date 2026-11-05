/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.enums;

/**
 * Resource ACL AuthorizationSubjectType：0-USER / 1-ROLE。
 *
 * @author wuji
 * @version 1.0.0
 */
public enum ResourceAclSubjectEnum {

    /**
     * User
     */
    USER(0, "User"),

    /**
     * Role
     */
    ROLE(1, "Role");

    private final int code;
    private final String name;

    ResourceAclSubjectEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Parsing by code;UnknownBack {@code null}。
     */
    public static ResourceAclSubjectEnum get(int code) {
        for (ResourceAclSubjectEnum subject : values()) {
            if (subject.code == code) {
                return subject;
            }
        }
        return null;
    }

    /**
     * YesNoSupported SubjectsTypecode.
     */
    public static boolean isSupported(int code) {
        return get(code) != null;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isRole() {
        return this == ROLE;
    }

    public boolean isUser() {
        return this == USER;
    }
}
