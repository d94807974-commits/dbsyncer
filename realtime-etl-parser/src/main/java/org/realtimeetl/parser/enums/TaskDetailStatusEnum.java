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
package org.realtimeetl.parser.enums;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;

/**
 * TasksDetailsListStatusFiltersuccess / fail）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-31 18:20
 */
public enum TaskDetailStatusEnum {

    /**
     * Success（Corresponding indicator column= 0）
     */
    SUCCESS(ConfigConstant.META_SUCCESS),

    /**
     * Failed（Corresponding indicator column&gt; 0）
     */
    FAIL(ConfigConstant.META_FAIL);

    private final String code;

    TaskDetailStatusEnum(String code) {
        this.code = code;
    }

    /**
     * ParsingRequest parameters；Space orNoneTraditionally.Back {@code null}（Means no pressStatusAgriculture
     *
     * @param value success / fail（Ignore case)
     * @return Enumer Sexualitynull
     */
    public static TaskDetailStatusEnum from(String value) {
        if (StringUtil.isBlank(value)) {
            return null;
        }
        for (TaskDetailStatusEnum status : values()) {
            if (StringUtil.equalsIgnoreCase(status.code, value)) {
                return status;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
}
