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
package org.realtimeetl.biz.vo;

import org.realtimeetl.sdk.model.DatabaseMapping;
import org.realtimeetl.sdk.model.TableMapping;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LibraryMap business view:LibraryVFieldSuccession{@link DatabaseMapping}，Maps only forInterfaceReference/Reference.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-23 10:45
 */
public final class DatabaseMappingVO extends DatabaseMapping {

    /**
     * TableMapSource table -&gt; Target table），Correspondrealtime_etl_table_group，Do Not Writetask.JSON
     */
    private List<TableMapping> tableMappings = new ArrayList<>();

    public List<TableMapping> getTableMappings() {
        return tableMappings;
    }

    public void setTableMappings(List<TableMapping> tableMappings) {
        this.tableMappings = tableMappings == null ? new ArrayList<>() : tableMappings;
    }

    /**
     * Pressindex RaiseBackMapList（Copy that.
     */
    public List<TableMapping> getSortedTableMappings() {
        if (tableMappings == null || tableMappings.isEmpty()) {
            return Collections.emptyList();
        }
        return tableMappings.stream()
                .sorted(Comparator.comparingInt(TableMapping::getIndex))
                .collect(Collectors.toList());
    }

    /**
     * FromLibraryMap Field ModelCopyLibraryVField。
     */
    public static DatabaseMappingVO from(DatabaseMapping mapping) {
        DatabaseMappingVO vo = new DatabaseMappingVO();
        if (mapping != null) {
            BeanUtils.copyProperties(mapping, vo);
        }
        return vo;
    }

    /**
     * Convert to AvailableEnduringYes.LibraryMap (excluding tables). Nonetheless, there are no tables.
     */
    public DatabaseMapping toDatabaseMapping() {
        DatabaseMapping mapping = new DatabaseMapping();
        BeanUtils.copyProperties(this, mapping);
        return mapping;
    }
}
