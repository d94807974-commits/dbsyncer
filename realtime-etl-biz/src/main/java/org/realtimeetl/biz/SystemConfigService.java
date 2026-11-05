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
package org.realtimeetl.biz;

import org.realtimeetl.biz.vo.SystemConfigVO;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.RsaVersion;
import org.realtimeetl.parser.model.SystemConfig;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * System settings
 *
 * @author Realtime ETL
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