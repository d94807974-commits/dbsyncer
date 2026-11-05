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
package org.realtimeetl.web.controller.cluster;

import org.realtimeetl.biz.MonitorService;
import org.realtimeetl.biz.PluginService;
import org.realtimeetl.biz.enums.MetricEnum;
import org.realtimeetl.biz.impl.MetricReporter;
import org.realtimeetl.biz.model.AppReportMetric;
import org.realtimeetl.biz.model.MetricResponse;
import org.realtimeetl.biz.model.Sample;
import org.realtimeetl.biz.vo.ClusterNodeMetricVO;
import org.realtimeetl.biz.vo.CpuVO;
import org.realtimeetl.biz.vo.DiskSpaceVO;
import org.realtimeetl.biz.vo.MemoryVO;
import org.realtimeetl.biz.vo.TpsVO;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.common.util.NetUtil;
import org.realtimeetl.sdk.spi.ClusterService;
import org.realtimeetl.web.controller.monitor.ValueFormatter;
import org.realtimeetl.web.controller.monitor.impl.CpuValueFormatter;
import org.realtimeetl.web.controller.monitor.impl.GBValueFormatter;
import org.realtimeetl.web.controller.monitor.impl.MemoryValueFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * HS/Application of indicators for cluster collectionNodesInterrogationandAggregation.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-20
 */
@Component
public class LocalNodeMetricProvider {

    private static final int HISTORY_COUNT = 60;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SystemInfo systemInfo = new SystemInfo();
    private final CentralProcessor processor = systemInfo.getHardware().getProcessor();
    private final GlobalMemory globalMemory = systemInfo.getHardware().getMemory();
    private final CpuVO cpu = new CpuVO();
    private final MemoryVO memory = new MemoryVO();
    private final DiskSpaceVO disk = new DiskSpaceVO();
    private long[] prevTicks = processor.getSystemCpuLoadTicks();
    /**
     * and CPU/MemorySame sample to avoid impregnation.ThreadsPut it on.ThreadsLift.
     */
    private volatile long threadLive;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Resource
    private MonitorService monitorService;

    @Resource
    private MetricReporter metricReporter;

    @Resource
    private MetricsEndpoint metricsEndpoint;

    @Resource
    private HealthEndpoint healthEndpoint;

    @Resource
    private CpuValueFormatter cpuValueFormatter;

    @Resource
    private MemoryValueFormatter memoryValueFormatter;

    @Resource
    private GBValueFormatter gbValueFormatter;

    @Resource
    private ClusterService clusterService;

    @Resource
    private PluginService pluginService;

    @PostConstruct
    public void init() {
        refreshSystemMetrics();
    }

    /**
     * Scatter of current indicatorLoginInterrogationandAggregationTotalUse it.
     *
     * @return Indicators
     */
    public ClusterNodeMetricVO snapshot() {
        ClusterNodeMetricVO vo = new ClusterNodeMetricVO();
        vo.setNodeId(clusterService.getLocalNodeId());
        vo.setLocal(true);
        vo.setReachable(true);
        vo.setCpuPercent(cpu.getTotalPercent() == null ? BigDecimal.ZERO : cpu.getTotalPercent());
        vo.setMemoryUsed(memory.getSysUsed());
        vo.setMemoryTotal(memory.getSysTotal());
        vo.setDiskUsed(disk.getUsed());
        vo.setDiskTotal(disk.getTotal());
        vo.setThreadLive(threadLive);
        vo.setPluginSize(pluginService.listPlugins().size());
        try {
            AppReportMetric app = monitorService.queryAppMetric(Stream
                    .of(MetricEnum.THREADS_LIVE, MetricEnum.THREADS_PEAK)
                    .map(m -> getMetricResponse(m.getCode()))
                    .collect(Collectors.toList()));
            if (app != null) {
                vo.setQueueUp(app.getQueueUp());
                vo.setStorageQueueUp(app.getStorageQueueUp());
            }
            TpsVO tpsSeries = metricReporter.getOneMinBufferActuatorRate();
            vo.setTps(metricReporter.getCurrentBufferActuatorTps());
            vo.setTpsSeries(tpsSeries);
        } catch (Exception e) {
            logger.warn("Failed to collect application indicator: {", e.getMessage());
        }
        return vo;
    }

    @Scheduled(fixedRate = 5000)
    public void refreshSystemMetrics() {
        collectCpu();
        collectMemory();
        collectDiskSpace();
        threadLive = readThreadLive();
    }

    private long readThreadLive() {
        try {
            MetricsEndpoint.MetricResponse metric = metricsEndpoint.metric(MetricEnum.THREADS_LIVE.getCode(), null);
            if (metric == null || CollectionUtils.isEmpty(metric.getMeasurements())) {
                return 0L;
            }
            Double value = metric.getMeasurements().get(0).getValue();
            return value == null ? 0L : value.longValue();
        } catch (Exception e) {
            return 0L;
        }
    }

