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
 * UniversalTasksStatus（TasksRun StateandSnapshots/conformity of progress:
 * 0-Not running；1-Running；2-Stopping；3-Completed。
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-10-18 19:52
 */
public enum CommonTaskStatusEnum {

    /**
     * Not running / Not implemented
     */
    READY(0, "Not running"),
    /**
     * Running
     */
    RUNNING(1, "Running"),
    /**
     * Stopping
     */
    STOPPING(2, "Stopping"),
    /**
     * Completed
     */
    DONE(3, "Completed");

    private final int code;
    private final String message;

    CommonTaskStatusEnum(int code, String message) {
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
     * RunningorStopping（Not back yetNot running/Completed）
     */
    public static boolean isRunning(int status) {
        return status == RUNNING.code || status == STOPPING.code;
    }

    public static boolean isStopping(int status) {
        return status == STOPPING.code;
    }

    public static boolean isDone(Integer status) {
        return status != null && status == DONE.code;
    }

    public static boolean isDone(int status) {
        return status == DONE.code;
    }

}
