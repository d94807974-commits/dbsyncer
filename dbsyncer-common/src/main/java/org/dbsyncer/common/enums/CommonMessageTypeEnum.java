/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.enums;

/**
 * Message business field.
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-09-23 00:27
 */
public enum CommonMessageTypeEnum {

    /**
     * DeleteConfigureCache
     */
    REMOVE_CONFIG_MODEL_CACHE(0);

    private final int code;

    CommonMessageTypeEnum(int code) {
        this.code = code;
    }

    /**
     * Presscode Analysis;UnknownThrow!Error（Co-authorJSON Inverse sequence entry.
     *
     * @param code Fieldcode
     * @return Enumeration
     */
    public static CommonMessageTypeEnum getByCode(int code) {
        for (CommonMessageTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("invalid code: " + code);
    }

    public int getCode() {
        return code;
    }
}
