/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz;

import java.io.File;

/**
 * ConfigureImport（ZIP formatVersion=2）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026/07/29
 */
public interface ConfigImportService {

    /**
     * FromZIP ConfigurePackageImport。
     *
     * @param file TemporaryConfigureDocumentation
     */
    void importConfig(File file);
}
