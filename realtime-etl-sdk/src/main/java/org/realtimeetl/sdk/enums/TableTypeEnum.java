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

import org.realtimeetl.sdk.SdkException;

/**
 * TableType
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2021/08/26 21:13
 */
public enum TableTypeEnum {

    /**
     * Table
     */
    TABLE("TABLE"),

    /**
     * View
     */
    VIEW("VIEW"),

    /**
     * Image View
     */
    MATERIALIZED_VIEW("MATERIALIZED VIEW"),

    /**
     * SQL(ExtensionType)
     */
    SQL("SQL"),

    /**
     * ‌Semi-structured(ExtensionType，LikeFile，Kafka，ES)
     */
    SEMI("SEMI");

    private final String code;

    TableTypeEnum(String code) {
        this.code = code;
    }

    /**
     * YesNoTableType
     *
     * @param type
     * @return
     */
    public static boolean isTable(String type) {
        return TABLE.getCode().equals(type);
    }

    public static TableTypeEnum getTableType(String type) throws SdkException {
        for (TableTypeEnum e : TableTypeEnum.values()) {
            if (e.getCode().equals(type)) {
                return e;
            }
        }
        throw new SdkException(String.format("TableTypeEnum type \"%s\" does not exist.", type));
    }

    public String getCode() {
        return code;
    }
}
