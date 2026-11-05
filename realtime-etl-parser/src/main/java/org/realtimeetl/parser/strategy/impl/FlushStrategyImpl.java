/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.parser.strategy.impl;

import com.google.protobuf.ByteString;
import org.realtimeetl.common.binlog.proto.BinlogMap;
import org.realtimeetl.common.enums.TaskLevelEnum;
import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.flush.BufferActuator;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.StorageRequest;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.strategy.FlushStrategy;
import org.realtimeetl.parser.util.FullTableProgressUtil;
import org.realtimeetl.parser.util.MetaLockUtil;
import org.realtimeetl.plugin.model.MappingErrorContent;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.ModelEnum;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.MetaIncrement;
import org.realtimeetl.sdk.notice.MessageService;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.storage.enums.StorageDataStatusEnum;
import org.realtimeetl.storage.impl.SnowflakeIdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2021-11-18 22:22
 */
@Component
public final class FlushStrategyImpl implements FlushStrategy {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private LogService logService;

    @Resource
    private BufferActuator storageBufferActuator;

    @Override
    public void flushFullData(Result result, SchemaResolver targetSchemaResolver, Map<String, Field> targetFieldMap) {
        // Oh, no.Record full volume data, Record onlyIncrementalSync data, WillErrorRecordedSystem LogMedium
        if (!systemConfigProfile.getSystemConfig().isEnableStorageWriteFull()) {
            // Oh, no.Record full volume data，Statistics onlySuccessFailedTotal
            refreshTotal(result);

            if (!CollectionUtils.isEmpty(result.getFailData())) {
                logger.error(result.getError().toString());
                LogType logType = LogType.TableGroupLog.FULL_FAILED;
                logService.log(logType, "%s:%s:%s", result.getTargetTableGroupName(), logType.getMessage(), result.getError().toString());
            }
            return;
        }

        flush(result, targetSchemaResolver, targetFieldMap);
    }

    @Override
    public void flushIncrementData(Result result, SchemaResolver targetSchemaResolver, Map<String, Field> targetFieldMap) {
        flush(result, targetSchemaResolver, targetFieldMap);
    }

