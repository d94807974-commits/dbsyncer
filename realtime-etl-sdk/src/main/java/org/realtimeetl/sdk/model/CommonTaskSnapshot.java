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
package org.realtimeetl.sdk.model;

import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.sdk.enums.DatabaseSyncDetailTypeEnum;

import java.io.Serializable;

/**
 * UniversalTasksTable snapshot:step In the migration scene{@link DatabaseSyncDetailTypeEnum}，
 * UNITED NATIONSPeople commented:RightsServiceHighLevelCommissioner the view of the school{@link org.realtimeetl.sdk.enums.ValidateSyncStepEnum}。
 *
 * @author Realtime ETL
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
