/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.model;

import org.dbsyncer.sdk.model.CommonTaskSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Database migrationListSummary of progress: showing fine snapshots+ Watches are walking together /Source tableTotal (%)andQuick in order.
 *
 * @author wuji
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
