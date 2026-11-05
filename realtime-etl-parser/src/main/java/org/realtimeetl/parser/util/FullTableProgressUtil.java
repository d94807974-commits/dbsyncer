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
package org.realtimeetl.parser.util;

import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.common.enums.TaskLevelEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.enums.ParserEnum;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.model.CommonTaskSnapshot;
import org.realtimeetl.sdk.util.TaskSnapshotUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Full syncTable of progress
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-11
 */
public abstract class FullTableProgressUtil {

    private FullTableProgressUtil() {
    }

    /**
     * Parsing Table LevelsMeta。
     *
     * @param taskMetaProfile Meta Services
     * @param tableGroupId    MapID
     * @return DetailsMeta，Maybe.null
     */
    public static Meta resolve(TaskMetaProfile taskMetaProfile, String tableGroupId) {
        if (taskMetaProfile == null || StringUtil.isBlank(tableGroupId)) {
            return null;
        }
        return taskMetaProfile.getMetaDetail(tableGroupId);
    }

    /**
     * TableYesNoCompleted
     *
     * @param taskMetaProfile Meta Services
     * @param tableGroupId    MapID
     * @return true Completed
     */
    public static boolean isDone(TaskMetaProfile taskMetaProfile, String tableGroupId) {
        Meta meta = resolve(taskMetaProfile, tableGroupId);
        if (meta == null) {
            return false;
        }
        if (CommonTaskStatusEnum.isDone(meta.getState())) {
            return true;
        }
        CommonTaskSnapshot snap = TaskSnapshotUtil.readTableSnapshot(meta.getSnapshot());
        return snap != null && CommonTaskStatusEnum.isDone(snap.getStatus());
    }

    /**
     * Read or initialize table progress (do not write)Library）。
     *
     * @param taskMetaProfile Meta Services
     * @param tableGroupId    MapID
     * @return Progress.null
     */
    public static CommonTaskSnapshot getOrInit(TaskMetaProfile taskMetaProfile, String tableGroupId) {
        Meta meta = resolve(taskMetaProfile, tableGroupId);
        if (meta != null) {
            CommonTaskSnapshot snap = TaskSnapshotUtil.readTableSnapshot(meta.getSnapshot());
            if (snap != null) {
                return snap;
            }
        }
        return readySnapshot();
    }

    /**
     * OverwriteWrite Form to DetailsMeta（Noneis created.
     * <p>Period Existing{@link TaskMetaProfile#updateMetaProgress} Reload Before Writing, AvoidOverwriteExclusive Refer POLICY Continuity Accommodation Moratorium
     *
     * @param taskMetaProfile Meta Services
     * @param tableGroupId    MapID
     * @param snapshot        Table snapshots;null OrganisationClearQuick!state=READY
     */
    public static void save(TaskMetaProfile taskMetaProfile, String tableGroupId, CommonTaskSnapshot snapshot) {
        if (taskMetaProfile == null || StringUtil.isBlank(tableGroupId)) {
            return;
        }
        synchronized (tableLock(tableGroupId)) {
            Meta meta = resolve(taskMetaProfile, tableGroupId);
            long now = System.currentTimeMillis();
            if (meta == null || StringUtil.isBlank(meta.getId())) {
                meta = new Meta();
                meta.setTaskId(tableGroupId);
                meta.setIsTaskDetail(TaskLevelEnum.TASK_DETAIL.getCode());
                meta.setCreateTime(now);
                apply(meta, snapshot, now);
                taskMetaProfile.addMeta(meta);
                return;
            }
            Map<String, String> newSnap = TaskSnapshotUtil.writeTableSnapshot(meta.getSnapshot(), snapshot, null);
            int state = snapshot == null
                    ? CommonTaskStatusEnum.READY.getCode()
                    : snapshot.getStatus();
            taskMetaProfile.updateMetaProgress(meta.getTaskId(), state, newSnap);
        }
    }

