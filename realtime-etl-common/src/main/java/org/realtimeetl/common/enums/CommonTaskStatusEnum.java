/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.enums;

/**
 * UniversalTasksStatus（TasksRun StateandSnapshots/conformity of progress:
 * 0-Not running；1-Running；2-Stopping；3-Completed。
 *
 * @author The clouds.
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
