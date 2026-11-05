/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.biz.vo.SystemConfigVO;
import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.common.model.RsaVersion;
import org.dbsyncer.parser.model.SystemConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * System settings
 *
 * @author AE86
 * @version 1.0.0
 * @date 2020/5/30 23:14
 */
public interface SystemConfigService {

    /**
     * Modify System Configuration
     *
     * @param params
     */
    String edit(Map<String, String> params);

    /**
     * UpdateAnnouncementsConfigure
     *
     * @param params
     */
    String editNoticeConfig(Map<String, String> params);

    /**
     * AccessSystem settings
     *
     * @return
     */
    SystemConfigVO getSystemConfigVo();

    /**
     * AccessSystem settings
     *
     * @return
     */
    SystemConfig getSystemConfig();

    /**
     * Get AllConfigure（system、user、connector、mapping、tableGroup、meta）
     *
     * @return
     */
    List<ConfigModel> getConfigModelAll();

    /**
     * Verify INTRODUCTIONDocumentationFormat
     *
     * @param filename
     */
    void checkFileSuffix(String filename);

    /**
     * UpdateConfigure（Only support.zip）
     *
     * @param file
     */
    void refreshConfig(File file);

    /**
     * Crude estimateExport ConfigurationVolume (bytes) forListPageNotice，Avoid AllVolumeSerialization.
     *
     * @return Estimated bytes
     */
    long estimateExportSize();

    /**
     * AccessWatermarkInfo
     *
     * @param systemConfig
     * @return
     */
    String getWatermark(SystemConfig systemConfig);

    /**
     * GenerateRSAConfigure
     *
     * @param keyLength
     * @return
     */
    RsaVersion createRSAConfig(int keyLength);
    
}