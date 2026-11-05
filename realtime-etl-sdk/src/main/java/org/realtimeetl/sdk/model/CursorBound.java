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
package org.realtimeetl.sdk.model;

import org.realtimeetl.common.util.StringUtil;

/**
 * minSpeculation boundary: beginning (exclusive) to end (inclusive), andYesNoEnd of table.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-09-04
 */
public class CursorBound {

    /**
     * YesNo(c) Supporting the demarcation of the boundary by cruise criteria;false The caller handles the entire table.
     */
    private boolean supported;

    /**
     * Initial Insert
     */
    private String startCursor = "";

    /**
     * End of cursor (include);NoneLine empty string
     */
    private String endCursor = "";

    /**
     * YesNoLastPage（Behind this window.NoneMore rows)
     */
    private boolean lastPage;

    /**
     * Number of positioning key lines actually scanned in this window;0 is an empty watch orNoneFollow-up
     */
    private int actualCount;

    /**
     * No support is given to the demarcation of the cruise.
     *
     * @param startCursor Request Initial Cursor
     * @return Result
     */
    public static CursorBound unsupported(String startCursor) {
        CursorBound bound = new CursorBound();
        bound.supported = false;
        bound.startCursor = StringUtil.getIfBlank(startCursor, StringUtil.EMPTY);
        bound.endCursor = StringUtil.EMPTY;
        bound.lastPage = true;
        bound.actualCount = 0;
        return bound;
    }

    /**
     * Support for the demarcation of the boundaryResult。
     *
     * @param startCursor Turner
     * @param endCursor   End Curve
     * @param lastPage    YesNoLastPage
     * @param actualCount Lines of this Window
     * @return Result
     */
    public static CursorBound of(String startCursor, String endCursor, boolean lastPage, int actualCount) {
        CursorBound bound = new CursorBound();
        bound.supported = true;
        bound.startCursor = StringUtil.getIfBlank(startCursor, StringUtil.EMPTY);
        bound.endCursor = StringUtil.getIfBlank(endCursor, StringUtil.EMPTY);
        bound.lastPage = lastPage;
        bound.actualCount = Math.max(actualCount, 0);
        return bound;
    }

    public boolean isSupported() {
        return supported;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }

    public String getStartCursor() {
        return startCursor;
    }

    public void setStartCursor(String startCursor) {
        this.startCursor = startCursor;
    }

    public String getEndCursor() {
        return endCursor;
    }

    public void setEndCursor(String endCursor) {
        this.endCursor = endCursor;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public int getActualCount() {
        return actualCount;
    }

    public void setActualCount(int actualCount) {
        this.actualCount = actualCount;
    }
}
