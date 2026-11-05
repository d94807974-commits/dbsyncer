/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.vo;

import org.dbsyncer.sdk.model.DatabaseMapping;
import org.dbsyncer.sdk.model.TableMapping;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LibraryMap business view:LibraryVFieldSuccession{@link DatabaseMapping}，Maps only forInterfaceReference/Reference.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-23 10:45
 */
public final class DatabaseMappingVO extends DatabaseMapping {

    /**
     * TableMapSource table -&gt; Target table），Corresponddbsyncer_table_group，Do Not Writetask.JSON
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
