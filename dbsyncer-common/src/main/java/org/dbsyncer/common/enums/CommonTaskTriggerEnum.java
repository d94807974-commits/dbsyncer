/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.enums;

import org.dbsyncer.common.util.StringUtil;

/**
 * TasksSBI Trigger Policy Count
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2025-10-18 19:52
 */
public enum CommonTaskTriggerEnum {

    /**
     * Run once
     */
    ONCE("once", "Run once"),

    /**
     * Run on schedule
     */
    TIMING("timing", "Run on schedule");

    private final String code;
    private final String message;

    CommonTaskTriggerEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * AccessType
     */
    public static CommonTaskTriggerEnum getType(String code) {
        for (CommonTaskTriggerEnum e : CommonTaskTriggerEnum.values()) {
            if (StringUtil.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }

    public static boolean isOnce(String code) {
        return ONCE.code.equals(code);
    }

    public static boolean isTiming(String code) {
        return TIMING.code.equals(code);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
