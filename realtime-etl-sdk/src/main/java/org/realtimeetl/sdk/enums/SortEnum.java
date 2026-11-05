/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.sdk.enums;

/**
 * Supported Sorting
 *
 * @author AE86
 * @version 1.0.0
 * @date 2023/6/1 00:07
 */
public enum SortEnum {

    /**
     * Raise
     */
    ASC("asc"),
    /**
     * Dec Terminating
     */
    DESC("desc");

    SortEnum(String code) {
        this.code = code;
    }

    private final String code;

    public String getCode() {
        return code;
    }

    /**
     * YesNoDec Terminating
     *
     * @return
     */
    public boolean isDesc() {
        return this == DESC;
    }
}
