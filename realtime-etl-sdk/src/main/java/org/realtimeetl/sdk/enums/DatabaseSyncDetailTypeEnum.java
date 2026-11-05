/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.enums;

import org.realtimeetl.common.enums.CommonTaskStatusEnum;

/**
 * Database migrationDetailsType（{@code realtime_etl_task_database_sync_detail.TYPE}）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-05-29 16:00
 */
public enum DatabaseSyncDetailTypeEnum {

    /**
     * Structure Migration
     */
    TABLE_SCHEMA("tableSchema"),
    /**
     * Data FactMigration
     */
    ROW_DATA("rowData");

    private final String code;

    DatabaseSyncDetailTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static DatabaseSyncDetailTypeEnum ofCode(String code) {
        if (code == null) {
            return null;
        }
        for (DatabaseSyncDetailTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Structure phaseYesNoSide Closed (includes skipping); data phase considered structuralCompleted。
     */
    public boolean isSchemaPhaseDone(int status) {
        if (this == ROW_DATA) {
            return true;
        }
        return this == TABLE_SCHEMA && CommonTaskStatusEnum.isDone(status);
    }

    /**
     * Data FactPhaseYesNoClosed (includes skipping).
     */
    public boolean isDataPhaseDone(int status) {
        return this == ROW_DATA && CommonTaskStatusEnum.isDone(status);
    }

    /**
     * Structure stage judged by stage codeYesNoClosed.
     */
    public static boolean isSchemaPhaseDone(String stepCode, int status) {
        DatabaseSyncDetailTypeEnum step = ofCode(stepCode);
        return step != null && step.isSchemaPhaseDone(status);
    }

    /**
     * Data phase judged by stage codeYesNoClosed.
     */
    public static boolean isDataPhaseDone(String stepCode, int status) {
        DatabaseSyncDetailTypeEnum step = ofCode(stepCode);
        return step != null && step.isDataPhaseDone(status);
    }
}
