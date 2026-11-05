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
import org.realtimeetl.sdk.model.DatabaseMapping;
import org.realtimeetl.sdk.model.DatabaseSyncTask;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Library Migration TaskList/Details VO。
 * <p>InterfaceAccess.{@code databaseMappings} Yes{@link DatabaseMappingVO}（Includes tables;
 * TasksEnduringLibraryMap still written in parentField（OnlyLibraryV.</p>
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-05-22 00:00
 */
public final class DatabaseSyncTaskVO extends DatabaseSyncTask {

    private final Connector sourceConnector;
    private final Connector targetConnector;

    /**
     * Edit/PresentationLibraryMaps (including tables)
     */
    private List<DatabaseMappingVO> mappingViews;

    private int mappingCount;
    /** TasksProgress0~100，ByTasks/DetailsMeta Calculate*/
    private BigDecimal progress;
    /** Failures（TasksAdministrationMeta CumulativeFAIL） */
    private long errorCount;
    /** TasksNumber of summary tablesTableGroup Number)*/
    private int totalTableCount;
    /** CompletedNumber of tables (total Mobilization; structure mode only)ListPresentation)*/
    private int completedTableCount;
    /** Structure phaseCompletedTableListPhase text)*/
    private int schemaCompletedCount;
    /** Total on foot (%)success+fail；When data are availableListPresentation)*/
    private long syncedRows;
    /** SourceTotal total (tables)sourceTotal Cache sum; when data phases are availableListPresentation)*/
    private long sourceTotal;
    /** TasksAdministrationMeta.state（(4) Current round of businessDONE=3） */
    private Integer metaState;
    /** Current roundStart time（TasksAdministrationMeta.startTime） */
    private Long startTime;

    public DatabaseSyncTaskVO(Connector sourceConnector, Connector targetConnector) {
        this.sourceConnector = sourceConnector;
        this.targetConnector = targetConnector;
    }

    public Connector getSourceConnector() {
        return sourceConnector;
    }

    public Connector getTargetConnector() {
        return targetConnector;
    }

    public List<DatabaseMappingVO> getMappingViews() {
        return mappingViews;
    }

    public void setMappingViews(List<DatabaseMappingVO> mappingViews) {
        this.mappingViews = mappingViews;
    }

    /**
     * Sequence to FrontendField {@code databaseMappings}（Element running as{@link DatabaseMappingVO}）。
     */
    @Override
    public List<DatabaseMapping> getDatabaseMappings() {
        if (mappingViews == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(mappingViews);
    }

    /**
     * BeanUtils / Interfaceinverse sequenced entry; if the element hasYes VO .
     */
    @Override
    public void setDatabaseMappings(List<DatabaseMapping> databaseMappings) {
        if (databaseMappings == null) {
            this.mappingViews = null;
            return;
        }
        List<DatabaseMappingVO> views = new ArrayList<>(databaseMappings.size());
        for (DatabaseMapping mapping : databaseMappings) {
            if (mapping instanceof DatabaseMappingVO) {
                views.add((DatabaseMappingVO) mapping);
            } else {
                views.add(DatabaseMappingVO.from(mapping));
            }
        }
        this.mappingViews = views;
    }

    public int getMappingCount() {
        return mappingCount;
    }

    public void setMappingCount(int mappingCount) {
        this.mappingCount = mappingCount;
    }

    public BigDecimal getProgress() {
        return progress;
    }

    public void setProgress(BigDecimal progress) {
        this.progress = progress;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = errorCount;
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

    public int getSchemaCompletedCount() {
        return schemaCompletedCount;
    }

    public void setSchemaCompletedCount(int schemaCompletedCount) {
        this.schemaCompletedCount = schemaCompletedCount;
    }

    public long getSyncedRows() {
        return syncedRows;
    }

    public void setSyncedRows(long syncedRows) {
        this.syncedRows = syncedRows;
    }

    public long getSourceTotal() {
        return sourceTotal;
    }

    public void setSourceTotal(long sourceTotal) {
        this.sourceTotal = sourceTotal;
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
