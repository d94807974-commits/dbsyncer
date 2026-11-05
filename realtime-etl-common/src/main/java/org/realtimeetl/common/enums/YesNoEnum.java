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
 * Yes/No（Integer DownLibrary：1-Yes；0-No）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-09-29
 */
public enum YesNoEnum {

    /**
     * No
     */
    NO(0, "No"),

    /**
     * Yes
     */
    YES(1, "Yes");

    private final int code;
    private final String message;

    YesNoEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Boolean→ Enumerate.
     *
     * @param value BooleanValue
     * @return Yes/No
     */
    public static YesNoEnum of(boolean value) {
        return value ? YES : NO;
    }

}
