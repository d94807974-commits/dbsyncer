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
package org.realtimeetl.common.model;

import org.realtimeetl.common.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * CursorminPageResult（cursor A string identification, compatible numerical offset cursor)
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-15 13:46
 */
public class CursorPageQuery<T> {

    private long total;
    /**
     * Cur 180aise (digital offsetable or business tour string)
     */
    private String cursor;
    private int pageSize;
    private boolean hasMore;
    private List<T> data = new ArrayList<>();

    public CursorPageQuery() {
    }

    /**
     * FillminPageFieldAnd calculatehasMore（ParameterIt's guaranteed by the caller.Default valueUnderground
     *
     * @param data     CurrentPageData Fact
     * @param total    Total number of articles
     * @param cursor   Cursor
     * @param pageSize EveryPageNumber of bars
     */
    protected void fill(List<T> data, long total, String cursor, int pageSize) {
        this.data = data;
        this.total = total;
        this.cursor = cursor;
        this.pageSize = pageSize;
        long from = Math.min(NumberUtil.toLong(cursor, 0L), this.total);
        long to = Math.min(from + this.pageSize, this.total);
        this.hasMore = to < this.total;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
