/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.vo;

import org.dbsyncer.common.model.CursorPageQuery;
import org.dbsyncer.sdk.model.Table;

import java.util.List;
import java.util.Map;

/**
 * Database migrationPreview TableList VO（CursorminPage + TableTypeStatistics)
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-15 13:46
 */
public final class TablePreviewVO extends CursorPageQuery<Table> {

    /**
     * TablesTypeQuantity statistics, e.g.TABLE/VIEW
     */
    private Map<String, Integer> typeCounts;

    /**
     * Build Preview Table CursorminPage VO
     *
     * @param data     CurrentPageTable data
     * @param total    Total number of articles
     * @param cursor   Cursor（RollminPageIdentification)
     * @param pageSize EveryPageNumber of bars
     * @return Preview TableVO
     */
    public static TablePreviewVO of(List<Table> data, long total, String cursor, int pageSize) {
        TablePreviewVO vo = new TablePreviewVO();
        vo.fill(data, total, cursor, pageSize);
        return vo;
    }

    public Map<String, Integer> getTypeCounts() {
        return typeCounts;
    }

    public void setTypeCounts(Map<String, Integer> typeCounts) {
        this.typeCounts = typeCounts;
    }
}
