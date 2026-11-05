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
package org.realtimeetl.biz.model;

import org.realtimeetl.sdk.model.CommonTaskSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Database migrationListSummary of progress: showing fine snapshots+ Watches are walking together /Source tableTotal (%)andQuick in order.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-09-24
 */
public class TableProgressBundle {

    /** It's all very specific.Meta SnapshotandTableID ListAlign the order.null） */
    private final List<CommonTaskSnapshot> snapshots = new ArrayList<>();

    /** Watches are on foot.success+fail，and snapshots tried)*/
    private final List<Long> syncedRowsPerTable = new ArrayList<>();

    /** TablesSourceTotal (%)NoneCache As0，and snapshots tried)*/
    private final List<Long> sourceTotalPerTable = new ArrayList<>();

    public List<CommonTaskSnapshot> getSnapshots() {
        return snapshots;
    }

    public List<Long> getSyncedRowsPerTable() {
        return syncedRowsPerTable;
    }

    public List<Long> getSourceTotalPerTable() {
        return sourceTotalPerTable;
    }

    /**
     * Add a table of progress samples (III)ListIt should be added in consecutive order).
     */
    public void addTable(CommonTaskSnapshot snapshot, long syncedRows, long sourceTotal) {
        snapshots.add(snapshot);
        syncedRowsPerTable.add(Math.max(0L, syncedRows));
        sourceTotalPerTable.add(Math.max(0L, sourceTotal));
    }

    public long sumSyncedRows() {
        long sum = 0L;
        for (Long rows : syncedRowsPerTable) {
            if (rows != null && rows > 0) {
                sum += rows;
            }
        }
        return sum;
    }

    public long sumSourceTotal() {
        long sum = 0L;
        for (Long rows : sourceTotalPerTable) {
            if (rows != null && rows > 0) {
                sum += rows;
            }
        }
        return sum;
    }
}
