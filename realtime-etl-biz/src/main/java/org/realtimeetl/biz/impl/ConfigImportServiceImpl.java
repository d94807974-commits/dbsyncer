/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.impl;

import org.apache.commons.io.FileUtils;
import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.ConfigImportService;
import org.realtimeetl.common.config.PackageFormatConfig;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.VersionInfo;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.PackageZipUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.manager.impl.PreloadTemplate;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.DatabaseSyncProfile;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.TaskProfile;
import org.realtimeetl.parser.UserProfile;
import org.realtimeetl.parser.ValidateSyncProfile;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.model.TaskImportResult;
import org.realtimeetl.sdk.model.DatabaseSyncTask;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * ConfigureImport：ZIP(formatVersion=2) Commission FormationProfile DropLibrary，Again.PreloadTemplate End of story.
 *
 * @author AE86
 * @version 1.0.0
 * @date 2026/07/29
 */
@Service
public class ConfigImportServiceImpl implements ConfigImportService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private PreloadTemplate preloadTemplate;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private UserProfile userProfile;

    @Resource
    private TaskProfile taskProfile;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private ValidateSyncProfile validateSyncProfile;

    @Resource
    private DatabaseSyncProfile databaseSyncProfile;

    @Override
    public void importConfig(File file) {
        Assert.notNull(file, "the config file is null.");
        String name = file.getName();
        Assert.isTrue(StringUtil.isNotBlank(name) && name.toLowerCase().endsWith(".zip"),
                "Only import .zip profile is supported");
        try {
            importZip(file);
        } finally {
            FileUtils.deleteQuietly(file);
        }
    }

    private void importZip(File file) {
        try (ZipFile zip = new ZipFile(file, StandardCharsets.UTF_8)) {
            VersionInfo info = PackageZipUtil.validateManifest(zip);
            logger.info("upload config zip: appName={}, version={}, createTime={}",
                    info.getAppName(), info.getVersion(), info.getCreateTime());

            systemConfigProfile.importFromJson(PackageZipUtil.readOptionalEntry(zip, PackageFormatConfig.SYSTEM));
            userProfile.importFromJson(PackageZipUtil.readOptionalEntry(zip, PackageFormatConfig.USER));
            connectorProfile.importConnectorsFromJson(PackageZipUtil.readOptionalEntry(zip, PackageFormatConfig.CONNECTOR));

            TaskImportResult taskResult = taskProfile.importTasksFromZip(zip);
            for (ConfigModel task : taskResult.getEnterpriseTasks()) {
                if (task instanceof ValidateSyncTask) {
                    validateSyncProfile.update((ValidateSyncTask) task);
                } else if (task instanceof DatabaseSyncTask) {
                    databaseSyncProfile.update((DatabaseSyncTask) task);
                }
            }

            importTableGroupsFromZip(zip);
            taskMetaProfile.importMetaFromJson(PackageZipUtil.readOptionalEntry(zip, PackageFormatConfig.META));
            taskProfile.importTaskDetailSchemasFromJson(PackageZipUtil.readOptionalEntry(zip, PackageFormatConfig.TASK_DETAIL));

            preloadTemplate.afterConfigImport();
        } catch (BizException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new BizException(e.getMessage(), e);
        } catch (Exception e) {
            throw new BizException("Import configuration ZIP failed: " + e.getMessage(), e);
        }
    }

    /**
     * FromZIP Import table_group/*.ndjson（No Table Level PresetMeta，Meta By Follow-upmeta.json Revert.
     */
    private void importTableGroupsFromZip(ZipFile zip) throws IOException {
        if (zip == null) {
            return;
        }
        List<String> buffer = new ArrayList<>(PackageFormatConfig.IMPORT_BATCH_SIZE);
        PackageZipUtil.pageScanTableGroupNdjsonLines(zip, line -> {
            buffer.add(line);
            if (buffer.size() >= PackageFormatConfig.IMPORT_BATCH_SIZE) {
                importTableGroupNdjsonLines(new ArrayList<>(buffer));
                buffer.clear();
            }
        });
        if (!CollectionUtils.isEmpty(buffer)) {
            importTableGroupNdjsonLines(buffer);
        }
    }

    private void importTableGroupNdjsonLines(List<String> ndjsonLines) {
        if (CollectionUtils.isEmpty(ndjsonLines)) {
            return;
        }
        List<TableGroup> buffer = new ArrayList<>(PackageFormatConfig.IMPORT_BATCH_SIZE);
        for (String line : ndjsonLines) {
            if (StringUtil.isBlank(line)) {
                continue;
            }
            TableGroup tg = JsonUtil.jsonToObj(line, TableGroup.class);
            if (tg == null) {
                continue;
            }
            buffer.add(tg);
            if (buffer.size() >= PackageFormatConfig.IMPORT_BATCH_SIZE) {
                tableGroupProfile.addTableGroupBatchWithoutMeta(new ArrayList<>(buffer));
                buffer.clear();
            }
        }
        if (!CollectionUtils.isEmpty(buffer)) {
            tableGroupProfile.addTableGroupBatchWithoutMeta(new ArrayList<>(buffer));
        }
    }
}
