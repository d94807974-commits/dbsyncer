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

import org.realtimeetl.biz.AppConfigService;
import org.realtimeetl.biz.SystemConfigService;
import org.realtimeetl.biz.vo.VersionVO;
import org.realtimeetl.common.config.AppConfig;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.model.SystemConfig;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2024-05-12 01:08
 */
@Component
public class AppConfigServiceImpl implements AppConfigService {

    @Resource
    private AppConfig appConfig;

    @Resource
    private SystemConfigService systemConfigService;

    public static final String WATERMARK_USERNAME = "${username}";

    @Override
    public VersionVO getVersionInfo(String username) {
        VersionVO versionVo = new VersionVO(appConfig.getName(), appConfig.getCopyright());
        // YesNoEnableWatermark
        SystemConfig systemConfig = systemConfigService.getSystemConfig();
        if (systemConfig.isEnableWatermark()) {
            String watermark = systemConfigService.getWatermark(systemConfig);
            watermark = StringUtil.replace(watermark, WATERMARK_USERNAME, username);
            versionVo.setWatermark(watermark);
        }
        return versionVo;
    }
}
