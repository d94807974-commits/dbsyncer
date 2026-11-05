/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.sdk.enums;

import org.realtimeetl.sdk.SdkException;

/**
 * TableType
 *
 * @author AE86
 * @version 1.0.0
 * @date 2021/08/26 21:13
 */
public enum TableTypeEnum {

    /**
     * Table
     */
    TABLE("TABLE"),

    /**
     * View
     */
    VIEW("VIEW"),

    /**
     * Image View
     */
    MATERIALIZED_VIEW("MATERIALIZED VIEW"),

    /**
     * SQL(ExtensionType)
     */
    SQL("SQL"),

    /**
     * ‌Semi-structured(ExtensionType，LikeFile，Kafka，ES)
     */
    SEMI("SEMI");

    private final String code;

    TableTypeEnum(String code) {
        this.code = code;
    }

    /**
     * YesNoTableType
     *
     * @param type
     * @return
     */
    public static boolean isTable(String type) {
        return TABLE.getCode().equals(type);
    }

    public static TableTypeEnum getTableType(String type) throws SdkException {
        for (TableTypeEnum e : TableTypeEnum.values()) {
            if (e.getCode().equals(type)) {
                return e;
            }
        }
        throw new SdkException(String.format("TableTypeEnum type \"%s\" does not exist.", type));
    }

    public String getCode() {
        return code;
    }
}
