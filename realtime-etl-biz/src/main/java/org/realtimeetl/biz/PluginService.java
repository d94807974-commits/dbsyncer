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

import org.realtimeetl.biz.vo.PluginVO;

import java.util.List;

/**
 * @author Realtime ETL
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
