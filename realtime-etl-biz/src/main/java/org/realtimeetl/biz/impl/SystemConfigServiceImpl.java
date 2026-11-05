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

import org.realtimeetl.biz.ConfigExportService;
import org.realtimeetl.biz.ConfigImportService;
import org.realtimeetl.biz.SystemConfigService;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.checker.impl.notice.NoticeConfigChecker;
import org.realtimeetl.biz.checker.impl.system.SystemConfigChecker;
import org.realtimeetl.biz.vo.SystemConfigVO;
import org.realtimeetl.common.config.AppConfig;
import org.realtimeetl.common.enums.FileSuffixEnum;
import org.realtimeetl.common.enums.TaskLevelEnum;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.model.RsaVersion;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.RSAUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.manager.impl.PreloadTemplate;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.SystemConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/10/17 23:20
 */
@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private PreloadTemplate preloadTemplate;

    @Resource
    private SystemConfigChecker systemConfigChecker;

    @Resource
    private NoticeConfigChecker noticeConfigChecker;

    @Resource
    private UserConfigService userConfigService;

    @Resource
    private AppConfig appConfig;

    @Resource
    private ConfigImportService configImportService;

    @Resource
    private ConfigExportService configExportService;

    @Override
    public String edit(Map<String, String> params) {
        SystemConfig systemConfig = systemConfigChecker.checkEditConfigModel(params);
        systemConfigProfile.saveSystemConfig(systemConfig);
        return "Change successfully.";
    }

    @Override
    public String editNoticeConfig(Map<String, String> params) {
        SystemConfig systemConfig = noticeConfigChecker.checkEditConfigModel(params);
        systemConfigProfile.saveSystemConfig(systemConfig);
        preloadTemplate.loadNotificationChannel();
        return "Change successfully.";
    }

    @Override
    public SystemConfigVO getSystemConfigVo() {
        SystemConfigVO systemConfigVo = new SystemConfigVO();
        BeanUtils.copyProperties(getSystemConfig(), systemConfigVo);
        systemConfigVo.setWatermark(getWatermark(systemConfigVo));
        return systemConfigVo;
    }

    @Override
    public SystemConfig getSystemConfig() {
        SystemConfig config = systemConfigProfile.getSystemConfig();
        if (null != config) {
            return config;
        }

        synchronized (this) {
            config = systemConfigProfile.getSystemConfig();
            if (null == config) {
                config = systemConfigChecker.checkAddConfigModel(new HashMap<>());
            }
            return config;
        }
    }

    @Override
    public List<ConfigModel> getConfigModelAll() {
        List<ConfigModel> list = new ArrayList<>();
        list.add(getSystemConfig());
        list.add(userConfigService.getUserConfig());
        list.addAll(connectorProfile.getConnectorAll().stream().limit(5).collect(Collectors.toList()));
        Paging<Mapping> mappingPaging = mappingProfile.query(1, 5, null, null);
        if (mappingPaging != null && !CollectionUtils.isEmpty(mappingPaging.getData())) {
            list.addAll(mappingPaging.getData());
        }
        Paging<Meta> metaPaging = taskMetaProfile.queryMeta(TaskLevelEnum.TASK.getCode(), 1, 5);
        if (metaPaging != null && !CollectionUtils.isEmpty(metaPaging.getData())) {
            list.addAll(metaPaging.getData());
        }
        return list;
    }

    @Override
    public void checkFileSuffix(String filename) {
        Assert.hasText(filename, "the config filename is null.");
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        FileSuffixEnum fileSuffix = FileSuffixEnum.getFileSuffix(suffix);
        Assert.notNull(fileSuffix, "Illegal file suffix");
        boolean supported = FileSuffixEnum.ZIP == fileSuffix;
        Assert.isTrue(supported, String.format("Invalid file extension \"%s\", only \"%s\" is supported.",
                filename, FileSuffixEnum.ZIP.getName()));
    }

    @Override
    public void refreshConfig(File file) {
        configImportService.importConfig(file);
    }

    @Override
    public long estimateExportSize() {
        return configExportService.estimateExportSize();
    }

    @Override
    public String getWatermark(SystemConfig systemConfig) {
        return StringUtil.isNotBlank(systemConfig.getWatermark()) ? systemConfig.getWatermark() : appConfig.getName() + "-${username}";
    }

    @Override
    public RsaVersion createRSAConfig(int keyLength) {
        Assert.isTrue(keyLength >= 1024 && keyLength <= 8192, "Scope of key length support [1024-8192]");
        return RSAUtil.createKeys(keyLength);
    }
}