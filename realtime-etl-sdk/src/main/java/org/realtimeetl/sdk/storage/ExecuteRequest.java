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
package org.realtimeetl.sdk.storage;

/**
 * NativeRequest parameters
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public class ExecuteRequest {

    private String sql;

    private Object[] args = new Object[0];

    private int pageNum;

    private int pageSize;

    private ExecuteRequest() {
    }

    /**
     * ICALLYNoneminPagequery.
     *
     * @param sql  SQL（ParameterUse? (in position)
     * @param args TieParameter
     * @return QuestionParameter
     */
    public static ExecuteRequest of(String sql, Object... args) {
        ExecuteRequest query = new ExecuteRequest();
        query.sql = sql;
        query.args = args == null ? new Object[0] : args;
        return query;
    }

    /**
     * SettingsminPage（pageNum/pageSize Both&gt; 0 It's only effective.
     *
     * @param pageNum  PageCode (from1 Start
     * @param pageSize EveryPageNumber of bars
     * @return this
     */
    public ExecuteRequest page(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        return this;
    }

    /**
     * YesNoEnableminPage。
     *
     * @return true 表示按分页查询
     */
    public boolean isPaged() {
        return pageNum > 0 && pageSize > 0;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getArgs() {
        return args;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }
}
