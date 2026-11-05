/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.manager.impl;

import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.manager.AbstractPuller;
import org.dbsyncer.parser.LogService;
import org.dbsyncer.parser.LogType;
import org.dbsyncer.parser.MappingProfile;
import org.dbsyncer.parser.TableGroupProfile;
import org.dbsyncer.parser.TaskMetaProfile;
import org.dbsyncer.parser.enums.ParserEnum;
import org.dbsyncer.parser.model.Mapping;
import org.dbsyncer.parser.model.Meta;
import org.dbsyncer.parser.util.FullTableProgressUtil;
import org.dbsyncer.sdk.enums.ModelEnum;
import org.dbsyncer.sdk.service.FullIncrementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Full + incrementalSync: Catch subscribed→ Full → From BitStartIncremental
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-05-18 15:02
 */
@Component
public final class FullIncrementPuller extends AbstractPuller implements FullIncrementService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Set<String> running = new CopyOnWriteArraySet<>();

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private FullPuller fullPuller;

    @Resource
    private IncrementPuller incrementPuller;

    @Resource
    private LogService logService;

    @Override
    public void start(Mapping mapping) {
        start(mapping, false);
    }

    @Override
    public void start(Mapping mapping, boolean autoRecovery) {
        final String taskId = mapping.getId();
        running.add(taskId);
        Thread worker = new Thread(() -> runFullIncrementSync(mapping, taskId, autoRecovery));
        worker.setName("full-increment-worker-" + mapping.getId());
        worker.setDaemon(false);
        worker.start();
    }

    @Override
    public void close(String taskId) {
        running.remove(taskId);
        fullPuller.close(taskId);
        incrementPuller.close(taskId);
    }

    @Override
    public List<String> getTaskId() {
        return Collections.emptyList();
    }

    /**
     * BatchFullPrepare: recover and skip.NoCaptureIncrementalbits.
     */
    @Override
    public void prepareFullPhase(String taskId) {
        Mapping mapping = mappingProfile.get(taskId);
        Meta meta = taskMetaProfile.getMeta(taskId);
        prepareFullPhase(mapping, meta);
    }

    /**
     * BatchFullFinishPostmarkIncrementalPhase andStartIncremental。
     */
    @Override
    public void switchToIncrement(String taskId) {
        Mapping mapping = mappingProfile.get(taskId);
        if (mapping == null) {
            return;
        }
        markFullIncrementPhase(mapping.getId(), ModelEnum.INCREMENT.getCode());
        logger.info("Start incremental sync: }", mapping.getName());
        incrementPuller.start(mapping, false);
    }

    private void runFullIncrementSync(Mapping mapping, String taskId, boolean autoRecovery) {
        try {
            Meta meta = taskMetaProfile.getMeta(taskId);
            if (ModelEnum.isIncrement(getFullIncrementPhase(meta))) {
                incrementPuller.start(mapping, autoRecovery);
                return;
            }
            prepareFullPhase(mapping, meta);
            logger.info("Start FullSync: }", mapping.getName());
            fullPuller.runSync(mapping, false);
            if (!isRunning(taskId)) {
                return;
            }
            markFullIncrementPhase(taskId, ModelEnum.INCREMENT.getCode());
            incrementPuller.start(mapping, autoRecovery);
        } catch (Exception e) {
            logger.error("Full + incremental sync failed: },}", taskId, e.getMessage(), e);
            logService.log(LogType.SystemLog.ERROR, e.getMessage());
            incrementPuller.close(taskId);
            publishClosedEvent(taskId);
        } finally {
            running.remove(taskId);
        }
    }

    private void prepareFullPhase(Mapping mapping, Meta meta) {
        if (shouldResumeFullPhase(meta)) {
            return;
        }
        //Start over, get in.IncrementalBitsInfo
        incrementPuller.captureAndSaveOffset(mapping);
    }

    private boolean isRunning(String taskId) {
        return running.contains(taskId);
    }

    private String getFullIncrementPhase(Meta meta) {
        if (meta == null || meta.getSnapshot() == null) {
            return null;
        }
        return meta.getSnapshot().get(ParserEnum.FULL_INCREMENT_PHASE.getCode());
    }

    /**
     * FullNot in placeFinish, fromsnapshot Breakpoints restored, avoid.ResetAfterShit.success Repeat cumulative
     */
    private boolean shouldResumeFullPhase(Meta meta) {
        String phase = getFullIncrementPhase(meta);
        //IfYesEmpty InterregionalFullIncrementalHe didn't run.
        if (StringUtil.isBlank(phase)) {
            return false;
        }
        long total = meta.getTotal().get();
        long processed = meta.getSuccess().get() + meta.getFail().get();
        if (total > 0 && processed >= total) {
            return false;
        }

        return FullTableProgressUtil.hasIncomplete(taskMetaProfile, tableGroupProfile, meta.getTaskId())
                || processed > 0;
    }

    /**
     * Tag           Status
     */
    private void markFullIncrementPhase(String taskId, String phase) {
        Meta meta = taskMetaProfile.getMeta(taskId);
        meta.getSnapshot().put(ParserEnum.FULL_INCREMENT_PHASE.getCode(), phase);

        //ClearFullTag           
        meta.getSnapshot().remove(ParserEnum.PAGE_INDEX.getCode());
        meta.getSnapshot().remove(ParserEnum.CURSOR.getCode());
        meta.getSnapshot().remove(ParserEnum.TABLE_GROUP_INDEX.getCode());
        meta.getSnapshot().remove(ParserEnum.TABLE_PROGRESS.getCode());
        FullTableProgressUtil.clearAll(taskMetaProfile, tableGroupProfile, meta.getTaskId());
        taskMetaProfile.updateMeta(meta);
    }

}
