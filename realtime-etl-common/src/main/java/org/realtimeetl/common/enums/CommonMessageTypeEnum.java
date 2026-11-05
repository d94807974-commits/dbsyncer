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
package org.realtimeetl.common.enums;

/**
 * Message business field.
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-09-23 00:27
 */
public enum CommonMessageTypeEnum {

    /**
     * DeleteConfigureCache
     */
    REMOVE_CONFIG_MODEL_CACHE(0);

    private final int code;

    CommonMessageTypeEnum(int code) {
        this.code = code;
    }

    /**
     * Presscode Analysis;UnknownThrow!Error（Co-authorJSON Inverse sequence entry.
     *
     * @param code Fieldcode
     * @return Enumeration
     */
    public static CommonMessageTypeEnum getByCode(int code) {
        for (CommonMessageTypeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("invalid code: " + code);
    }

    public int getCode() {
        return code;
    }
}
