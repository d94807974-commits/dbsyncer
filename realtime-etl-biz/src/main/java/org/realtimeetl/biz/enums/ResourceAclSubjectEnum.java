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
 * Resource ACL AuthorizationSubjectType：0-USER / 1-ROLE。
 *
 * @author Realtime ETL
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
