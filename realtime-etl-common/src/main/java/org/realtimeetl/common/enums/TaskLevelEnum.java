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
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-24 10:35
 */
public enum TaskLevelEnum {
    /**
     * 0 Task level（Top LevelTasks）
     */
    TASK(0, "Task level"),

    /**
     * 1 Task detail level
     */
    TASK_DETAIL(1, "Task detail level");

    private final int code;
    private final String desc;

    TaskLevelEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
