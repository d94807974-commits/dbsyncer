/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.sdk.storage;

/**
 * NativeRequest parameters
 *
 * @author wuji
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
     * @return true Other OrganiserminPageQuestion
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
