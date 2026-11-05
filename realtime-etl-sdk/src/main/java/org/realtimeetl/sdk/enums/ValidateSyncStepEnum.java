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
 * Data validationTable snapshot phase (written){@code CommonTaskSnapshot.step}）。
 *
 * @author Realtime ETL
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