    private void asyncWrite(Result result, SchemaResolver schemaResolver, Map<String, Field> targetFieldMap, boolean success, List<Map> data, String error) {
        String taskId = result.getTaskId();
        String event = result.getEvent();
        String tableGroupId = result.getTableGroupId();
        String targetTableGroupName = result.getTargetTableGroupName();

        long now = Instant.now().toEpochMilli();
        data.forEach(r -> {
            Map<String, Object> row = new HashMap<>();
            row.put(ConfigConstant.CONFIG_MODEL_ID, String.valueOf(snowflakeIdWorker.nextId()));
            // DetailsminTableTABLE_GROUP_ID (a) Associated map;EventWritingTYPE；SuccessStatusWritingIS_SUCCESS
            row.put(ConfigConstant.DETAIL_IS_SUCCESS, success ? StorageDataStatusEnum.SUCCESS.getValue() : StorageDataStatusEnum.FAIL.getValue());
            row.put(ConfigConstant.DATA_TABLE_GROUP_ID, tableGroupId);
            row.put(ConfigConstant.DETAIL_TARGET_TABLE, targetTableGroupName);
            row.put(ConfigConstant.CONFIG_MODEL_TYPE, event);
            row.put(ConfigConstant.DATA_ERROR, error);
            row.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, now);
            row.put(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, now);
            try {
                row.put(ConfigConstant.BINLOG_DATA, toBinlogBytes(schemaResolver, r, targetFieldMap));
            } catch (Exception e) {
                // ICALLYDetailsTypeInfo
                StringBuilder typeInfo = new StringBuilder();
                r.forEach((k, val) -> {
                    typeInfo.append(k).append("=").append(val == null ? "null" : val.getClass().getName()).append("; ");
                });
                logger.warn("There may be a large file type of Blob or inputstream, which cannot be sequenced. Field type details: {", typeInfo, e);
            }
            storageBufferActuator.offer(new StorageRequest(taskId, row));
        });
    }

    private byte[] toBinlogBytes(SchemaResolver schemaResolver, Map<String, Object> data, Map<String, Field> fieldMap) {
        BinlogMap.Builder dataBuilder = BinlogMap.newBuilder();
        data.forEach((k, v) -> {
            if (null != v) {
                // DDL
                if (fieldMap == null) {
                    ByteString bytes = schemaResolver.serialize(v, null);
                    if (null != bytes) {
                        dataBuilder.putRow(k, bytes);
                    }
                    return;
                }

                // DML
                Field field = fieldMap.get(k);
                if (field != null) {
                    ByteString bytes = schemaResolver.serialize(v, field);
                    if (null != bytes) {
                        dataBuilder.putRow(k, bytes);
                    }
                }
            }
        });
        return dataBuilder.build().toByteArray();
    }

    private void refreshTotal(Result writer) {
        int success = writer.getSuccessData().size();
        int fail = writer.getFailData().size();
        if (success == 0 && fail == 0) {
            return;
        }
        // and FullTableProgressUtil.save TotalUsetableLock；Update progressupdateMetaProgress（Reload before writing)OverwriteAtomic Count
        synchronized (MetaLockUtil.lock(writer.getTaskId())) {
            taskMetaProfile.incrementMeta(MetaIncrement.of(writer.getTaskId()).success(success).fail(fail));
        }
        incrementTableMeta(writer.getTableGroupId(), success, fail);
    }

    /**
     * Sync Table LevelMeta Count:taskId=table_group.id，isTaskDetail=1；Primary keyByADD PathGenerateSnowflake.
     * <p>Atom Plussuccess/fail；and {@link FullTableProgressUtil#save} TotalUse the watch lock.
     */
    private void incrementTableMeta(String tableGroupId, long success, long fail) {
        if (StringUtil.isBlank(tableGroupId)) {
            return;
        }
        synchronized (FullTableProgressUtil.tableLock(tableGroupId)) {
            Meta tableMeta = taskMetaProfile.getMetaDetail(tableGroupId);
            if (tableMeta == null) {
                tableMeta = new Meta();
                tableMeta.setTaskId(tableGroupId);
                tableMeta.setIsTaskDetail(TaskLevelEnum.TASK_DETAIL.getCode());
                long now = Instant.now().toEpochMilli();
                tableMeta.setCreateTime(now);
                tableMeta.setUpdateTime(now);
                taskMetaProfile.addMeta(tableMeta);
                tableMeta = taskMetaProfile.getMetaDetail(tableGroupId);
            }
            taskMetaProfile.incrementMeta(MetaIncrement.of(tableMeta.getTaskId()).success(success).fail(fail));
        }
    }

    private void flush(Result result, SchemaResolver schemaResolver, Map<String, Field> targetFieldMap) {
        refreshTotal(result);

        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        // YesNoWriteFailuresAccording to
        if (systemConfig.isEnableStorageWriteFail() && !CollectionUtils.isEmpty(result.getFailData())) {
            final String error = StringUtil.substring(result.getError().toString(), 0, systemConfig.getMaxStorageErrorLength());
            asyncWrite(result, schemaResolver, targetFieldMap, false, result.getFailData(), error);
        }

        // YesNoWriteSuccessesAccording to
        if (systemConfig.isEnableStorageWriteSuccess() && !CollectionUtils.isEmpty(result.getSuccessData())) {
            asyncWrite(result, schemaResolver, targetFieldMap, true, result.getSuccessData(), "");
        }
    }

    @Override
    public void writeStorageOnly(Result result, SchemaResolver targetSchemaResolver, Map<String, Field> targetFieldMap) {
        if (result == null || StringUtil.isBlank(result.getTaskId())) {
            return;
        }
        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        if (systemConfig.isEnableStorageWriteFail() && !CollectionUtils.isEmpty(result.getFailData())) {
            final String error = StringUtil.substring(result.getError().toString(), 0, systemConfig.getMaxStorageErrorLength());
            asyncWrite(result, targetSchemaResolver, targetFieldMap, false, result.getFailData(), error);
        }
        // FullSuccess(a) A large line Ninety-five;and flushFullData On「Record full volume data」AfterSuccessWriting Semantic Alignment
        if (systemConfig.isEnableStorageWriteFull() && systemConfig.isEnableStorageWriteSuccess()
                && !CollectionUtils.isEmpty(result.getSuccessData())) {
            asyncWrite(result, targetSchemaResolver, targetFieldMap, true, result.getSuccessData(), "");
        }
    }

}