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
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.ConfigExportService;
import org.realtimeetl.common.config.PackageFormatConfig;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.VersionInfo;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.TaskProfile;
import org.realtimeetl.parser.UserProfile;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.model.UserConfig;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * FluidZIP ConfigureExport：system/user/connector/task/meta WriteJSON array,
 * table_group PresstaskId minDocumentationWriteNDJSON；task.json Include sync/check/transferFullTasks；
 * task_detail.json 仅含任务 ID 清单，导入时预建空分表（不导出行数据）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026/07/29
 */
@Service
public class ConfigExportServiceImpl implements ConfigExportService {

    @Resource
    private UserProfile userProfile;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TaskProfile taskProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Override
    public void exportZip(OutputStream out, VersionInfo versionInfo) throws IOException {
        if (out == null) {
            throw new BizException("Export output stream cannot be empty");
        }
        if (versionInfo == null) {
            throw new BizException("Version information cannot be empty");
        }

        Map<String, Integer> counts = new LinkedHashMap<>();
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
            counts.put(ConfigConstant.SYSTEM, writeJsonArray(zos, PackageFormatConfig.SYSTEM,
                    systemConfig == null ? Collections.emptyList() : Collections.singletonList(systemConfig)));
            UserConfig userConfig = userProfile.getUserConfig();
            counts.put(ConfigConstant.USER, writeJsonArray(zos, PackageFormatConfig.USER,
                    userConfig == null ? Collections.emptyList() : Collections.singletonList(userConfig)));
            counts.put(ConfigConstant.CONNECTOR, writeJsonArray(zos, PackageFormatConfig.CONNECTOR, connectorProfile.getConnectorAll()));
            List<String> taskIds = new ArrayList<>();
            counts.put(ConfigConstant.TASK, writeAllTasks(zos, taskIds));
            counts.put(ConfigConstant.TABLE_GROUP, writeTableGroupsToZip(zos));
            counts.put(ConfigConstant.META, taskMetaProfile.writeMetasToZip(zos));
            counts.put(StorageEnum.TASK_DETAIL.getType(), writeTaskDetailSchemas(zos, taskIds));
            writeManifest(zos, versionInfo, counts);
        }
    }

    @Override
    public long estimateExportSize() {
        long rows = 0L;
        rows += systemConfigProfile.countSystemConfigs();
        rows += userProfile.countUsers();
        rows += connectorProfile.countConnectors();
        rows += taskProfile.countAllTasks();
        rows += tableGroupProfile.countTableGroups();
        rows += taskMetaProfile.countMeta();
        return Math.max(rows, 1L) * PackageFormatConfig.ESTIMATE_BYTES_PER_ROW;
    }

    private void writeManifest(ZipOutputStream zos, VersionInfo versionInfo, Map<String, Integer> counts) throws IOException {
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("formatVersion", PackageFormatConfig.FORMAT_VERSION);
        manifest.put("version", versionInfo.getVersion());
        manifest.put("appName", versionInfo.getAppName());
        manifest.put("createTime", versionInfo.getCreateTime());
        manifest.put("counts", counts);
        writeBytes(zos, PackageFormatConfig.MANIFEST, JsonUtil.objToJson(manifest).getBytes(StandardCharsets.UTF_8));
    }

    private int writeJsonArray(ZipOutputStream zos, String entryName, List<? extends ConfigModel> models) throws IOException {
        List<? extends ConfigModel> list = models == null ? Collections.emptyList() : models;
        writeBytes(zos, entryName, JsonUtil.objToJson(list).getBytes(StandardCharsets.UTF_8));
        return list.size();
    }

    /**
     * Export realtime_etl_task Full table: Synchronization(mapping)、Data validation(VALIDATE_SYNC)、Database migration(DATABASE_SYNC)。
     *
     * @param taskIds CollectionTasks ID，ForExport task_detail minTable schemaList
     */
    private int writeAllTasks(ZipOutputStream zos, List<String> taskIds) throws IOException {
        List<Map<String, Object>> tasks = taskProfile.listAllTaskJsonMaps();
        for (Map<String, Object> task : tasks) {
            Object id = task.get(ConfigConstant.CONFIG_MODEL_ID);
            if (id != null && StringUtil.isNotBlank(String.valueOf(id))) {
                taskIds.add(String.valueOf(id));
            }
        }
        writeBytes(zos, PackageFormatConfig.TASK, JsonUtil.objToJson(tasks).getBytes(StandardCharsets.UTF_8));
        return tasks.size();
    }

    /**
     * Export task_detail DynamicShit.minTable schemaListtaskIds，Oh, no.ExportRow data）。
     */
    private int writeTaskDetailSchemas(ZipOutputStream zos, List<String> taskIds) throws IOException {
        writeBytes(zos, PackageFormatConfig.TASK_DETAIL,
                taskProfile.exportTaskDetailSchemasJson(taskIds).getBytes(StandardCharsets.UTF_8));
        return taskIds == null ? 0 : taskIds.size();
    }

    /**
     * PresstaskId minDocumentationWritetable_group NDJSON。
     */
    private int writeTableGroupsToZip(ZipOutputStream zos) throws IOException {
        String[] currentTaskId = {null};
        BufferedWriter[] writer = {null};
        int[] count = {0};
        try {
            tableGroupProfile.pageScanTableGroupsByTaskId(tg -> {
                try {
                    if (!StringUtil.equals(currentTaskId[0], tg.getTaskId())) {
                        flushWriter(writer[0]);
                        if (currentTaskId[0] != null) {
                            zos.closeEntry();
                        }
                        currentTaskId[0] = tg.getTaskId();
                        zos.putNextEntry(new ZipEntry(PackageFormatConfig.TABLE_GROUP_DIR + currentTaskId[0] + PackageFormatConfig.NDJSON_SUFFIX));
                        writer[0] = new BufferedWriter(new OutputStreamWriter(zos, StandardCharsets.UTF_8));
                    }
                    writer[0].write(JsonUtil.objToJson(tg));
                    writer[0].newLine();
                    count[0]++;
                } catch (IOException e) {
                    throw new BizException("Export of table group failed: " + e.getMessage(), e);
                }
            });
        } finally {
            flushWriter(writer[0]);
            if (currentTaskId[0] != null) {
                zos.closeEntry();
            }
        }
        return count[0];
    }

    private void writeBytes(ZipOutputStream zos, String entryName, byte[] bytes) throws IOException {
        zos.putNextEntry(new ZipEntry(entryName));
        zos.write(bytes);
        zos.closeEntry();
    }

    private static void flushWriter(BufferedWriter writer) throws IOException {
        if (writer != null) {
            writer.flush();
        }
    }
}
