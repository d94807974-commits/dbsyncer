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

import com.alibaba.fastjson2.annotation.JSONCreator;
import com.alibaba.fastjson2.annotation.JSONField;

/**
 * Data validationDetailsType（{@code realtime_etl_task_validate_sync_detail.type}）。
 *
 * @author Realtime ETL
 */
public enum ValidateSyncDetailTypeEnum {

    /**
     * Row dataVerify INTRODUCTION
     */
    ROW_DATA("rowData"),
    /**
     * TableStructureVerify INTRODUCTION
     */
    TABLE_SCHEMA("tableSchema");

    private final String value;

    ValidateSyncDetailTypeEnum(String value) {
        this.value = value;
    }

    @JSONField
    public String getValue() {
        return value;
    }

    @JSONCreator
    public static ValidateSyncDetailTypeEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (ValidateSyncDetailTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
