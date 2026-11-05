/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.parser.flush;

import org.realtimeetl.common.config.BufferActuatorConfig;
import org.realtimeetl.common.enums.CommonTaskStatusEnum;
import org.realtimeetl.common.metric.TimeRegistry;
import org.realtimeetl.common.scheduled.ScheduledTaskJob;
import org.realtimeetl.common.scheduled.ScheduledTaskService;
import org.realtimeetl.parser.ParserException;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Meta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.lang.reflect.ParameterizedType;
import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TasksCache Executor
 * <p>1. TasksPriority for Cache Queue
 * <p>2. WillTasksminArea consolidation, bulk execution
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022/3/27 17:36
 */
public abstract class AbstractBufferActuator<Request extends BufferRequest, Response extends BufferResponse> implements BufferActuator, ScheduledTaskJob {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Class<Response> responseClazz;
    private Lock taskLock;
    private BufferActuatorConfig config;
    private BlockingQueue<Request> queue;

    @Resource
    private ScheduledTaskService scheduledTaskService;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TimeRegistry timeRegistry;

    public AbstractBufferActuator() {
        int level = 5;
        Class<?> aClass = getClass();
        while (level > 0) {
            if (aClass.getSuperclass() == AbstractBufferActuator.class) {
                responseClazz = (Class<Response>) ((ParameterizedType) aClass.getGenericSuperclass()).getActualTypeArguments()[1];
                break;
            }
            aClass = aClass.getSuperclass();
            level--;
        }
        Assert.notNull(responseClazz, String.format("%s' parent %s pane Response is empty.", getClass().getName(), AbstractBufferActuator.class.getName()));
    }

    /**
     * InitializeConfigure
     */
    protected void buildConfig() {
        Assert.notNull(config, "Please configure the cache implementer, setConfig (BufferActuatorConfig config)");
        buildQueueConfig();
        scheduledTaskService.start(config.getBufferPeriodMillisecond(), this);
    }

    /**
     * InitializeCache Que InstitutionConfigure
     */
    protected void buildQueueConfig() {
        taskLock = new ReentrantLock();
        this.queue = new LinkedBlockingQueue<>(config.getBufferQueueCapacity());
    }

    /**
     * GenerateminSector Entitieskey
     */
    protected abstract String getPartitionKey(Request request);

    /**
     * minSector Entities
     */
    protected abstract void partition(Request request, Response response);

    /**
     * Sync taskYesNoRunning
     */
    public boolean isRunning(BufferRequest request) {
        Meta meta = taskMetaProfile.getMeta(request.getTaskId());
        return meta != null && CommonTaskStatusEnum.isRunning(meta.getState());
    }

    /**
     * YesNoSkip ActionsminArea processing
     */
    protected boolean skipPartition(Request nextRequest, Response response) {
        return false;
    }

    /**
     * Batch
     */
    public abstract void pull(Response response);

    /**
     * Batch processingminArea data
     */
    protected void process(Map<String, Response> map) {
        map.forEach((key, response) -> {
            long now = Instant.now().toEpochMilli();
            try {
                pull(response);
                logger.info("[{}{}]{}, {}ms", key, response.getSuffixName(), response.getTaskSize(), (Instant.now().toEpochMilli() - now));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                // TimelyClearListReference, EasyGC Recovery, mitigationparser SideLinkedList ReservationsMemory
                response.clearDataList();
            }
        });
    }

    /**
     * SubmitTasksFailed
     */
    protected abstract void offerFailed(BlockingQueue<Request> queue, Request request);

    /**
     * Statistical consumptionTPS
     */
    protected void meter(TimeRegistry timeRegistry, long count) {

    }

    @Override
    public void offer(BufferRequest request) {
        if (queue.offer((Request) request)) {
            return;
        }
        if (isRunning(request)) {
            offerFailed(queue, (Request) request);
        }
    }

    @Override
    public void run() {
        boolean locked = false;
        try {
            locked = taskLock.tryLock(3, TimeUnit.SECONDS);
            if (locked) {
                submit();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (locked) {
                taskLock.unlock();
            }
        }
    }

    @Override
    public Queue getQueue() {
        return queue;
    }

    @Override
    public int getQueueCapacity() {
        return config.getBufferQueueCapacity();
    }

    /**
     * Team is empty and currently does not hold a consumption lock.None in-flight submit/process）ConsiderFree。
     * <p>DDL The entry fence should wait.Free，And not only.Yes queue.isEmpty()。
     *
     * @return FreeBack true
     */
    public boolean isIdle() {
        if (queue != null && !queue.isEmpty()) {
            return false;
        }
        if (taskLock instanceof ReentrantLock) {
            return !((ReentrantLock) taskLock).isLocked();
        }
        return taskLock == null;
    }

    private void submit() {
        if (queue.isEmpty()) {
            return;
        }

        AtomicLong batchCounter = new AtomicLong();
        Map<String, Response> map = new ConcurrentHashMap<>();
        while (!queue.isEmpty() && batchCounter.get() < config.getBufferPullCount()) {
            Request poll = queue.poll();
            String key = getPartitionKey(poll);
            Response response = map.compute(key, (k, v) -> {
                if (v == null) {
                    try {
                        return responseClazz.newInstance();
                    } catch (Exception e) {
                        throw new ParserException(e);
                    }
                }
                return v;
            });
            partition(poll, response);
            batchCounter.incrementAndGet();

            Request next = queue.peek();
            if (null != next && skipPartition(next, response)) {
                break;
            }
        }

        process(map);
        map.clear();
        meter(timeRegistry, batchCounter.get());
        map = null;
        batchCounter = null;
    }

    public void setConfig(BufferActuatorConfig config) {
        this.config = config;
    }

    protected int getBufferWriterCount() {
        return config.getBufferWriterCount();
    }
}