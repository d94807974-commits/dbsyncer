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
package org.realtimeetl.connector.sqlserver.enums;

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
