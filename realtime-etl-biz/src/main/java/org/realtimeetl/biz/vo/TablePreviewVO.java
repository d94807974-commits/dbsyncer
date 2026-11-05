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

import org.realtimeetl.common.model.CursorPageQuery;
import org.realtimeetl.sdk.model.Table;

import java.util.List;
import java.util.Map;

/**
 * Database migrationPreview TableList VO（CursorminPage + TableTypeStatistics)
 *
 * @author Realtime ETL
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
