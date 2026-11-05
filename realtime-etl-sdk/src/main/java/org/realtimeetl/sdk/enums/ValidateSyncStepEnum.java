/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.enums;

/**
 * Data validationTable snapshot phase (written){@code CommonTaskSnapshot.step}）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-08 20:09
 */
public enum ValidateSyncStepEnum {
    TABLE_SCHEMA("tableSchema", "Table schema"),
    TABLE_SCHEMA_SYNC("tableSchemaSync", "Schema fix"),
    ROW_DATA("rowData", "Row data"),
    ROW_DATA_SYNC("rowDataSync", "Row data fix"),
    ROW_DATA_REVERSE_SYNC("rowDataReverseSync", "Reverse row data fix");

    private final String code;
    private final String message;

    ValidateSyncStepEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ValidateSyncStepEnum ofCode(String code) {
        if (code == null) {
            return null;
        }
        for (ValidateSyncStepEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
