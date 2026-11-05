/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.flush;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.UUIDUtil;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.flush.impl.GeneralBufferActuator;
import org.realtimeetl.parser.flush.impl.TableGroupBufferActuator;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.model.WriterRequest;
import org.realtimeetl.sdk.enums.ChangedEventTypeEnum;
import org.realtimeetl.sdk.listener.ChangedEvent;
import org.realtimeetl.sdk.model.BufferActuatorMetric;
import org.realtimeetl.sdk.spi.BufferActuatorRouterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * IncrementalCache Executioner Route Subcategory:TotalUsetrace、DDL Incoming!andMonitorQuick-shot assembly.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-08-12
 */
public abstract class AbstractBufferActuatorRouter implements BufferActuatorRouterService, DisposableBean {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    protected SystemConfigProfile systemConfigProfile;

    @Resource
    protected GeneralBufferActuator generalBufferActuator;

    /**
     * PrintIncremental trace。
     *
     * @param event ChangeEvent
     */
    protected void printTraceInfo(ChangedEvent event) {
        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        if (systemConfig != null && systemConfig.isEnablePrintTraceInfo()) {
            event.setTraceId(UUIDUtil.getUUID().toLowerCase());
            logger.info("traceId:{}, tableName:{}, event:{}, offset:{}, row:{}", event.getTraceId(), event.getSourceTableName(),
                    event.getEvent(), JsonUtil.objToJson(event.getChangedOffset()), event.getChangedRow());
        }
    }

    /**
     * (a) The delivery to had been made to 41.DDL I'll wait.Objective CombinedExecut MOUFree（Queue empty andNone in-flight process）We'll be back in line.
     * <p>Waiting for the break mark to be restored and still trying to join, avoidDDL Lost.
     *
     * @param actuator Objective CombinedExecut MOU
     * @param event    ChangeEvent
     */
    protected void offer(AbstractBufferActuator actuator, ChangedEvent event) {
        if (ChangedEventTypeEnum.isDDL(event.getType())) {
            WriterRequest request = new WriterRequest(event);
            while (actuator.isRunning(request)) {
                if (actuator.isIdle()) {
                    actuator.offer(request);
                    return;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        actuator.offer(new WriterRequest(event));
    }

    /**
     * Compile Single ExecutorMonitorSc 49.
     *
     * @param name     Presentation Name
     * @param actuator Execut MOU
     * @return Photo
     */
    protected BufferActuatorMetric toMetric(String taskId, String name, AbstractBufferActuator actuator) {
        BufferActuatorMetric metric = new BufferActuatorMetric();
        metric.setTaskId(taskId);
        metric.setName(name);
        if (actuator == null) {
            return metric;
        }
        if (actuator.getQueue() != null) {
            metric.setQueueSize(actuator.getQueue().size());
        }
        metric.setQueueCapacity(actuator.getQueueCapacity());
        Executor executor = actuator.getExecutor();
        if (executor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolExecutor pool = ((ThreadPoolTaskExecutor) executor).getThreadPoolExecutor();
            metric.setActiveCount(pool.getActiveCount());
            metric.setMaxPoolSize(pool.getMaximumPoolSize());
            metric.setCompletedTaskCount(pool.getCompletedTaskCount());
        }
        return metric;
    }

    /**
     * StopTable/pipe implementer.
     *
     * @param actuator Execut MOU
     */
    protected void stopActuator(TableGroupBufferActuator actuator) {
        if (actuator != null) {
            actuator.stop();
        }
    }
}
