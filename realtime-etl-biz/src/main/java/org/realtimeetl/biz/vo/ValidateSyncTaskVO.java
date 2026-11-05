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
package org.realtimeetl.biz.vo;

import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.sdk.model.ValidateSyncTask;

import java.math.BigDecimal;

public final class ValidateSyncTaskVO extends ValidateSyncTask {
    // Connector
    private final Connector sourceConnector;
    private final Connector targetConnector;
    // ErrorNumber WorkshopsTasksAdministrationMeta CumulativeVarianceapologized to me
    private long errorCount;
    //Current progress
    private BigDecimal progress;
    // TableTotal
    private int totalTableCount;
    // CompletedTables
    private int completedTableCount;
    /** TasksAdministrationMeta.state（(4) Current round of businessDONE=3） */
    private Integer metaState;
    /** Current roundStart time（TasksAdministrationMeta.startTime） */
    private Long startTime;

    public ValidateSyncTaskVO(Connector sourceConnector, Connector targetConnector) {
        this.sourceConnector = sourceConnector;
        this.targetConnector = targetConnector;
    }

    public Connector getSourceConnector() {
        return sourceConnector;
    }

    public Connector getTargetConnector() {
        return targetConnector;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    public int getTotalTableCount() {
        return totalTableCount;
    }

    public void setTotalTableCount(int totalTableCount) {
        this.totalTableCount = totalTableCount;
    }

    public int getCompletedTableCount() {
        return completedTableCount;
    }

    public void setCompletedTableCount(int completedTableCount) {
        this.completedTableCount = completedTableCount;
    }

    public Integer getMetaState() {
        return metaState;
    }

    public void setMetaState(Integer metaState) {
        this.metaState = metaState;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
}
