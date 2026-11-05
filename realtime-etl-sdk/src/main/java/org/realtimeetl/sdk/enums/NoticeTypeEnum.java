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
 * AnnouncementsTypeEnumeration
 *
 * @author Realtime ETL
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
