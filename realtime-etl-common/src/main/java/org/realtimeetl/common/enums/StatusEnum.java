/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.enums;

/**
 * UniversalStatus（Integer DownLibrary：0-Failed；1-Success）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-27
 */
public enum StatusEnum {

    /**
     * Failed
     */
    FAIL(0, "Failed"),

    /**
     * Success
     */
    SUCCESS(1, "Success");

    private final int code;
    private final String message;

    StatusEnum(int code, String message) {
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
     * Boolean→ Status。
     *
     * @param success true YesSuccess
     * @return Success/Failed
     */
    public static StatusEnum of(boolean success) {
        return success ? SUCCESS : FAIL;
    }

    /**
     * Presscode Analysis;UnknownValueBack null。
     *
     * @param code StatusCode
     * @return Enumer Sexualitynull
     */
    public static StatusEnum get(Integer code) {
        if (code == null) {
            return null;
        }
        for (StatusEnum value : values()) {
            if (value.code == code) {
                return value;
            }
        }
        return null;
    }

    public static boolean isSuccess(Integer code) {
        return code != null && code == SUCCESS.code;
    }

    public static boolean isFail(Integer code) {
        return code != null && code == FAIL.code;
    }

}
