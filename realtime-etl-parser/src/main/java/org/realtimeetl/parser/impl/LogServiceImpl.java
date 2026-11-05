/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.parser.impl;

import org.realtimeetl.common.enums.StatusEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.storage.StorageService;
import org.realtimeetl.storage.impl.SnowflakeIdWorker;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @version 1.0.0
 * @author AE86
 * @date 2020-05-21 23:18
 */
@Component
public class LogServiceImpl implements LogService {

    /**
     * and {@link org.realtimeetl.parser.model.SystemConfig} DefaultmaxStorageErrorLength Alignment;
     * System settingsNot yet enteredLibraryKeep your head down. Keep your foot down.Log NPE。
     */
    private static final int DEFAULT_MAX_STORAGE_ERROR_LENGTH = 8192;

    @Resource
    private StorageService storageService;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    private Executor storageExecutor;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Override
    public void log(LogType logType) {
        asyncWrite(logType.getType(), StringUtil.EMPTY, StringUtil.EMPTY, StatusEnum.SUCCESS.getCode(),
                String.format("%s%s", logType.getName(), logType.getMessage()));
    }

    @Override
    public void log(LogType logType, String msg) {
        asyncWrite(logType.getType(), StringUtil.EMPTY, StringUtil.EMPTY, StatusEnum.SUCCESS.getCode(),
                null == msg ? logType.getMessage() : msg);
    }

    @Override
    public void log(LogType logType, String format, Object... args) {
        asyncWrite(logType.getType(), StringUtil.EMPTY, StringUtil.EMPTY, StatusEnum.SUCCESS.getCode(),
                String.format(format, args));
    }

    @Override
    public void log(LogType logType, String username, String ip, int status, String msg) {
        asyncWrite(logType.getType(), username, ip, status, null == msg ? logType.getMessage() : msg);
    }

    @Override
    public void log(LogType logType, String username, String ip, int status, Map<String, Object> detail) {
        String json;
        if (CollectionUtils.isEmpty(detail)) {
            json = logType.getMessage();
        } else {
            json = JsonUtil.objToJsonSafe(detail);
        }
        asyncWrite(logType.getType(), username, ip, status, json);
    }

    private void asyncWrite(String type, String username, String ip, int status, String content) {
        storageExecutor.execute(() -> {
            Map<String, Object> params = new HashMap<>();
            params.put(ConfigConstant.CONFIG_MODEL_ID, String.valueOf(snowflakeIdWorker.nextId()));
            params.put(ConfigConstant.CONFIG_MODEL_TYPE, type);
            params.put(ConfigConstant.LOG_USERNAME, StringUtil.getIfBlank(username, StringUtil.EMPTY));
            params.put(ConfigConstant.LOG_IP, StringUtil.getIfBlank(ip, StringUtil.EMPTY));
            params.put(ConfigConstant.LOG_STATUS, status);
            params.put(ConfigConstant.CONFIG_MODEL_JSON, StringUtil.substring(content, 0, resolveMaxStorageErrorLength()));
            params.put(ConfigConstant.CONFIG_MODEL_CREATE_TIME, Instant.now().toEpochMilli());
            storageService.add(StorageEnum.LOG, params);
        });
    }

    private int resolveMaxStorageErrorLength() {
        int max = systemConfigProfile.getSystemConfig().getMaxStorageErrorLength();
        return max > 0 ? max : DEFAULT_MAX_STORAGE_ERROR_LENGTH;
    }

}
