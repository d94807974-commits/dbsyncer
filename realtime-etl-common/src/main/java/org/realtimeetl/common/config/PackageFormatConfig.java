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
package org.realtimeetl.common.config;

/**
 * ConfigureImportExport ZIP Package format as agreedformatVersion=2）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026/07/29
 */
public final class PackageFormatConfig {

    /**
     * NewZIP minSnippet FormatVersionNo. No.
     */
    public static final int FORMAT_VERSION = 2;

    public static final String MANIFEST = "manifest.json";
    public static final String SYSTEM = "system.json";
    public static final String USER = "user.json";
    public static final String CONNECTOR = "connector.json";
    /**
     * TasksTableFull（Syncmapping / Data validation / Database migration）
     */
    public static final String TASK = "task.json";
    /**
     * Old CompatibilityZIP：OnlySync task
     */
    public static final String MAPPING = "mapping.json";
    public static final String META = "meta.json";
    /**
     * TasksDetailed dynamicsminTable schemaListtaskIds，Oh, no.ExportRow data；Import)
     */
    public static final String TASK_DETAIL = "task_detail.json";
    public static final String TABLE_GROUP_DIR = "table_group/";
    public static final String NDJSON_SUFFIX = ".ndjson";

    /**
     * ImportTimetable_group / task Equal BatchLibraryBatch Size
     */
    public static final int IMPORT_BATCH_SIZE = 200;

    /**
     * ListPageExportCrude size estimate: eachConfigureAbout bytes (uncompressed)
     */
    public static final long ESTIMATE_BYTES_PER_ROW = 2048L;

    private PackageFormatConfig() {
    }
}
