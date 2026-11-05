/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz;

import org.realtimeetl.common.model.VersionInfo;

import java.io.IOException;
import java.io.OutputStream;

/**
 * ConfigureExport（FluidZIP）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026/07/29
 */
public interface ConfigExportService {

    /**
     * Synchronising folderConfigure ZIP Package to output stream (check and write, no)FullIn.Memory）。
     *
     * @param out         Objective Combinedflow (e.g.)HttpServletResponse Output stream)
     * @param versionInfo VersionHeadInfo
     * @throws IOException WriteFailed
     */
    void exportZip(OutputStream out, VersionInfo versionInfo) throws IOException;

    /**
     * DrugsExportuncompressed volume (bytes) forListPageNotice，Avoid AllVolumeSerialization.
     *
     * @return Estimated bytes
     */
    long estimateExportSize();
}
