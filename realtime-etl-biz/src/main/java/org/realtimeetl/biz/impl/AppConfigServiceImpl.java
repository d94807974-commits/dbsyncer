/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
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
 * @author AE86
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
