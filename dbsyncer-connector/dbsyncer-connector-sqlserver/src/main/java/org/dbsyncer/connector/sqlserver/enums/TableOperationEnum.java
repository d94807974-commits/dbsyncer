/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.connector.sqlserver.enums;

public enum TableOperationEnum {

    /**
     * Insert
     */
    INSERT(2),
    /**
     * Update（AppealValue）
     */
    UPDATE_BEFORE(3),
    /**
     * Update（NewValue）
     */
    UPDATE_AFTER(4),
    /**
     * Delete
     */
    DELETE(1);

    private final int code;

    TableOperationEnum(int code) {
        this.code = code;
    }

    public static boolean isInsert(int code) {
        return INSERT.getCode() == code;
    }

    public static boolean isUpdateBefore(int code) {
        return UPDATE_BEFORE.getCode() == code;
    }

    public static boolean isUpdateAfter(int code) {
        return UPDATE_AFTER.getCode() == code;
    }

    public static boolean isDelete(int code) {
        return DELETE.getCode() == code;
    }

    public int getCode() {
        return code;
    }
}
