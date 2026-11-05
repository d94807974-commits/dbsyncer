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
package org.realtimeetl.biz;

import java.util.Collections;
import java.util.List;

/**
 * PressGroupFilterResourceListPar 147Result（Only supportID IN / Do not filter/ emptyResult）。
 *
 * @author Realtime ETL
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
