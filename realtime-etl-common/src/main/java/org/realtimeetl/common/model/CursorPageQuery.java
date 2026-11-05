/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.common.model;

import org.realtimeetl.common.util.NumberUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * CursorminPageResult（cursor A string identification, compatible numerical offset cursor)
 *
 * @author wuji
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
