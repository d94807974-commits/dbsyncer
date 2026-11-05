package org.realtimeetl.sdk.model;

import java.util.List;

public class PageSql {

    private String querySql;

    // Filter Conditions
    private String queryFilter;

    private List<String> primaryKeys;

    // Field
    private List<Field> fields;

    public PageSql(String querySql, String queryFilter, List<String> primaryKeys, List<Field> fields) {
        this.querySql = querySql;
        this.queryFilter = queryFilter;
        this.primaryKeys = primaryKeys;
        this.fields = fields;
    }

    public String getQuerySql() {
        return querySql;
    }

    public String getQueryFilter() {
        return queryFilter;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public List<Field> getFields() {
        return fields;
    }
}
