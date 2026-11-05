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
package org.realtimeetl.parser.util;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.sdk.model.DatabaseMapping;
import org.realtimeetl.sdk.model.TableMapping;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Database migration：LibraryTable Groupand table_group ComplementaryShh..
 *
 * @author Realtime ETL.
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
