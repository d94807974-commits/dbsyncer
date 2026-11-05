/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.ConnectorConfigService;
import org.realtimeetl.biz.MonitorService;
import org.realtimeetl.biz.SystemConfigService;
import org.realtimeetl.biz.enums.BufferActuatorMetricEnum;
import org.realtimeetl.biz.enums.MetricEnum;
import org.realtimeetl.biz.metric.MetricDetailFormatter;
import org.realtimeetl.biz.metric.MetricGroupProcessor;
import org.realtimeetl.biz.metric.impl.DoubleRoundMetricDetailFormatter;
import org.realtimeetl.biz.metric.impl.ValueMetricDetailFormatter;
import org.realtimeetl.biz.model.AppReportMetric;
import org.realtimeetl.biz.model.DashboardMetric;
import org.realtimeetl.biz.model.MetricResponse;
import org.realtimeetl.biz.vo.DataVO;
import org.realtimeetl.biz.vo.LogVO;
import org.realtimeetl.biz.vo.MetaVO;
import org.realtimeetl.biz.vo.MetricResponseVO;
import org.realtimeetl.common.enums.TaskLevelEnum;
import org.realtimeetl.common.enums.YesNoEnum;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.scheduled.ScheduledTaskJob;
import org.realtimeetl.common.scheduled.ScheduledTaskService;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.plugin.model.ConnectorOfflineContent;
import org.realtimeetl.plugin.model.MappingErrorContent;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.FilterEnum;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.BooleanFilter;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.filter.impl.LongFilter;
import org.realtimeetl.sdk.service.ScheduledScanService;
import org.realtimeetl.sdk.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020-04-27 10:20
 */
