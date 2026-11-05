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
 * Supported Sorting
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2023/6/1 00:07
 */
public enum SortEnum {

    /**
     * Raise
     */
    ASC("asc"),
    /**
     * Dec Terminating
     */
    DESC("desc");

    SortEnum(String code) {
        this.code = code;
    }

    private final String code;

    public String getCode() {
        return code;
    }

    /**
     * YesNoDec Terminating
     *
     * @return
     */
    public boolean isDesc() {
        return this == DESC;
    }
}
