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
package org.realtimeetl.parser.model;

import org.realtimeetl.common.model.ConfigModel;

import java.util.Collections;
import java.util.List;

/**
 * @author Realtime ETL
 * @version 1.0.0
 */
public final class TaskImportResult {

    private final int mappingCount;

    private final List<ConfigModel> enterpriseTasks;

    public TaskImportResult(int mappingCount, List<ConfigModel> enterpriseTasks) {
        this.mappingCount = mappingCount;
        this.enterpriseTasks = enterpriseTasks == null ? Collections.emptyList() : enterpriseTasks;
    }

    public int getMappingCount() {
        return mappingCount;
    }

    /**
     * DroppedLibraryEnterprisesTasks（s Verification/transfer) , to be called byRefresh TaskService MemoryCache.
     */
    public List<ConfigModel> getEnterpriseTasks() {
        return enterpriseTasks;
    }
}
