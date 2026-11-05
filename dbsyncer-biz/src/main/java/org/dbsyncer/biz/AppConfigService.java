/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.biz.vo.VersionVO;

/**
 * @Author AE86
 * @Version 1.0.0
 * @Date 2024-05-12 01:08
 */
public interface AppConfigService {

    /**
     * AccessVersionInfo
     *
     * @return
     */
    VersionVO getVersionInfo(String username);
}