package org.dbsyncer.sdk.config;

import org.dbsyncer.sdk.connector.database.Database;
import org.dbsyncer.sdk.model.Field;

import java.util.List;

public class SqlBuilderConfig {

    private Database database;

    // Structure Name
    private String schema;

    // Table name
    private String tableName;

    // Primary keyList
    private List<String> primaryKeys;

    // Field
    private List<Field> fields;

    // Filter Conditions
    private String queryFilter;

    public SqlBuilderConfig(Database database, String schema, String tableName, List<String> primaryKeys, List<Field> fields, String queryFilter) {
        this.database = database;
        this.schema = schema;
        this.tableName = tableName;
        this.primaryKeys = primaryKeys;
        this.fields = fields;
        this.queryFilter = queryFilter;
    }

    public Database getDatabase() {
        return database;
    }

    public String getSchema() {
        return schema;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public List<Field> getFields() {
        return fields;
    }

    public String getQueryFilter() {
        return queryFilter;
    }
}