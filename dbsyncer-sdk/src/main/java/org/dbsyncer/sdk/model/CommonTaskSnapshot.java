/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.model;

import org.dbsyncer.common.enums.CommonTaskStatusEnum;
import org.dbsyncer.sdk.enums.DatabaseSyncDetailTypeEnum;

import java.io.Serializable;

/**
 * UniversalTasksTable snapshot:step In the migration scene{@link DatabaseSyncDetailTypeEnum}，
 * UNITED NATIONSPeople commented:RightsServiceHighLevelCommissioner the view of the school{@link org.dbsyncer.sdk.enums.ValidateSyncStepEnum}。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-09 10:37
 */
public class CommonTaskSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Current Phase Encoding
     */
    private String step;

    /**
     * Current stageStatus，See{@link CommonTaskStatusEnum}
     */
    private int status;

    /**
     * Data FactMigrationminPageCursor
     */
    private String cursor;

    private long pageIndex;

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public long getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(long pageIndex) {
        this.pageIndex = pageIndex;
    }
}
