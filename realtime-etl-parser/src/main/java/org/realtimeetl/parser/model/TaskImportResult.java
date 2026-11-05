/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.model;

import org.realtimeetl.common.model.ConfigModel;

import java.util.Collections;
import java.util.List;

/**
 * @author wuji
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
