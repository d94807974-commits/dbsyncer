/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.util;

import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.sdk.model.DatabaseMapping;
import org.dbsyncer.sdk.model.TableMapping;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Database migration：LibraryTable Groupand table_group ComplementaryShh..
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-07-20 15:30
 */
public abstract class DatabaseSyncMappingUtil {

    private DatabaseSyncMappingUtil() {
    }

    /**
     * Build 3/Map.
     */
    public static TableMapping toTableMapping(String sourceTable, String targetTable, int sortIndex) {
        TableMapping tm = new TableMapping();
        tm.setIndex(sortIndex);
        tm.setSourceTable(sourceTable);
        tm.setTargetTable(targetTable);
        return tm;
    }

    /**
     * Pressindex RaiseBackLibraryTable GroupList（Copy that.
     */
    public static List<DatabaseMapping> sortByIndex(List<DatabaseMapping> mappings) {
        if (CollectionUtils.isEmpty(mappings)) {
            return Collections.emptyList();
        }
        return mappings.stream()
                .sorted(Comparator.comparingInt(DatabaseMapping::getIndex))
                .collect(Collectors.toList());
    }
}