@Service
public class MonitorServiceImpl extends BaseServiceImpl implements MonitorService, ScheduledTaskJob, ScheduledScanService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private MetricReporter metricReporter;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private ScheduledTaskService scheduledTaskService;

    @Resource
    private StorageService storageService;

    @Resource
    private ConnectorConfigService connectorConfigService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private MetricGroupProcessor metricGroupProcessor;

    private static final String CONNECTOR_MONITOR_KEY = "monitor-notice-task";

    private volatile boolean started;

    private final Map<String, MetricDetailFormatter> metricMap = new ConcurrentHashMap<>();

    private MetricResponse systemInfo;

    private LocalDateTime delayTime = LocalDateTime.now();

    private final AtomicLong LAST_EXECUTE_TIME = new AtomicLong(System.currentTimeMillis());

    private static final long FAIL_DETAIL_LOOK_BACK_MS = 3 * 60 * 1000L;

    @PostConstruct
    private void init() {
        metricMap.putIfAbsent(BufferActuatorMetricEnum.GENERAL.getCode(), new ValueMetricDetailFormatter());
        metricMap.putIfAbsent(BufferActuatorMetricEnum.STORAGE.getCode(), new ValueMetricDetailFormatter());
        metricMap.putIfAbsent(MetricEnum.THREADS_LIVE.getCode(), new DoubleRoundMetricDetailFormatter());
        metricMap.putIfAbsent(MetricEnum.THREADS_PEAK.getCode(), new DoubleRoundMetricDetailFormatter());
        metricMap.putIfAbsent(MetricEnum.SYSTEM_ENV.getCode(), vo -> {
            // ActionsSystem
            String osName = System.getProperty("os.name");
            // Structure
            vo.setDetail(String.format("%s %s %s", osName, System.getProperty("os.arch"), System.getProperty("os.version")));
        });
        systemInfo = new MetricResponse();
        systemInfo.setCode(MetricEnum.SYSTEM_ENV.getCode());
        systemInfo.setGroup(MetricEnum.SYSTEM_ENV.getGroup());
    }

    @Override
    public Paging<MetaVO> queryMeta(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 50);
        String searchKey = params.get("searchKey");
        // PressTasksminPage，Avoid SweepingFull Meta（Including verification/relocationSync task）
        Paging<Mapping> paging = mappingProfile.query(pageNum, pageSize, searchKey, null);
        Paging<MetaVO> result = new Paging<>(pageNum, pageSize);
        if (paging == null) {
            return result;
        }
        result.setTotal(paging.getTotal());
        if (CollectionUtils.isEmpty(paging.getData())) {
            return result;
        }
        List<MetaVO> rows = new ArrayList<>(paging.getData().size());
        for (Mapping mapping : paging.getData()) {
            if (mapping == null) {
                continue;
            }
            Meta meta = taskMetaProfile.getMeta(mapping.getId());
            if (meta == null) {
                continue;
            }
            MetaVO vo = convertMeta2Vo(meta);
            if (vo != null) {
                rows.add(vo);
            }
        }
        result.setData(rows);
        return result;
    }

    @Override
    public Paging queryData(Map<String, String> params) {
        String id = params.get(ConfigConstant.CONFIG_MODEL_ID);
        Assert.notNull(id, "The taskId is null.");
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String error = params.get(ConfigConstant.DATA_ERROR);
        String status = params.get("status");
        String tableGroupId = params.get(ConfigConstant.DATA_TABLE_GROUP_ID);
        if (StringUtil.isBlank(tableGroupId)) {
            tableGroupId = params.get("tableGroupId");
        }

        Paging paging = queryData(id, pageNum, pageSize, error, status, tableGroupId);
        List<Map> data = (List<Map>) paging.getData();
        List<DataVO> list = new ArrayList<>();
        for (Map row : data) {
            try {
                // StreamliningminTable Map toDataVO：TYPE→event, IS_SUCCESS→success, TARGET_TABLE→targetTableName
                row.put("event", row.get(ConfigConstant.CONFIG_MODEL_TYPE));
                row.put("success", row.get(ConfigConstant.DETAIL_IS_SUCCESS));
                row.put(ConfigConstant.DATA_TARGET_TABLE_NAME, row.get(ConfigConstant.DETAIL_TARGET_TABLE));
                DataVO dataVo = convert2Vo(row, DataVO.class);
                // ListI don't know.blob，FieldDetailsAs required
                dataVo.setJson(null);
                list.add(dataVo);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }
        paging.setData(list);
        return paging;
    }

    @Override
    public String clearData(String id, String tableGroupId) {
        Assert.hasText(id, "Sync task does not exist.");
        Mapping mapping = mappingProfile.get(id);
        Assert.notNull(mapping, "Sync task does not exist.");

        Query query = new Query();
        query.setType(StorageEnum.TASK_DETAIL);
        query.setTaskId(id);
        query.addFilter(ConfigConstant.DATA_TABLE_GROUP_ID, tableGroupId);
        storageService.delete(query);
        return "Successfully emptied current table sync data";
    }

    @Override
    public Paging queryLog(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String json = params.get(ConfigConstant.CONFIG_MODEL_JSON);
        Query query = new Query(pageNum, pageSize);
        if (StringUtil.isNotBlank(json)) {
            query.addFilter(ConfigConstant.CONFIG_MODEL_JSON, json, true);
        }
        query.setType(StorageEnum.LOG);
        return queryAndConvertLog(query);
    }

    @Override
    public Paging queryLoginLog(Map<String, String> params) {
        Query query = buildLogQuery(params);
        query.addFilter(ConfigConstant.CONFIG_MODEL_TYPE, FilterEnum.IN,
                LogType.LoginLog.SUCCESS.getType() + StringUtil.COMMA + LogType.LoginLog.FAIL.getType());
        return queryAndConvertLog(query);
    }

    @Override
    public Paging queryOperLog(Map<String, String> params) {
        Query query = buildLogQuery(params);
        String module = params.get("module");
        String moduleTypes = LogType.joinTypesByModuleName(module);
        if (StringUtil.isNotBlank(moduleTypes)) {
            query.addFilter(ConfigConstant.CONFIG_MODEL_TYPE, FilterEnum.IN, moduleTypes);
        } else {
            String type = params.get(ConfigConstant.CONFIG_MODEL_TYPE);
            if (StringUtil.isNotBlank(type)) {
                query.addFilter(ConfigConstant.CONFIG_MODEL_TYPE, type);
            } else {
                query.addFilter(ConfigConstant.CONFIG_MODEL_TYPE, FilterEnum.NOT_EQUAL, LogType.LoginLog.SUCCESS.getType());
                query.addFilter(ConfigConstant.CONFIG_MODEL_TYPE, FilterEnum.NOT_EQUAL, LogType.LoginLog.FAIL.getType());
            }
        }
        return queryAndConvertLog(query);
    }

    @Override
    public LogVO getLogDetail(String id) {
        Assert.hasText(id, "Log ID cannot be empty");
        Query query = new Query(1, 1);
        query.setType(StorageEnum.LOG);
        query.addFilter(ConfigConstant.CONFIG_MODEL_ID, id);
        Map row = storageService.queryObject(query);
        Assert.notNull(row, "Log does not exist");
        return enrichLogVo(convert2Vo(row, LogVO.class));
    }

    @Override
    public String clearLog() {
        storageService.clear(StorageEnum.LOG, null);
        return "Clear Log Success";
    }

    private Query buildLogQuery(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        Query query = new Query(pageNum, pageSize);
        query.setType(StorageEnum.LOG);
        String username = params.get(ConfigConstant.LOG_USERNAME);
        if (StringUtil.isNotBlank(username)) {
            query.addFilter(ConfigConstant.LOG_USERNAME, username, true);
        }
        String ip = params.get(ConfigConstant.LOG_IP);
        if (StringUtil.isNotBlank(ip)) {
            query.addFilter(ConfigConstant.LOG_IP, ip, true);
        }
        // Keyword：Fuzzy MatchLogContentandPerformance monitorLogQuery is consistent)
        String json = params.get(ConfigConstant.CONFIG_MODEL_JSON);
        if (StringUtil.isNotBlank(json)) {
            query.addFilter(ConfigConstant.CONFIG_MODEL_JSON, json, true);
        }
        String status = params.get(ConfigConstant.LOG_STATUS);
        if (StringUtil.isNotBlank(status) && NumberUtil.isCreatable(status)) {
            query.addFilter(ConfigConstant.LOG_STATUS, NumberUtil.toInt(status));
        }
        long beginTime = NumberUtil.toLong(params.get("beginTime"), 0L);
        if (beginTime > 0) {
            query.addFilter(ConfigConstant.CONFIG_MODEL_CREATE_TIME, FilterEnum.GT_AND_EQUAL, beginTime);
        }
        long endTime = NumberUtil.toLong(params.get("endTime"), 0L);
        if (endTime > 0) {
            query.addFilter(ConfigConstant.CONFIG_MODEL_CREATE_TIME, FilterEnum.LT_AND_EQUAL, endTime);
        }
        return query;
    }

    private Paging queryAndConvertLog(Query query) {
        Paging paging = storageService.query(query);
        List<Map> data = (List<Map>) paging.getData();
        paging.setData(data.stream().map(m -> enrichLogVo(convert2Vo(m, LogVO.class))).collect(Collectors.toList()));
        return paging;
    }

    private LogVO enrichLogVo(LogVO vo) {
        if (vo == null) {
            return null;
        }
        vo.setTypeName(LogType.getNameByType(vo.getType()));
        String json = vo.getJson();
        if (StringUtil.isBlank(json)) {
            vo.setMessage(StringUtil.EMPTY);
            return vo;
        }
        String trimmed = json.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                Map<String, Object> detail = JsonUtil.parseMap(trimmed);
                if (detail != null) {
                    Object message = detail.get("message");
                    vo.setMessage(message == null ? json : String.valueOf(message));
                    Object requestUri = detail.get("requestUri");
                    if (requestUri != null) {
                        vo.setRequestUri(String.valueOf(requestUri));
                    }
                    vo.setRequestParam(detail.get("requestParam"));
                    vo.setResponseParam(detail.get("responseParam"));
                    Object costMs = detail.get("costMs");
                    if (costMs != null && NumberUtil.isCreatable(String.valueOf(costMs))) {
                        vo.setCostMs(NumberUtil.toLong(String.valueOf(costMs)));
                    }
                    return vo;
                }
            } catch (Exception ignore) {
                // Old plain text or illegalJSON，Show by Original
            }
        }
        vo.setMessage(json);
        return vo;
    }

    @Override
    public void deleteExpiredDataAndLog() {
        deleteExpiredData();
        deleteExpiredLog();
        deleteExpiredFileLog();
    }

    @Override
    public AppReportMetric queryAppMetric(List<MetricResponse> metrics) {
        AppReportMetric app = metricReporter.getAppReportMetric();
        metrics.add(systemInfo);
        // System indicators
        metrics.addAll(metricReporter.getMetricInfo());
        // MergeGroupShow
        app.setMetrics(metricGroupProcessor.process(metricResponseToVo(metrics)));
        return app;
    }

    @Override
    public DashboardMetric queryDashboardMetric() {
        return metricReporter.getMappingReportMetric();
    }

    @Override
    public Paging<MetricResponse> queryActuator(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        String taskId = params.get(ConfigConstant.CONFIG_MODEL_ID);
        String name = params.get(ConfigConstant.CONFIG_MODEL_NAME);
        return metricReporter.queryActuator(taskId, name, pageNum, pageSize);
    }

    @Override
    public void run() {
        // Early warning: onlyTasksAdministrationMeta，minPagescanning;eachSync taskSend separately, easy to pressTasksOverwrite @
        long endTime = System.currentTimeMillis();
        // Look back down and avoid moving forward.LibraryLate 275-50.
        long fromTime = Math.max(0L, LAST_EXECUTE_TIME.get() - FAIL_DETAIL_LOOK_BACK_MS);
        taskMetaProfile.pageScanMetas(TaskLevelEnum.TASK.getCode(), ConfigConstant.PAGE_SIZE, page -> {
            for (Meta meta : page) {
                Mapping mapping = mappingProfile.get(meta.getTaskId());
                if (mapping == null || !StringUtil.equals(ConfigConstant.MAPPING, mapping.getType())) {
                    continue;
                }
                long failCount = meta.getFail() != null ? meta.getFail().get() : 0L;
                if (failCount <= 0) {
                    continue;
                }
                Query query = new Query(1, 1);
                query.setType(StorageEnum.TASK_DETAIL);
                query.setTaskId(meta.getTaskId());
                query.addFilter(ConfigConstant.CONFIG_MODEL_CREATE_TIME, FilterEnum.GT_AND_EQUAL, fromTime);
                query.addFilter(ConfigConstant.CONFIG_MODEL_CREATE_TIME, FilterEnum.LT_AND_EQUAL, endTime);
                query.setQueryTotal(true);
                query.addFilter(ConfigConstant.DETAIL_IS_SUCCESS, 0);
                Paging queryTemp = storageService.query(query);
                if (queryTemp.getTotal() <= 0) {
                    continue;
                }
                MappingErrorContent content = new MappingErrorContent();
                content.setTitle("Sync failed");
                content.setResourceType(ConfigConstant.MAPPING);
                content.setResourceId(mapping.getId());
                writeMappingReport(meta, content);
                if (!CollectionUtils.isEmpty(content.getErrorItems())) {
                    sendNotifyMessage(content);
                }
            }
        });
        //ResetLast time.Time
        LAST_EXECUTE_TIME.set(endTime);
        // CollectionConnect OfflineStatus
        collectConnectorOffline();
    }

    private void collectConnectorOffline() {
        // Prevention of the first timeStart，ConnectorStatusNot yet.Refresh，Default returnYesOfflineStatus，Prevention of miscalculation
        if (LocalDateTime.now().minusMinutes(1).isAfter(delayTime)) {
            // CollectionConnect OfflineStatus
            ConnectorOfflineContent content = new ConnectorOfflineContent();
            connectorConfigService.getConnectorAll().forEach(connector -> {
                if (connector.getStatus() != YesNoEnum.YES.getCode()) {
                    ConnectorOfflineContent.ErrorItem item = new ConnectorOfflineContent.ErrorItem();
                    item.setName(connector.getName());
                    item.setType(connector.getConfig().getConnectorType());
                    item.setUrl(connector.getConfig().getUrl());
                    content.addErrorItem(item);
                }
            });
            if (!CollectionUtils.isEmpty(content.getErrorItems())) {
                content.setTitle("Connect Offline");
                sendNotifyMessage(content);
            }
        }
        delayTime = LocalDateTime.now();
    }

    private void writeMappingReport(Meta meta, MappingErrorContent content) {
        Mapping mapping = mappingProfile.get(meta.getTaskId());
        if (null != mapping) {
            ModelEnum modelEnum = ModelEnum.getModelEnum(mapping.getModel());
            MappingErrorContent.ErrorItem item = new MappingErrorContent.ErrorItem();
            item.setName(mapping.getName());
            item.setModel(modelEnum);
            item.setFail(meta.getFail());
            item.setSuccess(meta.getSuccess());
            if (ModelEnum.isFull(modelEnum.getCode())) {
                item.setTotal(meta.getTotal());
            }
            content.addErrorItem(item);
        }
    }

    private Paging queryData(String taskId, int pageNum, int pageSize, String error, String status, String tableGroupId) {
        Query query = new Query(pageNum, pageSize);
        // ListNo, no, no.DATA blob，DetailsHit the window again.id Pull manipulate
        Set<String> selectFields = new HashSet<>();
        selectFields.add(ConfigConstant.CONFIG_MODEL_ID);
        selectFields.add(ConfigConstant.DATA_TABLE_GROUP_ID);
        selectFields.add(ConfigConstant.CONFIG_MODEL_TYPE);
        selectFields.add(ConfigConstant.DETAIL_TARGET_TABLE);
        selectFields.add(ConfigConstant.DETAIL_IS_SUCCESS);
        selectFields.add(ConfigConstant.DATA_ERROR);
        selectFields.add(ConfigConstant.CONFIG_MODEL_CREATE_TIME);
        selectFields.add(ConfigConstant.CONFIG_MODEL_UPDATE_TIME);
        query.setSelectFlied(selectFields);

        query.setTaskId(taskId);
        // QuestionOrganisation
        if (StringUtil.isNotBlank(error)) {
            query.addFilter(ConfigConstant.DATA_ERROR, error, true);
        }
        // Query DataStatus
        if (StringUtil.isNotBlank(status) && !StringUtil.equals("-1", status)) {
            query.addFilter(ConfigConstant.DETAIL_IS_SUCCESS, NumberUtil.toInt(status));
        }
        if (StringUtil.isNotBlank(tableGroupId)) {
            query.addFilter(ConfigConstant.DATA_TABLE_GROUP_ID, tableGroupId);
        }
        query.setType(StorageEnum.TASK_DETAIL);
        return storageService.query(query);
    }

    private void deleteExpiredData() {
        // DetailsminTable: IndividualTasksminTable byExpiration time3, 2, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 3, 3, 4, 3, 3, 3, 4, 4, favoured, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 10, 3
        int expireDataDays = systemConfigService.getSystemConfig().getExpireDataDays();
        long expiredTime = Timestamp.valueOf(LocalDateTime.now().minusDays(expireDataDays)).getTime();
        taskMetaProfile.pageScanMetas(TaskLevelEnum.TASK.getCode(), ConfigConstant.PAGE_SIZE, page -> {
            for (Meta meta : page) {
                Mapping mapping = mappingProfile.get(meta.getTaskId());
                if (mapping == null || !StringUtil.equals(ConfigConstant.MAPPING, mapping.getType())) {
                    continue;
                }
                Query query = new Query();
                query.setType(StorageEnum.TASK_DETAIL);
                query.setTaskId(meta.getTaskId());
                query.setBooleanFilter(new BooleanFilter().add(new LongFilter(ConfigConstant.CONFIG_MODEL_CREATE_TIME, FilterEnum.LT, expiredTime)));
                storageService.delete(query);
            }
        });
    }

    private void deleteExpiredLog() {
        Query query = new Query();
        query.setType(StorageEnum.LOG);
        int expireLogDays = systemConfigService.getSystemConfig().getExpireLogDays();
        long expiredTime = Timestamp.valueOf(LocalDateTime.now().minusDays(expireLogDays)).getTime();
        LongFilter expiredFilter = new LongFilter(ConfigConstant.CONFIG_MODEL_CREATE_TIME, FilterEnum.LT, expiredTime);
        query.setBooleanFilter(new BooleanFilter().add(expiredFilter));
        storageService.delete(query);
    }

    /**
     * DeleteExpiredLogDocumentation（logsArchive under Directory.logDocumentation）
     */
    private void deleteExpiredFileLog() {
        String logHome = System.getProperty("LOG_HOME", "logs");
        File logDir = new File(logHome);
        if (!logDir.exists() || !logDir.isDirectory()) {
            return;
        }
        int expireFileLogDays = systemConfigService.getSystemConfig().getExpireFileLogDays();
        long expireMillis = System.currentTimeMillis() - expireFileLogDays * 24L * 60L * 60L * 1000L;
        deleteExpiredLogFile(logDir, expireFileLogDays, expireMillis);
    }

    private void deleteExpiredLogFile(File dir, int expireFileLogDays, long expireMillis) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                deleteExpiredLogFile(file, expireFileLogDays, expireMillis);
                // If the subdirectories are empty,DeleteEmpty directory
                File[] children = file.listFiles();
                if (children != null && children.length == 0) {
                    file.delete();
                }
            } else if (file.getName().endsWith(".log") && file.lastModified() < expireMillis) {
                if (file.delete()) {
                    logger.info("Delete the expired {day log file: {", expireFileLogDays, file.getAbsolutePath());
                }
            }
        }
    }

    private MetaVO convertMeta2Vo(Meta meta) {
        Mapping mapping = mappingProfile.get(meta.getTaskId());
        // UnsyncDriver（Check/transfer, etc.) Skip
        if (mapping == null || !StringUtil.equals(ConfigConstant.MAPPING, mapping.getType())
                || StringUtil.isBlank(mapping.getModel())) {
            return null;
        }
        ModelEnum modelEnum = ModelEnum.getModelEnum(mapping.getModel());
        MetaVO metaVo = new MetaVO(modelEnum.getName(), mapping.getName());
        BeanUtils.copyProperties(meta, metaVo);
        return metaVo;
    }

    private <T> T convert2Vo(Map map, Class<T> clazz) {
        return JsonUtil.jsonToObj(JsonUtil.objToJson(map), clazz);
    }

    private List<MetricResponseVO> metricResponseToVo(Collection<MetricResponse> metrics) {
        return metrics.stream().map(metric -> {
            MetricResponseVO vo = new MetricResponseVO();
            vo.setCode(metric.getCode());
            vo.setGroup(metric.getGroup());
            vo.setMetricName(metric.getMetricName());
            vo.setMeasurements(metric.getMeasurements());
            metricMap.computeIfPresent(vo.getCode(), (k, mdf) -> {
                mdf.format(vo);
                return mdf;
            });
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public synchronized void start() {
        if (started) {
            return;
        }
        // Interval10minutesEarly warning
        scheduledTaskService.start(CONNECTOR_MONITOR_KEY, "0 */10 * * * ?", this);
        started = true;
    }

    @Override
    public synchronized void stop() {
        if (!started) {
            return;
        }
        scheduledTaskService.stop(CONNECTOR_MONITOR_KEY);
        started = false;
        logger.info("connector health scheduler stopped");
    }
}