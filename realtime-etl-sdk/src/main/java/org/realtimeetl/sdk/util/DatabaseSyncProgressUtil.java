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
package org.realtimeetl.sdk.util;

import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.sdk.enums.DatabaseSyncDetailTypeEnum;
import org.realtimeetl.sdk.model.CommonTaskSnapshot;
import org.realtimeetl.sdk.model.DatabaseSyncTask;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Library Migration TaskProgress: PressCompositeMode Count (structure / only)Data / Synchronise.
 * <p>Structure+When Sovereignty is open,minPhase: Structure incomplete by chart degrees0~100；StructureFinishThen reset from the data level0~100。
 * <p>Data phase by「Qualifications」Summary of table rowsFinishdegrees, avoid only a few returns WritesourceTotal GlobalminThe mother's smallness Factes that progress is weak.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public final class DatabaseSyncProgressUtil {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ONE = BigDecimal.ONE;

    private DatabaseSyncProgressUtil() {
    }

    /**
     * Count 100% progressminThat's right.0~100。
     *
     * @param task                TasksConfigure（Switchtip)
     * @param tableGroupSize      MapTotal
     * @param roundDone           TasksAdministrationMeta YesNoCurrent roundCompleted（STATE=DONE）
     * @param tableSnapshots      It's all very specific.Meta Reliablenull）
     * @param syncedRowsPerTable  Watches are on foot.and snapshots Reciprocal, empty)
     * @param sourceTotalPerTable TablesSourceTotal (%)and snapshots (a) In the same order, be empty;NoneCache As0）
     */
    public static BigDecimal calculateProgressPercent(DatabaseSyncTask task, int tableGroupSize,
                                                      boolean roundDone,
                                                      List<CommonTaskSnapshot> tableSnapshots,
                                                      List<Long> syncedRowsPerTable,
                                                      List<Long> sourceTotalPerTable) {
        if (task == null) {
            return null;
        }
        if (roundDone) {
            return new BigDecimal("100.00");
        }
        boolean schema = task.isEnableCopySchema();
        boolean data = task.isEnableCopyData();
        if (!schema && !data) {
            return null;
        }
        if (schema && !data) {
            return ratioPercent(countSchemaDoneTables(tableSnapshots), tableGroupSize);
        }
        BigDecimal dataPart = tableWeightedDataRatio(tableSnapshots, syncedRowsPerTable, sourceTotalPerTable, tableGroupSize);
        if (!schema) {
            return dataPart.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
        }
        // Synchronization: full structure phase100 Then we're going to the data phase.0 Total
        BigDecimal schemaPart = ratio(countSchemaDoneTables(tableSnapshots), tableGroupSize);
        if (schemaPart.compareTo(ONE) < 0) {
            return schemaPart.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
        }
        return dataPart.multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * ListPresentedCompletedTable:EnablePhasesCompletedThe table.
     */
    public static int countCompletedTables(DatabaseSyncTask task, int totalTableCount, boolean roundDone,
                                           List<CommonTaskSnapshot> tableSnapshots) {
        if (task == null) {
            return 0;
        }
        if (roundDone && totalTableCount > 0) {
            return totalTableCount;
        }
        if (totalTableCount <= 0 || CollectionUtils.isEmpty(tableSnapshots)) {
            return 0;
        }
        int count = 0;
        for (CommonTaskSnapshot tableSnapshot : tableSnapshots) {
            if (isTableFullyDone(task, tableSnapshot)) {
                count++;
            }
        }
        return Math.min(count, totalTableCount);
    }

    /**
     * Data progress: equal rights per table. FormFinishdegrees= Completed 1；Yes Notably.sourceTotal thensynced/source；Nothen0。
     */
    private static BigDecimal tableWeightedDataRatio(List<CommonTaskSnapshot> tableSnapshots,
                                                     List<Long> syncedRowsPerTable,
                                                     List<Long> sourceTotalPerTable,
                                                     int tableGroupSize) {
        if (tableGroupSize <= 0) {
            return BigDecimal.ZERO;
        }
        if (CollectionUtils.isEmpty(tableSnapshots)) {
            return BigDecimal.ZERO;
        }
        boolean hasPerTable = !CollectionUtils.isEmpty(syncedRowsPerTable)
                && !CollectionUtils.isEmpty(sourceTotalPerTable)
                && syncedRowsPerTable.size() == tableSnapshots.size()
                && sourceTotalPerTable.size() == tableSnapshots.size();
        if (!hasPerTable) {
            return ratio(countDataDoneTables(tableSnapshots), tableGroupSize);
        }
        BigDecimal sum = BigDecimal.ZERO;
        int n = Math.min(tableGroupSize, tableSnapshots.size());
        for (int i = 0; i < n; i++) {
            sum = sum.add(singleTableDataRatio(tableSnapshots.get(i),
                    syncedRowsPerTable.get(i), sourceTotalPerTable.get(i)));
        }
        return sum.divide(BigDecimal.valueOf(tableGroupSize), 6, RoundingMode.HALF_UP);
    }

    private static BigDecimal singleTableDataRatio(CommonTaskSnapshot snapshot, Long syncedRows, Long sourceTotal) {
        if (snapshot != null
                && DatabaseSyncDetailTypeEnum.isDataPhaseDone(snapshot.getStep(), snapshot.getStatus())) {
            return ONE;
        }
        long source = sourceTotal == null ? 0L : Math.max(0L, sourceTotal);
        if (source <= 0L) {
            return BigDecimal.ZERO;
        }
        long synced = syncedRows == null ? 0L : Math.max(0L, syncedRows);
        return ratio(Math.min(synced, source), source);
    }

    private static BigDecimal ratioPercent(long completed, long total) {
        return ratio(completed, total).multiply(HUNDRED).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal ratio(long completed, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        long capped = Math.min(Math.max(completed, 0L), total);
        return BigDecimal.valueOf(capped)
                .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP);
    }

    public static int countSchemaDoneTables(List<CommonTaskSnapshot> tableSnapshots) {
        if (CollectionUtils.isEmpty(tableSnapshots)) {
            return 0;
        }
        int count = 0;
        for (CommonTaskSnapshot tableSnapshot : tableSnapshots) {
            if (tableSnapshot != null
                    && DatabaseSyncDetailTypeEnum.isSchemaPhaseDone(tableSnapshot.getStep(), tableSnapshot.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public static int countDataDoneTables(List<CommonTaskSnapshot> tableSnapshots) {
        if (CollectionUtils.isEmpty(tableSnapshots)) {
            return 0;
        }
        int count = 0;
        for (CommonTaskSnapshot tableSnapshot : tableSnapshots) {
            if (tableSnapshot != null
                    && DatabaseSyncDetailTypeEnum.isDataPhaseDone(tableSnapshot.getStep(), tableSnapshot.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private static boolean isTableFullyDone(DatabaseSyncTask task, CommonTaskSnapshot tableSnapshot) {
        if (tableSnapshot == null) {
            return false;
        }
        if (task.isEnableCopySchema()
                && !DatabaseSyncDetailTypeEnum.isSchemaPhaseDone(tableSnapshot.getStep(), tableSnapshot.getStatus())) {
            return false;
        }
        if (task.isEnableCopyData()
                && !DatabaseSyncDetailTypeEnum.isDataPhaseDone(tableSnapshot.getStep(), tableSnapshot.getStatus())) {
            return false;
        }
        return task.isEnableCopySchema() || task.isEnableCopyData();
    }

    /**
     * TasksAdministrationMeta.state YesNoFor current cycleCompleted（and {@link org.realtimeetl.common.enums.CommonTaskStatusEnum#DONE} Same number.
     */
    public static boolean isRoundDone(Integer metaState) {
        return CommonTaskStatusEnum.isDone(metaState);
    }
}
