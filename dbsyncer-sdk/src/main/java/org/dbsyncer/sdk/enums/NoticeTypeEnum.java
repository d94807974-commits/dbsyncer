/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

/**
 * AnnouncementsTypeEnumeration
 *
 * @author AE86
 * @version 1.0.0
 * @date 2026-03-05 00:27
 */
public enum NoticeTypeEnum {
    /**
     * CCPR Test Message
     */
    TEST_MESSAGE(0),
    /**
     * Connect Offline
     */
    CONNECTOR_OFFLINE(1),
    /**
     * Sync taskExistenceFailedRecords
     */
    MAPPING_ERROR(2),
    /**
     * Stop Sync Task Manually
     */
    MAPPING_STOP(3),
    /**
     * Revision verification failed
     */
    VALIDATE_SYNC_FAIL(4),
    /**
     * Authorization irregular.
     */
    LICENSE_EXCEPTION(5),
    /**
     * Organisation
     */
    LICENSE_EXPIRE_REMIND(6),
    /**
     * Mandate expired
     */
    LICENSE_EXPIRED(7),
    /**
     * Full Sync
     */
    MAPPING_FULL_COMPLETE(8);

    private final int code;

    NoticeTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Parsing by code;UnknownBack {@code null}。
     */
    public static NoticeTypeEnum getByCode(int code) {
        for (NoticeTypeEnum type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}
