/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.biz.vo.PluginVO;

import java.util.List;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2020/01/13 17:18
 */
public interface PluginService {

    /**
     * Get installedPlugin List（Light, noMappingAssociationInfo）。
     * ForMapping/MapEditPagePull Interregional.AllTasks。
     *
     * @return Direction InstalledPlugin
     */
    List<PluginVO> listPlugins();

    /**
     * AccessPluginUploadPath
     *
     * @return
     */
    String getPluginPath();

    /**
     * Get Development Package Path
     *
     * @return
     */
    String getLibraryPath();

    /**
     * LoadPlugin
     */
    void loadPlugins();

    /**
     * Unmount loadedUploadPlugin（OffSPI/ClassLoader，No, no, no.Documentation）。OverwriteUploadForward call, avoidWindows DocumentationLock.
     *
     * @param fileName PluginFilename
     */
    void unload(String fileName);

    /**
     * DeleteAlreadyUploadYes.Plugin JAR（Inline PluginsI can't delete it.
     *
     * @param fileName PluginFilename
     */
    void delete(String fileName);

    /**
     * InspectionDocumentationFormat
     *
     * @param filename
     */
    void checkFileSuffix(String filename);
}
