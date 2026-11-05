/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

/**
 * Police strategy CorruptSend frequency:0-Immediately /1-cycle.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-06-16 12:00
 */
public enum NoticeFrequencyEnum {

    /**
     * Send Now
     */
    IMMEDIATE(0),

    /**
     * Periodically sent (byCron Organisationflush）
     */
    INTERVAL(1);

    private final int code;

    NoticeFrequencyEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Parsing by code;UnknownBack {@code null}。
     */
    public static NoticeFrequencyEnum getByCode(int code) {
        for (NoticeFrequencyEnum frequency : values()) {
            if (frequency.code == code) {
                return frequency;
            }
        }
        return null;
    }
}
