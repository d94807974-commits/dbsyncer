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
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.model.CommonTaskSnapshot;
import org.realtimeetl.sdk.model.DatabaseSyncTableSnapshot;

import com.alibaba.fastjson2.TypeReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Run Snapshotand Meta.SNAPSHOT Translating.{@code realtime_etl_meta}，Not maintainedTask MemoryTree.
 * <p>TasksLevelIS_TASK_DETAIL=0）：Database migrationLibrary Map status Summary{@link ConfigConstant#META_SNAPSHOT_DATABASE}）；
 * DetailsMeta（TASK_ID=table_group.id）：Quit running the picture.{@link ConfigConstant#META_SNAPSHOT_TABLE_ONE}）。</p>
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-22 16:00
 */
public final class TaskSnapshotUtil {

    private TaskSnapshotUtil() {
    }

    /**
     * TasksLevel: individual written/updatedLibrary Map status。
     */
    public static Map<String, String> putMappingStatus(Map<String, String> metaSnapshot, int mappingIndex, int status) {
        Map<String, String> target = ensureMap(metaSnapshot);
        Map<String, Map<String, Object>> summary = readMappingStatusMap(target);
        Map<String, Object> one = new HashMap<>();
        one.put("status", status);
        summary.put(String.valueOf(mappingIndex), one);
        target.put(ConfigConstant.META_SNAPSHOT_DATABASE, JsonUtil.objToJson(summary));
        return target;
    }

    /**
     * TasksLevel: Read individualLibrary Map status（Nonethennull）。
     */
    public static Integer getMappingStatus(Map<String, String> metaSnapshot, int mappingIndex) {
        Map<String, Map<String, Object>> summary = readMappingStatusMap(metaSnapshot);
        Map<String, Object> one = summary.get(String.valueOf(mappingIndex));
        if (one == null || one.get("status") == null) {
            return null;
        }
        Object status = one.get("status");
        if (status instanceof Number) {
            return ((Number) status).intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(status).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * TasksLevel:LibraryTable GroupYesNoCompleted。
     */
    public static boolean isMappingDone(Map<String, String> metaSnapshot, int mappingIndex) {
        return CommonTaskStatusEnum.isDone(getMappingStatus(metaSnapshot, mappingIndex));
    }

    /**
     * TasksLevel:ClearLibrary Map status Summary.
     */
    public static Map<String, String> clearMappingStatusSummary(Map<String, String> metaSnapshot) {
        Map<String, String> target = ensureMap(metaSnapshot);
        target.put(ConfigConstant.META_SNAPSHOT_DATABASE, "{}");
        return target;
    }

    /**
     * Progress details: Scene of writing toResult Meta.SNAPSHOT。
     *
     * @param mappingIndex Database migrationOrganisationLibrary Map index；Validationnull
     */
    public static Map<String, String> writeTableSnapshot(Map<String, String> metaSnapshot,
                                                         CommonTaskSnapshot tableSnapshot,
                                                         Integer mappingIndex) {
        Map<String, String> target = ensureMap(metaSnapshot);
        if (tableSnapshot == null) {
            target.remove(ConfigConstant.META_SNAPSHOT_TABLE_ONE);
            target.remove(ConfigConstant.META_SNAPSHOT_MAPPING_INDEX);
            return target;
        }
        target.put(ConfigConstant.META_SNAPSHOT_TABLE_ONE, JsonUtil.objToJson(tableSnapshot));
        if (mappingIndex != null) {
            target.put(ConfigConstant.META_SNAPSHOT_MAPPING_INDEX, String.valueOf(mappingIndex));
        } else {
            target.remove(ConfigConstant.META_SNAPSHOT_MAPPING_INDEX);
        }
        return target;
    }

    /**
     * Detailed progress: Read table snapshots.
     */
    public static CommonTaskSnapshot readTableSnapshot(Map<String, String> metaSnapshot) {
        if (metaSnapshot == null) {
            return null;
        }
        String json = metaSnapshot.get(ConfigConstant.META_SNAPSHOT_TABLE_ONE);
        if (StringUtil.isBlank(json) || StringUtil.equals("{}", json.trim())) {
            return null;
        }
        DatabaseSyncTableSnapshot tableSnap = JsonUtil.jsonToObj(json, DatabaseSyncTableSnapshot.class);
        if (tableSnap != null) {
            return tableSnap;
        }
        return JsonUtil.jsonToObj(json, CommonTaskSnapshot.class);
    }

    /**
     * Progress detail: readLibrary Map index（Nonethennull）。
     */
    public static Integer readMappingIndex(Map<String, String> metaSnapshot) {
        if (metaSnapshot == null) {
            return null;
        }
        return parseIndex(metaSnapshot.get(ConfigConstant.META_SNAPSHOT_MAPPING_INDEX));
    }

    public static boolean hasDatabaseSnapshots(Map<String, String> metaSnapshot) {
        return hasNonEmptyJson(metaSnapshot, ConfigConstant.META_SNAPSHOT_DATABASE);
    }

    public static boolean hasTableSnapshot(Map<String, String> metaSnapshot) {
        return hasNonEmptyJson(metaSnapshot, ConfigConstant.META_SNAPSHOT_TABLE_ONE);
    }

    /**
     * ParsingTasksLevelLibrary Map status Summary asindex → status。
     */
    public static Map<Integer, Integer> readMappingStatusCodes(Map<String, String> metaSnapshot) {
        Map<Integer, Integer> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : readMappingStatusMap(metaSnapshot).entrySet()) {
            Integer index = parseIndex(entry.getKey());
            if (index == null || entry.getValue() == null || entry.getValue().get("status") == null) {
                continue;
            }
            Object status = entry.getValue().get("status");
            if (status instanceof Number) {
                result.put(index, ((Number) status).intValue());
            } else {
                try {
                    result.put(index, Integer.valueOf(String.valueOf(status).trim()));
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }
        return result;
    }

    private static Map<String, Map<String, Object>> readMappingStatusMap(Map<String, String> metaSnapshot) {
        if (metaSnapshot == null) {
            return new HashMap<>();
        }
        String json = metaSnapshot.get(ConfigConstant.META_SNAPSHOT_DATABASE);
        if (StringUtil.isBlank(json) || StringUtil.equals("{}", json.trim())) {
            return new HashMap<>();
        }
        Map<String, Map<String, Object>> parsed = JsonUtil.jsonToObj(json,
                new TypeReference<Map<String, Map<String, Object>>>() {
                });
        return parsed == null ? new HashMap<>() : new HashMap<>(parsed);
    }

    private static boolean hasNonEmptyJson(Map<String, String> metaSnapshot, String key) {
        if (metaSnapshot == null) {
            return false;
        }
        String json = metaSnapshot.get(key);
        return StringUtil.isNotBlank(json) && !StringUtil.equals("{}", json.trim());
    }

    private static Map<String, String> ensureMap(Map<String, String> metaSnapshot) {
        return metaSnapshot == null ? new HashMap<>() : metaSnapshot;
    }

    private static Integer parseIndex(String key) {
        if (StringUtil.isBlank(key)) {
            return null;
        }
        try {
            return Integer.valueOf(key.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
