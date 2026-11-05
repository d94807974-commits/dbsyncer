/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.impl;

import org.realtimeetl.common.enums.TaskLevelEnum;
import org.realtimeetl.common.scheduled.ScheduledTaskJob;
import org.realtimeetl.common.scheduled.ScheduledTaskService;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.MetaAggProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.MetaAgg;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.service.ScheduledScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Sync task Meta Day summary: by{@link ScheduledScanService} Break, cluster onlyLeader Implementation.
 *
 * @author wuji
 * @version 1.0.0
 */
@Component
public class MetaAggCollector implements ScheduledScanService, ScheduledTaskJob, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SCHEDULE_KEY = "meta.daily.collector";

    /**
     * 30 minutes
     */
    private static final long PERIOD_MS = 30L * 60L * 1000L;

    /**
     * ReservationsdaysNumberdays）
     */
    private static final int RETAIN_DAYS = 30;

    @Resource
    private ScheduledTaskService scheduledTaskService;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private MetaAggProfile metaAggProfile;

    private final AtomicBoolean collecting = new AtomicBoolean(false);

    private volatile boolean started;

    @Override
    public synchronized void start() {
        if (started) {
            return;
        }
        scheduledTaskService.start(SCHEDULE_KEY, PERIOD_MS, this);
        started = true;
        // StartRun the round now, avoid it.First pageHistory is empty.
        run();
    }

    @Override
    public synchronized void stop() {
        if (!started) {
            return;
        }
        scheduledTaskService.stop(SCHEDULE_KEY);
        started = false;
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void run() {
        if (!collecting.compareAndSet(false, true)) {
            return;
        }
        try {
            collectAndCleanup();
        } catch (Exception e) {
            logger.error("meta daily collect failed: {}", e.getMessage(), e);
        } finally {
            collecting.set(false);
        }
    }

    private void collectAndCleanup() {
        AtomicLong total = new AtomicLong();
        AtomicLong success = new AtomicLong();
        AtomicLong fail = new AtomicLong();
        taskMetaProfile.pageScanMetas(TaskLevelEnum.TASK.getCode(), ConfigConstant.PAGE_SIZE, page -> sumMappingMeta(page, total, success, fail));

        String today = LocalDate.now().format(DateFormatUtil.YYYY_MM_DD);
        MetaAgg metaAgg = new MetaAgg();
        metaAgg.setStatDate(today);
        metaAgg.setTotal(total.get());
        metaAgg.setSuccess(success.get());
        metaAgg.setFail(fail.get());
        metaAggProfile.upsert(metaAgg);

        String cutoff = LocalDate.now().minusDays(RETAIN_DAYS).format(DateFormatUtil.YYYY_MM_DD);
        metaAggProfile.removeBefore(cutoff);
    }

    private void sumMappingMeta(List<Meta> page, AtomicLong total, AtomicLong success, AtomicLong fail) {
        if (CollectionUtils.isEmpty(page)) {
            return;
        }
        for (Meta meta : page) {
            if (meta == null || StringUtil.isBlank(meta.getTaskId())) {
                continue;
            }
            if (mappingProfile.get(meta.getTaskId()) == null) {
                continue;
            }
            if (meta.getTotal() != null) {
                total.addAndGet(meta.getTotal().get());
            }
            if (meta.getSuccess() != null) {
                success.addAndGet(meta.getSuccess().get());
            }
            if (meta.getFail() != null) {
                fail.addAndGet(meta.getFail().get());
            }
        }
    }
}
