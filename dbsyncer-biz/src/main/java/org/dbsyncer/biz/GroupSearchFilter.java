/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz;

import java.util.Collections;
import java.util.List;

/**
 * PressGroupFilterResourceListPar 147Result（Only supportID IN / Do not filter/ emptyResult）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-14 18:00
 */
public class GroupSearchFilter {

    /**
     * Do Not FilterAll）
     */
    public static final GroupSearchFilter NONE = new GroupSearchFilter(false, Collections.emptyList());

    /**
     * DirectBackEmptyminPage（GroupNoneMembers)
     */
    public static final GroupSearchFilter EMPTY = new GroupSearchFilter(true, Collections.emptyList());

    private final boolean emptyResult;
    private final List<String> ids;

    public GroupSearchFilter(boolean emptyResult, List<String> ids) {
        this.emptyResult = emptyResult;
        this.ids = ids == null ? Collections.emptyList() : ids;
    }

    public boolean isEmptyResult() {
        return emptyResult;
    }

    public List<String> getIds() {
        return ids;
    }

    public boolean hasIdFilter() {
        return !emptyResult && !ids.isEmpty();
    }
}
