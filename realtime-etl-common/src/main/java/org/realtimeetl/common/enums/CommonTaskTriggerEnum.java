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

import org.realtimeetl.common.util.StringUtil;

/**
 * TasksSBI Trigger Policy Count
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-10-18 19:52
 */
public enum CommonTaskTriggerEnum {

    /**
     * Run once
     */
    ONCE("once", "Run once"),

    /**
     * Run on schedule
     */
    TIMING("timing", "Run on schedule");

    private final String code;
    private final String message;

    CommonTaskTriggerEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * AccessType
     */
    public static CommonTaskTriggerEnum getType(String code) {
        for (CommonTaskTriggerEnum e : CommonTaskTriggerEnum.values()) {
            if (StringUtil.equals(code, e.getCode())) {
                return e;
            }
        }
        return null;
    }

    public static boolean isOnce(String code) {
        return ONCE.code.equals(code);
    }

    public static boolean isTiming(String code) {
        return TIMING.code.equals(code);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