    /**
     * PressPageClearTasksconcludeAllShow details.Clear、state=READY；Reservationssuccess/fail I Appeal Articles Democracy Issues
     *
     * @param taskMetaProfile   Meta Services
     * @param tableGroupProfile Map Service
     * @param taskId            Tasks ID
     */
    public static void clearAll(TaskMetaProfile taskMetaProfile, TableGroupProfile tableGroupProfile, String taskId) {
        if (taskMetaProfile == null || tableGroupProfile == null || StringUtil.isBlank(taskId)) {
            return;
        }
        tableGroupProfile.pageScanTableGroups(taskId, ConfigConstant.PAGE_SIZE, page ->
                clearAll(taskMetaProfile, toTableGroupIds(page)));
    }

    /**
     * ClearDetailed progress for given map (scatter)Clear、state=READY；Reservationssuccess/fail I Appeal Articles Democracy Issues
     *
     * @param taskMetaProfile Meta Services
     * @param tableGroupIds   This batch mapID
     */
    public static void clearAll(TaskMetaProfile taskMetaProfile, List<String> tableGroupIds) {
        if (taskMetaProfile == null || CollectionUtils.isEmpty(tableGroupIds)) {
            return;
        }
        for (String tableGroupId : tableGroupIds) {
            if (StringUtil.isBlank(tableGroupId)) {
                continue;
            }
            save(taskMetaProfile, tableGroupId, null);
        }
    }

    /**
     * ResetTasksconcludeAllShow fineMeta：Reservations;state=READY、PhotoClear、Count Enhancement is zero; missing is created.
     * <p>Batch{@link TaskMetaProfile#getDetailMetaMap} reading;missing batchesADD，Exists article by articleUPDATE。
     */
    public static void resetAll(TaskMetaProfile taskMetaProfile, List<String> tableGroupIds) {
        if (taskMetaProfile == null || CollectionUtils.isEmpty(tableGroupIds)) {
            return;
        }
        Map<String, Meta> existing = taskMetaProfile.getDetailMetaMap(tableGroupIds);
        List<Meta> addList = new ArrayList<>();
        List<Meta> updateList = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (String tableGroupId : tableGroupIds) {
            synchronized (tableLock(tableGroupId)) {
                Meta meta = existing.get(tableGroupId);
                if (meta == null || StringUtil.isBlank(meta.getId())) {
                    meta = new Meta();
                    meta.setTaskId(tableGroupId);
                    meta.setIsTaskDetail(TaskLevelEnum.TASK_DETAIL.getCode());
                    meta.setCreateTime(now);
                    apply(meta, null, now);
                    addList.add(meta);
                    continue;
                }
                meta.clear();
                Map<String, String> newSnap = TaskSnapshotUtil.writeTableSnapshot(meta.getSnapshot(), null, null);
                meta.setState(CommonTaskStatusEnum.READY.getCode());
                meta.setIsTaskDetail(TaskLevelEnum.TASK_DETAIL.getCode());
                meta.setSnapshot(newSnap);
                meta.setStartTime(0L);
                meta.setUpdateTime(now);
                meta.setTotal(new AtomicLong(0));
                meta.setSuccess(new AtomicLong(0));
                meta.setFail(new AtomicLong(0));
                meta.setDiff(new AtomicLong(0));
                meta.setFixed(new AtomicLong(0));
                updateList.add(meta);
            }
        }
        taskMetaProfile.addMetaBatch(addList);
        taskMetaProfile.updateMetaBatch(updateList);
    }

    /**
     * TasksconcludeYesNoExistingFinishTable of progress (byPageScan, load Thirty-one.AllThe map.
     *
     * @param taskMetaProfile   Meta Services
     * @param tableGroupProfile Map Service
     * @param taskId            Tasks ID
     * @return true ExistingFinishProgress
     */
    public static boolean hasIncomplete(TaskMetaProfile taskMetaProfile, TableGroupProfile tableGroupProfile, String taskId) {
        if (taskMetaProfile == null || tableGroupProfile == null || StringUtil.isBlank(taskId)) {
            return false;
        }
        AtomicBoolean incomplete = new AtomicBoolean(false);
        tableGroupProfile.pageScanTableGroups(taskId, ConfigConstant.PAGE_SIZE, page -> {
            if (incomplete.get()) {
                return;
            }

            if (hasIncomplete(taskMetaProfile, toTableGroupIds(page))) {
                incomplete.set(true);
            }
        });
        return incomplete.get();
    }

