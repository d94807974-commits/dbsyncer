/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.util;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * TasksImplementation DetailsminTable(realtime_etl_task_detail)Tools.
 * <p>Validation/removalResultPresentationFieldFrom company table:table_group(Associated Columns) + meta(Indicators) + task_detail(Load)。
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-07-17 18:00
 */
public abstract class TaskDetailUtil {

    /**
     * MySQL BLOB Upper limitand realtime_etl_task_detail.DATA They must be intercepted before writing.Data truncation。
     */
    public static final int MAX_DATA_BYTES = 65535;

    private TaskDetailUtil() {
    }

    /**
     * Structure content sequence toDATA blob Bytes
     * <p>Over{@link #MAX_DATA_BYTES} Break first{@code content} Field，Keep it as far as you can.JSON Can be parsed.
     *
     * @param content StructureField
     * @return blob bytes, content is emptyBack null
     */
    public static byte[] serializeContent(Map<String, Object> content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        byte[] bytes = toUtf8Bytes(content);
        if (bytes.length <= MAX_DATA_BYTES) {
            return bytes;
        }
        // Prefer CutloadField，Guaranteed entryLibraryAnd try to reverse the sequence.
        Map<String, Object> copy = new HashMap<>(content);
        Object raw = copy.get(ConfigConstant.TASK_CONTENT);
        if (raw instanceof String && StringUtil.isNotBlank((String) raw)) {
            copy.put(ConfigConstant.TASK_CONTENT, StringUtil.EMPTY);
            int overhead = toUtf8Bytes(copy).length;
            int budget = Math.max(0, MAX_DATA_BYTES - overhead);
            copy.put(ConfigConstant.TASK_CONTENT, truncateUtf8((String) raw, budget));
            bytes = toUtf8Bytes(copy);
            if (bytes.length <= MAX_DATA_BYTES) {
                return bytes;
            }
        }
        // Bottom: Hard-cut by bytes (possible damage)JSON，Inverse sequenceFailedTimeBackEmptyMap）
        return truncateBytes(bytes, MAX_DATA_BYTES);
    }

    private static byte[] toUtf8Bytes(Map<String, Object> content) {
        return JsonUtil.objToJsonSafe(content).getBytes(StandardCharsets.UTF_8);
    }

