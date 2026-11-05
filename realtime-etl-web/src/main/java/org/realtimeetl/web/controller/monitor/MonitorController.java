/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.web.controller.monitor;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.DataSyncService;
import org.realtimeetl.biz.MappingService;
import org.realtimeetl.biz.MonitorService;
import org.realtimeetl.biz.enums.MetricEnum;
import org.realtimeetl.biz.model.AppReportMetric;
import org.realtimeetl.biz.model.MetricResponse;
import org.realtimeetl.biz.model.Sample;
import org.realtimeetl.biz.vo.CpuVO;
import org.realtimeetl.biz.vo.DiskSpaceVO;
import org.realtimeetl.biz.vo.HistoryStackVO;
import org.realtimeetl.biz.vo.MemoryVO;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.manager.impl.PreloadTemplate;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.web.controller.BaseController;
import org.realtimeetl.web.controller.monitor.impl.CpuValueFormatter;
import org.realtimeetl.web.controller.monitor.impl.GBValueFormatter;
import org.realtimeetl.web.controller.monitor.impl.MemoryValueFormatter;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.SystemHealth;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/monitor")
public class MonitorController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final SystemInfo systemInfo = new SystemInfo();
    private final CentralProcessor processor = systemInfo.getHardware().getProcessor();
    private final GlobalMemory globalMemory = systemInfo.getHardware().getMemory();
    private long[] prevTicks = processor.getSystemCpuLoadTicks();
    private static final int COUNT = 60;
    private final CpuVO cpu = new CpuVO();
    private final MemoryVO memory = new MemoryVO();
    private final DiskSpaceVO disk = new DiskSpaceVO();

    @Resource
    private MonitorService monitorService;

    @Resource
    private DataSyncService dataSyncService;

    @Resource
    private MappingService mappingService;

    @Resource
    private PreloadTemplate preloadTemplate;

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

    @RequestMapping("")
    public String index(ModelMap model) {
        initEditionInfo(model);
        return "monitor/list.html";
    }

    @GetMapping("/page/retry")
    public String page(ModelMap model, String taskId, String messageId) {
        model.put("mapping", mappingService.getMapping(taskId));
        model.put("message", dataSyncService.getMessageVo(taskId, messageId));
        return "monitor/retry.html";
    }

    @Scheduled(fixedRate = 5000)
    public void recordHistoryStackMetric() {
        collectCpu();
        collectMemory();
        collectDiskSpace();
    }

    @Scheduled(fixedRate = 30000)
    public void deleteExpiredDataAndLog() {
        if (preloadTemplate.isPreloadCompleted()) {
            monitorService.deleteExpiredDataAndLog();
        }
    }

    @PostMapping("/queryMeta")
    @ResponseBody
    public RestResult queryMeta(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(monitorService.queryMeta(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/queryData")
    @ResponseBody
    public RestResult queryData(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(monitorService.queryData(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/queryLog")
    @ResponseBody
    public RestResult queryLog(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(monitorService.queryLog(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/sync")
    @ResponseBody
    public RestResult sync(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(dataSyncService.sync(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.MAPPING_CLEAR_DATA)
    @PostMapping("/clearData")
    @ResponseBody
    public RestResult clearData(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
            if (StringUtil.isBlank(id)) {
                id = params.get("id");
            }
            String tableGroupId = params.get(ConfigConstant.DATA_TABLE_GROUP_ID);
            if (StringUtil.isBlank(tableGroupId)) {
                tableGroupId = params.get("tableGroupId");
            }
            return RestResult.restSuccess(monitorService.clearData(id, tableGroupId));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * Get Provincial Lines on NeedsSync dataFieldDetails（I don't know.List blob）
     */
    @PostMapping("/getDataDetail")
    @ResponseBody
    public RestResult getDataDetail(String taskId, String messageId) {
        try {
            return RestResult.restSuccess(dataSyncService.getMessageVo(taskId, messageId));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.SYSTEM_INFO, message = "Empty Log")
    @PostMapping("/clearLog")
    @ResponseBody
    public RestResult clearLog() {
        try {
            return RestResult.restSuccess(monitorService.clearLog());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @ResponseBody
    @GetMapping("/metric")
    public RestResult metric() {
        try {
            AppReportMetric reportMetric = monitorService
                    .queryAppMetric(Stream.of(MetricEnum.THREADS_LIVE, MetricEnum.THREADS_PEAK).map(m -> getMetricResponse(m.getCode())).collect(Collectors.toList()));
            reportMetric.setCpu(cpu);
            reportMetric.setMemory(memory);
            reportMetric.setDisk(disk);
            return RestResult.restSuccess(reportMetric);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @ResponseBody
    @GetMapping("/dashboard")
    public RestResult dashboard() {
        try {
            return RestResult.restSuccess(monitorService.queryDashboardMetric());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @ResponseBody
    @PostMapping("/queryActuator")
    public RestResult queryActuator(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(monitorService.queryActuator(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    private void collectCpu() {
        collectStackMetric(MetricEnum.CPU_USAGE, cpu, cpuValueFormatter);
        // Collect instantaneous data
        long[] ticks = processor.getSystemCpuLoadTicks();
        if (prevTicks != null) {
            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long system = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long total = user + nice + system + idle;

            // Prevention of ZeroError：If cares.total Yes0，Description CPU tick No change, useDefault value 0
            if (total == 0) {
                cpu.setUserPercent(BigDecimal.ZERO);
                cpu.setSysPercent(BigDecimal.ZERO);
                cpu.setTotalPercent(BigDecimal.ZERO);
            } else {
                // UserStateC Produce Use（user + nice）
                BigDecimal userCpuPercent = BigDecimal.valueOf(user + nice).divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                cpu.setUserPercent(userCpuPercent);

                // System StateC Produce Use
                BigDecimal systemCpuPercent = BigDecimal.valueOf(system).divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
                cpu.setSysPercent(systemCpuPercent);

                // TotalC Produce Use（NotFreeTime）
                BigDecimal totalCpuPercent = BigDecimal.valueOf(total - idle).divide(BigDecimal.valueOf(total), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                cpu.setTotalPercent(totalCpuPercent);
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
        // SystemMemory
        memory.setSysTotal(gbValueFormatter.formatValue(globalMemory.getTotal()));
        // System used
        memory.setSysUsed(gbValueFormatter.formatValue(globalMemory.getTotal() - globalMemory.getAvailable()));
        memory.setTotalPercent(formatPercent(memory.getSysUsed(), memory.getSysTotal()));
        // JVM Used
        memory.setJvmUsed(gbValueFormatter.formatValue(collectValue(MetricEnum.MEMORY_USED)));
        // JVM TotalMemory
        memory.setJvmTotal(gbValueFormatter.formatValue(collectValue(MetricEnum.MEMORY_MAX)));
    }

    private void collectDiskSpace() {
        SystemHealth health = (SystemHealth) healthEndpoint.health();
        Map<String, HealthComponent> details = health.getComponents();
        Health diskSpace = (Health) details.get("diskSpace");
        Map<String, Object> diskSpaceDetails = diskSpace.getDetails();
        // Total capacity
        disk.setTotal(gbValueFormatter.formatValue(diskSpaceDetails.get("total")));
        // Remaining
        disk.setFree(gbValueFormatter.formatValue(diskSpaceDetails.get("free")));
        // Used
        disk.setUsed(disk.getTotal().subtract(disk.getFree()));
        // Use 100minThat's right.
        disk.setUsedPercent(formatPercent(disk.getUsed(), disk.getTotal()));
    }

    private BigDecimal formatPercent(BigDecimal used, BigDecimal total) {
        if (total == null || total.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (used == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal percent = used.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        return percent.setScale(2, RoundingMode.HALF_UP);
    }

    private MetricResponse getMetricResponse(String code) {
        MetricsEndpoint.MetricResponse metric = metricsEndpoint.metric(code, null);
        if (metric == null) {
            throw new IllegalArgumentException("Indicator not supported =" + code);
        }
        MetricResponse metricResponse = new MetricResponse();
        MetricEnum metricEnum = MetricEnum.getMetric(metric.getName());
        if (metricEnum == null) {
            throw new BizException(String.format("Metric code \"%s\" does not exist.", code));
        }
        metricResponse.setCode(metricEnum.getCode());
        metricResponse.setGroup(metricEnum.getGroup());
        metricResponse.setMetricName(metricEnum.getMetricName());
        if (!CollectionUtils.isEmpty(metric.getMeasurements())) {
            List<Sample> measurements = new ArrayList<>();
            metric.getMeasurements().forEach(s -> measurements.add(new Sample(s.getStatistic().getTagValueRepresentation(), s.getValue())));
            metricResponse.setMeasurements(measurements);
        }
        return metricResponse;
    }

    private void collectStackMetric(MetricEnum metricEnum, HistoryStackVO stackVo, ValueFormatter<Object, Object> formatter) {
        MetricResponse metricResponse = getMetricResponse(metricEnum.getCode());
        List<Sample> measurements = metricResponse.getMeasurements();
        if (!CollectionUtils.isEmpty(measurements)) {
            stackVo.addValue(formatter.formatValue(measurements.get(0).getValue()));
            stackVo.addName(DateFormatUtil.getCurrentTime());
            optimizeStackOverflow(stackVo.getName());
            optimizeStackOverflow(stackVo.getValue());
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
        if (stack.size() >= COUNT) {
            stack.remove(0);
        }
    }

}