    /**
     * YesNoExistingFinish. . . . .DONE，orstate YesRunning/Stopping）。
     *
     * @param taskMetaProfile Meta Services
     * @param tableGroupIds   This batch mapID
     * @return true ExistingFinishProgress
     */
    public static boolean hasIncomplete(TaskMetaProfile taskMetaProfile, List<String> tableGroupIds) {
        if (taskMetaProfile == null || CollectionUtils.isEmpty(tableGroupIds)) {
            return false;
        }
        for (String tableGroupId : tableGroupIds) {
            if (StringUtil.isBlank(tableGroupId) || isDone(taskMetaProfile, tableGroupId)) {
                continue;
            }
            Meta meta = resolve(taskMetaProfile, tableGroupId);
            if (meta == null) {
                continue;
            }
            if (CommonTaskStatusEnum.isRunning(meta.getState())) {
                return true;
            }
            CommonTaskSnapshot snap = TaskSnapshotUtil.readTableSnapshot(meta.getSnapshot());
            if (snap == null) {
                continue;
            }
            if (CommonTaskStatusEnum.isRunning(snap.getStatus())
                    || snap.getPageIndex() > ParserEnum.PAGE_INDEX.getDefaultValue()
                    || StringUtil.isNotBlank(snap.getCursor())) {
                return true;
            }
        }
        return false;
    }

    /**
     * ConstructRunningSc 49.
     *
     * @param pageIndex PageCode
     * @param cursor    Cursor
     * @return Photo
     */
    public static CommonTaskSnapshot runningSnapshot(int pageIndex, String cursor) {
        CommonTaskSnapshot snap = new CommonTaskSnapshot();
        snap.setStatus(CommonTaskStatusEnum.RUNNING.getCode());
        snap.setPageIndex(pageIndex > 0 ? pageIndex : ParserEnum.PAGE_INDEX.getDefaultValue());
        snap.setCursor(StringUtil.getIfBlank(cursor, StringUtil.EMPTY));
        return snap;
    }

    /**
     * ConstructCompletedSc 49.
     *
     * @return Photo
     */
    public static CommonTaskSnapshot doneSnapshot() {
        CommonTaskSnapshot snap = new CommonTaskSnapshot();
        snap.setStatus(CommonTaskStatusEnum.DONE.getCode());
        snap.setPageIndex(ParserEnum.PAGE_INDEX.getDefaultValue());
        snap.setCursor(StringUtil.EMPTY);
        return snap;
    }

    /**
     * TableLevelWriteLock（and FlushStrategyImpl Show countIncrementalTotalUse the same key.
     *
     * @param tableGroupId MapID
     * @return Lock 15:00
     */
    public static Object tableLock(String tableGroupId) {
        String id = StringUtil.isBlank(tableGroupId) ? StringUtil.EMPTY : tableGroupId;
        return ("table-meta-" + id).intern();
    }

    private static List<String> toTableGroupIds(List<TableGroup> groups) {
        if (CollectionUtils.isEmpty(groups)) {
            return Collections.emptyList();
        }
        return groups.stream().map(TableGroup::getId).collect(Collectors.toList());
    }

    private static CommonTaskSnapshot readySnapshot() {
        CommonTaskSnapshot snap = new CommonTaskSnapshot();
        snap.setStatus(CommonTaskStatusEnum.READY.getCode());
        snap.setPageIndex(ParserEnum.PAGE_INDEX.getDefaultValue());
        snap.setCursor(StringUtil.EMPTY);
        return snap;
    }

    private static void apply(Meta meta, CommonTaskSnapshot snapshot, long now) {
        Map<String, String> snap = TaskSnapshotUtil.writeTableSnapshot(meta.getSnapshot(), snapshot, null);
        meta.setSnapshot(snap);
        if (snapshot == null) {
            meta.setState(CommonTaskStatusEnum.READY.getCode());
        } else {
            meta.setState(snapshot.getStatus());
        }
        meta.setUpdateTime(now);
    }
}