    /**
     * PressUTF-8 byte upper limit cut a string, ensuring that it falls on the full character boundary.
     */
    private static String truncateUtf8(String value, int maxBytes) {
        if (value == null || maxBytes <= 0) {
            return StringUtil.EMPTY;
        }
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }
        int end = maxBytes;
        // If the cut point falls in the middle of a multi-byte character, back to the beginning of the character
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }
        return new String(bytes, 0, end, StandardCharsets.UTF_8);
    }

    private static byte[] truncateBytes(byte[] bytes, int maxBytes) {
        if (bytes == null || bytes.length <= maxBytes) {
            return bytes;
        }
        int end = maxBytes;
        while (end > 0 && (bytes[end] & 0xC0) == 0x80) {
            end--;
        }
        byte[] truncated = new byte[end];
        System.arraycopy(bytes, 0, truncated, 0, end);
        return truncated;
    }

    /**
     * WillDATA blob Inverse to structured contentMap
     *
     * @param dataBlob DetailsDATA ColumnsValue(byte[] orString)
     * @return StructureField，ErrorOr is it empty?BackEmptyMap
     */
    public static Map<String, Object> deserializeContent(Object dataBlob) {
        String json = null;
        if (dataBlob instanceof byte[]) {
            byte[] b = (byte[]) dataBlob;
            if (b.length > 0) {
                json = new String(b, StandardCharsets.UTF_8);
            }
        } else if (dataBlob instanceof String) {
            json = (String) dataBlob;
        }
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            Map<String, Object> map = JsonUtil.parseMap(json);
            return map == null ? new HashMap<>() : map;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    /**
     * When you've got a line on it.DATA blob StructureFieldMerge Back LinesMap。
     * <p>{@code isSuccess} For success or failure (Sync)Event 0/1）；Life cycle demonstration{@code status}/{@code state}。
     *
     * @param row Line(Key withlabelName)
     * @return Merged LinesMap
     */
    public static Map<String, Object> mergeDetailRow(Map<String, Object> row) {
        if (row == null) {
            return null;
        }
        Map<String, Object> content = deserializeContent(row.get(ConfigConstant.BINLOG_DATA));
        if (!content.isEmpty()) {
            content.forEach(row::putIfAbsent);
        }
        // TARGET_TABLE(targetTable) Map to FrontendVO RequirementstargetTableName
        if (!row.containsKey(ConfigConstant.DATA_TARGET_TABLE_NAME) && row.containsKey(ConfigConstant.DETAIL_TARGET_TABLE)) {
            row.put(ConfigConstant.DATA_TARGET_TABLE_NAME, row.get(ConfigConstant.DETAIL_TARGET_TABLE));
        }
        return row;
    }

    /**
     * Combination/relocationResultLines:table_group Associated Columns+ meta Indicators+ detail Load.
     *
     * @param detailRow     task_detail Okay.
     * @param tableGroupRow table_group PresentationField(Empty)
     * @param metaRow       meta PresentationField(Empty)
     * @return Combined Presentation Lines
     */
    public static Map<String, Object> assembleJoinedRow(Map<String, Object> detailRow,
                                                        Map<String, Object> tableGroupRow,
                                                        Map<String, Object> metaRow) {
        Map<String, Object> row = detailRow == null ? new HashMap<>() : new HashMap<>(detailRow);
        mergeDetailRow(row);
        if (tableGroupRow != null) {
            putIfPresent(row, ConfigConstant.TASK_SOURCE_TABLE_NAME, tableGroupRow.get(ConfigConstant.TABLE_GROUP_SOURCE_TABLE));
            putIfPresent(row, ConfigConstant.DATA_TARGET_TABLE_NAME, tableGroupRow.get(ConfigConstant.TABLE_GROUP_TARGET_TABLE));
            putIfPresent(row, ConfigConstant.DETAIL_TARGET_TABLE, tableGroupRow.get(ConfigConstant.TABLE_GROUP_TARGET_TABLE));
            putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_SOURCE_DATABASE, tableGroupRow.get(ConfigConstant.TABLE_GROUP_SOURCE_DATABASE));
            putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_SOURCE_SCHEMA, tableGroupRow.get(ConfigConstant.TABLE_GROUP_SOURCE_SCHEMA));
            putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_TARGET_DATABASE, tableGroupRow.get(ConfigConstant.TABLE_GROUP_TARGET_DATABASE));
            putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_TARGET_SCHEMA, tableGroupRow.get(ConfigConstant.TABLE_GROUP_TARGET_SCHEMA));
            putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_SOURCE_TABLE, tableGroupRow.get(ConfigConstant.TABLE_GROUP_SOURCE_TABLE));
            putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_TARGET_TABLE, tableGroupRow.get(ConfigConstant.TABLE_GROUP_TARGET_TABLE));
            putIfPresent(row, ConfigConstant.TASK_SOURCE_TOTAL, tableGroupRow.get(ConfigConstant.TABLE_GROUP_SOURCE_TOTAL));
            putIfPresent(row, ConfigConstant.TASK_TARGET_TOTAL, tableGroupRow.get(ConfigConstant.TABLE_GROUP_TARGET_TOTAL));
            putIfPresent(row, ConfigConstant.TABLE_GROUP_SORT_INDEX, tableGroupRow.get(ConfigConstant.TABLE_GROUP_SORT_INDEX));
        }
        if (metaRow != null) {
            putIfPresent(row, ConfigConstant.TASK_DIFF_TOTAL, metaRow.get(ConfigConstant.META_DIFF));
            putIfPresent(row, ConfigConstant.TASK_FIXED_TOTAL, metaRow.get(ConfigConstant.META_FIXED));
            putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_SUCCESS_TOTAL, metaRow.get(ConfigConstant.META_SUCCESS));
            putIfPresent(row, ConfigConstant.DATABASE_SYNC_DETAIL_FAIL_TOTAL, metaRow.get(ConfigConstant.META_FAIL));
            putIfPresent(row, ConfigConstant.META_TOTAL, metaRow.get(ConfigConstant.META_TOTAL));
            putIfPresent(row, ConfigConstant.META_STATE, metaRow.get(ConfigConstant.META_STATE));
            // StatusPrioritymeta.state；Frontend Compatibilitystatus
            if (metaRow.get(ConfigConstant.META_STATE) != null) {
                row.put(ConfigConstant.TASK_STATUS, metaRow.get(ConfigConstant.META_STATE));
            }
        }
        return row;
    }

    private static void putIfPresent(Map<String, Object> row, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String && StringUtil.isBlank((String) value)) {
            return;
        }
        row.put(key, value);
    }

    /**
     * WillMeta Model to ShowField Map
     */
    public static Map<String, Object> toMetaDisplayMap(Object total, Object success, Object fail,
                                                       Object diff, Object fixed, Object state) {
        Map<String, Object> map = new HashMap<>();
        map.put(ConfigConstant.META_TOTAL, total);
        map.put(ConfigConstant.META_SUCCESS, success);
        map.put(ConfigConstant.META_FAIL, fail);
        map.put(ConfigConstant.META_DIFF, diff);
        map.put(ConfigConstant.META_FIXED, fixed);
        map.put(ConfigConstant.META_STATE, state);
        return map;
    }

    /**
     * DetailsminUniform post-processing after table query: consolidationDATA blob → Apply Filter→ Sort 2000/01→ minPage。
     *
     * @param rows       Original Line Gathering(Every Main BehaviourMap)
     * @param filter     Filter Conditions(Empty)
     * @param comparator Sorter(Empty)
     * @param pageNum    PageCode(From1 Start)
     * @param pageSize   EveryPageNumber of bars
     * @return minPageResult
     */
    public static Paging pageDetails(Collection<?> rows, Predicate<Map<String, Object>> filter,
                                     Comparator<Map<String, Object>> comparator, int pageNum, int pageSize) {
        List<Map<String, Object>> merged = mergeAll(rows, filter);
        if (comparator != null) {
            merged.sort(comparator);
        }
        int safePageNum = Math.max(1, pageNum);
        int safePageSize = Math.max(1, pageSize);
        Paging paging = new Paging(safePageNum, safePageSize);
        paging.setTotal(merged.size());
        int offset = (safePageNum - 1) * safePageSize;
        if (offset < merged.size()) {
            paging.setData(merged.stream().skip(offset).limit(safePageSize).collect(Collectors.toList()));
        }
        return paging;
    }

    /**
     * MergeDATA blob and press undertookConditionStatistics after filtering
     *
     * @param rows   Original Line Gathering
     * @param filter Filter Conditions(Empty)
     * @return Superior
     */
    public static long countDetails(Collection<?> rows, Predicate<Map<String, Object>> filter) {
        return mergeAll(rows, filter).size();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> mergeAll(Collection<?> rows, Predicate<Map<String, Object>> filter) {
        List<Map<String, Object>> merged = new ArrayList<>();
        if (rows == null) {
            return merged;
        }
        for (Object o : rows) {
            if (!(o instanceof Map)) {
                continue;
            }
            Map<String, Object> row = mergeDetailRow(new HashMap<>((Map<String, Object>) o));
            if (filter == null || filter.test(row)) {
                merged.add(row);
            }
        }
        return merged;
    }
}
