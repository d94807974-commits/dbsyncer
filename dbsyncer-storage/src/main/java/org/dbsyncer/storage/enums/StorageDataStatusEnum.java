/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.storage.enums;

/**
 * Sync dataStatusEnumeration
 *
 * @author AE86
 * @version 1.0.0
 * @date 2021-03-10 20:31
 */
public enum StorageDataStatusEnum {

    /**
     * Failed
     */
    FAIL(0, "Failed"),
    /**
     * Success
     */
    SUCCESS(1, "Success");

    private final int value;

    private final String message;

    StorageDataStatusEnum(int value, String message) {
        this.value = value;
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