    private void collectCpu() {
        collectStackMetric(MetricEnum.CPU_USAGE, cpu, cpuValueFormatter);
        long[] ticks = processor.getSystemCpuLoadTicks();
        if (prevTicks != null) {
            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long system = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long total = user + nice + system + idle;
            if (total == 0) {
                cpu.setUserPercent(BigDecimal.ZERO);
                cpu.setSysPercent(BigDecimal.ZERO);
                cpu.setTotalPercent(BigDecimal.ZERO);
            } else {
                cpu.setUserPercent(BigDecimal.valueOf(user + nice)
                        .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP));
                cpu.setSysPercent(BigDecimal.valueOf(system)
                        .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP));
                cpu.setTotalPercent(BigDecimal.valueOf(total - idle)
                        .divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP));
            }
            prevTicks = ticks;
        } else {
            cpu.setUserPercent(BigDecimal.ZERO);
            cpu.setSysPercent(BigDecimal.ZERO);
            cpu.setTotalPercent(BigDecimal.ZERO);
        }
    }

    private void collectMemory() {
        collectStackMetric(MetricEnum.MEMORY_USED, memory, memoryValueFormatter);
        memory.setSysTotal(gbValueFormatter.formatValue(globalMemory.getTotal()));
        memory.setSysUsed(gbValueFormatter.formatValue(globalMemory.getTotal() - globalMemory.getAvailable()));
        memory.setTotalPercent(formatPercent(memory.getSysUsed(), memory.getSysTotal()));
        memory.setJvmUsed(gbValueFormatter.formatValue(collectValue(MetricEnum.MEMORY_USED)));
        memory.setJvmTotal(gbValueFormatter.formatValue(collectValue(MetricEnum.MEMORY_MAX)));
    }

    private void collectDiskSpace() {
        try {
            SystemHealth health = (SystemHealth) healthEndpoint.health();
            Map<String, HealthComponent> details = health.getComponents();
            Health diskSpace = (Health) details.get("diskSpace");
            if (diskSpace == null) {
                return;
            }
            Map<String, Object> diskSpaceDetails = diskSpace.getDetails();
            disk.setTotal(gbValueFormatter.formatValue(diskSpaceDetails.get("total")));
            disk.setFree(gbValueFormatter.formatValue(diskSpaceDetails.get("free")));
            disk.setUsed(disk.getTotal().subtract(disk.getFree()));
            disk.setUsedPercent(formatPercent(disk.getUsed(), disk.getTotal()));
        } catch (Exception e) {
            logger.warn("Failed to collect disk indicator: {", e.getMessage());
        }
    }

    private BigDecimal formatPercent(BigDecimal used, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (used == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return used.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private MetricResponse getMetricResponse(String code) {
        MetricsEndpoint.MetricResponse metric = metricsEndpoint.metric(code, null);
        if (metric == null) {
            throw new IllegalArgumentException("Indicator not supported =" + code);
        }
        MetricResponse metricResponse = new MetricResponse();
        MetricEnum metricEnum = MetricEnum.getMetric(metric.getName());
        if (metricEnum == null) {
            throw new IllegalArgumentException(String.format("Metric code \"%s\" does not exist.", code));
        }
        metricResponse.setCode(metricEnum.getCode());
        metricResponse.setGroup(metricEnum.getGroup());
        metricResponse.setMetricName(metricEnum.getMetricName());
        if (!CollectionUtils.isEmpty(metric.getMeasurements())) {
            List<Sample> measurements = new ArrayList<>();
            metric.getMeasurements().forEach(s ->
                    measurements.add(new Sample(s.getStatistic().getTagValueRepresentation(), s.getValue())));
            metricResponse.setMeasurements(measurements);
        }
        return metricResponse;
    }

    private void collectStackMetric(MetricEnum metricEnum, org.realtimeetl.biz.vo.HistoryStackVO stackVo,
                                    ValueFormatter<Object, Object> formatter) {
        try {
            MetricResponse metricResponse = getMetricResponse(metricEnum.getCode());
            List<Sample> measurements = metricResponse.getMeasurements();
            if (!CollectionUtils.isEmpty(measurements)) {
                stackVo.addValue(formatter.formatValue(measurements.get(0).getValue()));
                stackVo.addName(DateFormatUtil.getCurrentTime());
                optimizeStackOverflow(stackVo.getName());
                optimizeStackOverflow(stackVo.getValue());
            }
        } catch (Exception e) {
            logger.debug("Collect stack Chairmanship failed", metricEnum, e.getMessage());
        }
    }

    private Object collectValue(MetricEnum metricEnum) {
        MetricResponse metricResponse = getMetricResponse(metricEnum.getCode());
        List<Sample> measurements = metricResponse.getMeasurements();
        if (!CollectionUtils.isEmpty(measurements)) {
            return measurements.get(0).getValue();
        }
        return 0;
    }

    private void optimizeStackOverflow(List<Object> stack) {
        if (stack.size() >= HISTORY_COUNT) {
            stack.remove(0);
        }
    }

    /**
     * Ben.NodesUnder an agreement.Web Root Address (Following){@code server.ssl.enabled} and {@code server.servlet.context-path}）。
     *
     * @param ip       IP
     * @param httpPort Port
     * @return Likehttp(s)://ip:port[/context-path]；Space at Illegal
     */
    public String buildHttpUrl(String ip, int httpPort) {
        return NetUtil.buildWebRootUrl(ip, httpPort, sslEnabled, contextPath);
    }
}
