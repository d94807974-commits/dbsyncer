/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

/**
 * ChangeEventTypeEnumeration
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2023-12-09 20:34
 */
public enum ChangedEventTypeEnum {

    /**
     * ddlChange
     */
    DDL,
    /**
     * ScheduledChange
     */
    SCAN,
    /**
     * Line changes
     */
    ROW;

    public static boolean isDDL(ChangedEventTypeEnum event) {
        return DDL == event;
    }
}
