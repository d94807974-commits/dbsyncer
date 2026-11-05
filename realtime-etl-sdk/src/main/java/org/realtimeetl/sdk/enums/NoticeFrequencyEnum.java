/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.sdk.enums;

/**
 * Police strategy CorruptSend frequency:0-Immediately /1-cycle.
 *
 * @author Realtime ETL
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
