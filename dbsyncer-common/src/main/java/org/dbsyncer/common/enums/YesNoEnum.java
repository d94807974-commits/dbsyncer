/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.enums;

/**
 * Yes/No（Integer DownLibrary：1-Yes；0-No）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-09-29
 */
public enum YesNoEnum {

    /**
     * No
     */
    NO(0, "No"),

    /**
     * Yes
     */
    YES(1, "Yes");

    private final int code;
    private final String message;

    YesNoEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Boolean→ Enumerate.
     *
     * @param value BooleanValue
     * @return Yes/No
     */
    public static YesNoEnum of(boolean value) {
        return value ? YES : NO;
    }

}
