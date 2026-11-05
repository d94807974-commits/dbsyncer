/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.controller.cluster;

import com.alibaba.fastjson2.TypeReference;
import org.dbsyncer.biz.vo.ClusterMetricsOverviewVO;
import org.dbsyncer.biz.vo.ClusterNodeMetricVO;
import org.dbsyncer.biz.vo.HistoryStackVO;
import org.dbsyncer.biz.vo.RestResult;
import org.dbsyncer.common.model.HttpResult;
import org.dbsyncer.common.util.BatchTaskUtil;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.DateFormatUtil;
import org.dbsyncer.common.util.HttpClientUtil;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.common.util.NumberUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.common.util.UnderlineToCamelUtils;
import org.dbsyncer.parser.SystemConfigProfile;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.dbsyncer.sdk.enums.ModelEnum;
import org.dbsyncer.sdk.model.ClusterNode;
import org.dbsyncer.sdk.spi.ClusterService;
import org.dbsyncer.sdk.storage.ExecuteRequest;
import org.dbsyncer.sdk.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedCaseInsensitiveMap;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ThroughNodes HTTP Address draws running indicators and aggregates them.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-08-20
 */
@Service
public class ClusterNodeMetricAggregator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int CONNECT_TIMEOUT_MS = 2000;
    private static final int READ_TIMEOUT_MS = 3000;
    private static final int PULL_CONCURRENCY = 8;
    private static final int CHART_HISTORY_COUNT = 12;
    private final HistoryStackVO chartQueue = new HistoryStackVO();
    private final HistoryStackVO chartFullWorkItems = new HistoryStackVO();

    @Resource
    private LocalNodeMetricProvider localNodeMetricProvider;

    @Resource
    private StorageService storageService;

    @Resource
    private ClusterService clusterService;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    /**
     * Pull manipulateAllNodesIndicator (inline, remote)HTTP）。
     *
     * @return Overview
     */
    public ClusterMetricsOverviewVO collectAll() {
        Map<String, String> query = new HashMap<>();
        query.put("pageNum", "1");
        query.put("pageSize", "100");
        List<ClusterNode> nodes = (List<ClusterNode>) clusterService.query(query).getData();
        Map<String, Integer> workItemByNode = resolveFullWorkItemCounts();
        Map<String, Integer> incByNode = resolveIncrementalCounts();
        List<ClusterNode> remotes = new ArrayList<>();
        List<ClusterNodeMetricVO> metrics = new ArrayList<>();
        for (ClusterNode node : nodes) {
            // IfYesHere.
            if (node.isLocal()) {
                metrics.add(pullOne(node, workItemByNode, incByNode));
                continue;
            }
            // Far
            remotes.add(node);
        }
        // Parallel AccessOtherNodesInfo
        if (!CollectionUtils.isEmpty(remotes)) {
            metrics.addAll(BatchTaskUtil.submit(remotes, node -> pullOne(node, workItemByNode, incByNode), Math.min(PULL_CONCURRENCY, Math.max(1, remotes.size())), logger));
        }
        ClusterMetricsOverviewVO overview = new ClusterMetricsOverviewVO();
        double totalTps = 0D;
        long totalQueue = 0L;
        long totalWorkItems = 0L;
        long totalInc = 0L;
        for (ClusterNodeMetricVO item : metrics) {
            if (item == null) {
                continue;
            }
            overview.getNodes().add(item);
            if (item.isReachable()) {
                totalTps += item.getTps();
                totalQueue += item.getQueueUp();
            }
            totalWorkItems += item.getFullWorkItemCount();
            totalInc += item.getIncrementalCount();
        }
        overview.setTotalTps(Math.floor(totalTps));
        overview.setTotalQueue(totalQueue);
        overview.setTotalFullWorkItems(totalWorkItems);
        overview.setTotalIncremental(totalInc);
        // TPS UseNodessecondsLevel sequences;Backlog/FullminHit it and press the same set.labels We're on the same page.X Axisymmetrical
        HistoryStackVO tps = mergeTpsSeries(metrics);
        String pointLabel = lastSeriesLabel(tps);
        if (StringUtil.isBlank(pointLabel)) {
            pointLabel = DateFormatUtil.getCurrentTime();
        }
        pushChartPoint(chartQueue, pointLabel, overview.getTotalQueue());
        pushChartPoint(chartFullWorkItems, pointLabel, overview.getTotalFullWorkItems());
        overview.setTps(tps);
        if (CollectionUtils.isEmpty(tps.getName())) {
            overview.setQueue(snapshotHistory(chartQueue));
            overview.setFullWorkItems(snapshotHistory(chartFullWorkItems));
        } else {
            overview.setQueue(alignToLabels(tps.getName(), chartQueue));
            overview.setFullWorkItems(alignToLabels(tps.getName(), chartFullWorkItems));
        }
        return overview;
    }

    /**
     * additional sampling points;samesecondsInternal RepeatRefreshthenOverwriteEndpoint, avoid.TimeAxis repeat.
     */
    private void pushChartPoint(HistoryStackVO history, String label, double value) {
        if (history == null || StringUtil.isBlank(label)) {
            return;
        }
        List<Object> names = history.getName();
        List<Object> values = history.getValue();
        if (!CollectionUtils.isEmpty(names) && StringUtil.equals(label, String.valueOf(names.get(names.size() - 1)))) {
            values.set(values.size() - 1, value);
        } else {
            history.addName(label);
            history.addValue(value);
        }
        while (history.getName().size() > CHART_HISTORY_COUNT) {
            history.getName().remove(0);
            history.getValue().remove(0);
        }
        history.setAverage(average(history.getValue()));
    }

    /**
     * Press a thin sampleTimeStep toObjective Combined labels（and TPS secondsIt's the same axis.
     */
    private HistoryStackVO alignToLabels(List<Object> labels, HistoryStackVO history) {
        HistoryStackVO result = new HistoryStackVO();
        if (CollectionUtils.isEmpty(labels)) {
            return result;
        }
        List<Object> histNames = history == null ? null : history.getName();
        List<Object> histValues = history == null ? null : history.getValue();
        int histSize = histNames == null || histValues == null ? 0 : Math.min(histNames.size(), histValues.size());
        int cursor = 0;
        double last = 0D;
        for (Object labelObj : labels) {
            String label = String.valueOf(labelObj);
            while (cursor < histSize) {
                String histLabel = String.valueOf(histNames.get(cursor));
                if (histLabel.compareTo(label) > 0) {
                    break;
                }
                Object raw = histValues.get(cursor);
                last = raw instanceof Number ? ((Number) raw).doubleValue() : 0D;
                cursor++;
            }
            result.addName(label);
            result.addValue(last);
        }
        result.setAverage(average(result.getValue()));
        return result;
    }

    private String lastSeriesLabel(HistoryStackVO series) {
        if (series == null || CollectionUtils.isEmpty(series.getName())) {
            return null;
        }
        Object last = series.getName().get(series.getName().size() - 1);
        return last == null ? null : String.valueOf(last);
    }

    private HistoryStackVO snapshotHistory(HistoryStackVO source) {
        HistoryStackVO snapshot = new HistoryStackVO();
        if (source == null) {
            return snapshot;
        }
        snapshot.setName(new ArrayList<>(source.getName()));
        snapshot.setValue(new ArrayList<>(source.getValue()));
        snapshot.setAverage(source.getAverage());
        return snapshot;
    }

    private double average(List<Object> values) {
        if (CollectionUtils.isEmpty(values)) {
            return 0D;
        }
        double sum = 0D;
        for (Object value : values) {
            if (value instanceof Number) {
                sum += ((Number) value).doubleValue();
            }
        }
        return Math.floor(sum / values.size());
    }

    private ClusterNodeMetricVO pullOne(ClusterNode node, Map<String, Integer> workItemByNode, Map<String, Integer> incByNode) {
        ClusterNodeMetricVO vo;
        if (node.isLocal()) {
            vo = localNodeMetricProvider.snapshot();
        } else {
            vo = pullRemote(node);
        }
        vo.setNodeId(node.getNodeId());
        vo.setName(StringUtil.getIfBlank(node.getName(), node.getNodeId()));
        vo.setStatus(node.getStatus());
        vo.setLocal(node.isLocal());
        vo.setIp(node.getIp());
        vo.setHttpPort(node.getHttpPort());
        vo.setFullWorkItemCount(workItemByNode.getOrDefault(node.getNodeId(), 0));
        vo.setIncrementalCount(incByNode.getOrDefault(node.getNodeId(), 0));
        return vo;
    }

    private ClusterNodeMetricVO pullRemote(ClusterNode node) {
        // AlreadyOffline
        if (node.getStatus() == 0) {
            return unreachable();
        }
        String base = localNodeMetricProvider.buildHttpUrl(node.getIp(), node.getHttpPort());
        if (StringUtil.isBlank(base)) {
            return unreachable();
        }
        try {
            HttpResult result = HttpClientUtil.get(base + "/cluster/internal/metrics", HttpClientUtil.clusterTokenHeaders(getClusterSecret()), CONNECT_TIMEOUT_MS, READ_TIMEOUT_MS);
            if (!result.isOk()) {
                logger.warn("Node indicator failed, node={, http=}", node.getNodeId(), result.getStatusCode());
                return unreachable();
            }
            RestResult<ClusterNodeMetricVO> res = JsonUtil.jsonToObj(result.getBody(), new TypeReference<RestResult<ClusterNodeMetricVO>>() {
            });
            if (res == null || !res.isSuccess()) {
                return unreachable();
            }
            ClusterNodeMetricVO vo = res.getData();
            if (vo == null) {
                return unreachable();
            }
            vo.setReachable(true);
            return vo;
        } catch (Exception e) {
            logger.warn("Pull node indicator anomaly", node.getId(), e.getMessage());
            return unreachable();
        }
    }

    public String getClusterSecret() {
        return systemConfigProfile.getSystemConfig().getClusterSecret();
    }

    private ClusterNodeMetricVO unreachable() {
        ClusterNodeMetricVO vo = new ClusterNodeMetricVO();
        vo.setReachable(false);
        vo.setCpuPercent(BigDecimal.ZERO);
        vo.setMemoryUsed(BigDecimal.ZERO);
        vo.setMemoryTotal(BigDecimal.ZERO);
        vo.setDiskUsed(BigDecimal.ZERO);
        vo.setDiskTotal(BigDecimal.ZERO);
        return vo;
    }

    private Map<String, Integer> resolveFullWorkItemCounts() {
        // FullminScrap: By ImplementationNodesStatisticsdbsyncer_cluster_task_plan（HamCompletedNot yet deletedDONE）
        try {
            String sql = "SELECT NODE_ID, COUNT(*) AS CNT FROM " + ConfigConstant.CLUSTER_TASK_PLAN_TABLE
                    + " WHERE NODE_ID IS NOT NULL AND STATUS IN (0, 1, 2) GROUP BY NODE_ID";
            return toNodeCountMap(storageService.queryList(ExecuteRequest.of(sql)));
        } catch (Exception e) {
            logger.warn("Failed to load full fraction statistics: {", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Integer> resolveIncrementalCounts() {
        // ScheduleTASK_TYPE Yesmapping；Synchronisation Methodmodel Yes.dbsyncer_task.JSON
        try {
            String sql = "SELECT t.NODE_ID, COUNT(*) AS CNT FROM " + ConfigConstant.CLUSTER_TASK_TABLE + " t"
                    + " JOIN dbsyncer_task dt ON dt.ID = t.TASK_ID"
                    + " WHERE t.NODE_ID IS NOT NULL"
                    + " AND t.TASK_TYPE = '" + ConfigConstant.MAPPING + "'"
                    + " AND JSON_UNQUOTE(JSON_EXTRACT(dt.JSON, '$.model')) IN ('"
                    + ModelEnum.INCREMENT.getCode() + "', '" + ModelEnum.FULL_INCREMENT.getCode() + "')"
                    + " GROUP BY t.NODE_ID";
            return toNodeCountMap(storageService.queryList(ExecuteRequest.of(sql)));
        } catch (Exception e) {
            logger.warn("Failed to load an Incremental Task Statistics", e.getMessage());
            return new LinkedHashMap<>();
        }
    }

    private Map<String, Integer> toNodeCountMap(List<Map<String, Object>> rows) {
        Map<String, Integer> result = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(rows)) {
            return result;
        }
        for (Map<String, Object> row : rows) {

            Map<String, Object> normalized = normalizeRow(row);
            String nodeId = String.valueOf(normalized.getOrDefault(ConfigConstant.SCHEDULE_NODE_ID, ""));
            if (StringUtil.isBlank(nodeId)) {
                continue;
            }
            result.put(nodeId, NumberUtil.toInt(String.valueOf(normalized.get("cnt")), 0));
        }
        return result;
    }

    private Map<String, Object> normalizeRow(Map<String, Object> row) {
        Map<String, Object> result = new LinkedCaseInsensitiveMap<>();
        if (row == null) {
            return result;
        }
        row.forEach((key, value) -> {
            String keyStr = key == null ? StringUtil.EMPTY : key;
            String camelKey = keyStr.contains(StringUtil.UNDERLINE) ? UnderlineToCamelUtils.underlineToCamel(keyStr.toLowerCase(), true) : keyStr.toLowerCase();
            result.put(camelKey, value);
        });
        return result;
    }

    private HistoryStackVO mergeTpsSeries(List<ClusterNodeMetricVO> metrics) {
        Map<String, Long> merged = new LinkedHashMap<>();
        List<String> labelOrder = new ArrayList<>();
        for (ClusterNodeMetricVO item : metrics) {
            if (item == null || item.getTpsSeries() == null) {
                continue;
            }
            HistoryStackVO series = item.getTpsSeries();
            List<Object> names = series.getName();
            List<Object> values = series.getValue();
            if (CollectionUtils.isEmpty(names) || CollectionUtils.isEmpty(values)) {
                continue;
            }
            if (labelOrder.isEmpty()) {
                for (Object name : names) {
                    labelOrder.add(String.valueOf(name));
                }
            }
            int size = Math.min(names.size(), values.size());
            for (int i = 0; i < size; i++) {
                String key = String.valueOf(names.get(i));
                long value = values.get(i) instanceof Number ? ((Number) values.get(i)).longValue() : 0L;
                merged.merge(key, value, Long::sum);
            }
        }
        HistoryStackVO result = new HistoryStackVO();
        for (String label : labelOrder) {
            result.addName(label);
            result.addValue(merged.getOrDefault(label, 0L));
        }
        result.setAverage(averageSeries(result.getValue()));
        return result;
    }

    private double averageSeries(List<Object> values) {
        if (CollectionUtils.isEmpty(values)) {
            return 0D;
        }
        long total = 0L;
        for (Object value : values) {
            if (value instanceof Number) {
                total += ((Number) value).longValue();
            }
        }
        return Math.floor((double) total / values.size());
    }
}
