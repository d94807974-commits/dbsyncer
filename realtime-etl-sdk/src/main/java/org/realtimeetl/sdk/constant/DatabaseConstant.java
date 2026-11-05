package org.realtimeetl.sdk.constant;

public class DatabaseConstant {

    /**
     * dbsUnique ID number
     */
    public static final String DBS_UNIQUE_CODE = "/*dbs*/";

    // *********************************** Mysql **************************************//
    /**
     * MysqlminPageStatement
     */
    public static final String MYSQL_PAGE_SQL = " LIMIT ?,?";

    // *********************************** Oracle **************************************//
    /**
     * OracleminPageOrganisation
     */
    public static final String ORACLE_PAGE_SQL_START = "SELECT * FROM (SELECT A.*, ROWNUM RN FROM (";

    /**
     * OracleminPageOrganisation
     */
    public static final String ORACLE_PAGE_SQL_END = ") A WHERE ROWNUM <= ?) WHERE RN > ?";

    /**
     * OracleCursorminPageOrganisation
     */
    public static final String ORACLE_PAGE_CURSOR_SQL_START = "SELECT * FROM (SELECT A.* FROM (";

    /**
     * OracleCursorminPageEnd of statement
     */
    public static final String ORACLE_PAGE_CURSOR_SQL_END = ")A WHERE ROWNUM <= ?)";

    // *********************************** SqlServer **************************************//
    /**
     * SqlServerminPageStatement(2008VersionSupport)
     * <pre>
     *  select * from (
     *      select row_number() over(order by id) as sqlserver_row_id, * from (select * from my_user) s
     *  ) as a where a.sqlserver_row_id between 1 and 10
     * </pre>
     */
    public static final String SQLSERVER_PAGE_SQL = "SELECT * FROM (SELECT ROW_NUMBER() OVER(ORDER BY %s) AS SQLSERVER_ROW_ID, * FROM (%s)" + " S) A WHERE A.SQLSERVER_ROW_ID BETWEEN ? AND ?";

    // *********************************** PostgreSQL **************************************//
    /**
     * PostgreSQLminPageStatement
     */
    public static final String POSTGRESQL_PAGE_SQL = " limit ? OFFSET ?";

    // *********************************** SQLite **************************************//
    /**
     * SQLiteminPageStatement
     */
    public static final String SQLITE_PAGE_SQL = " limit ? OFFSET ?";

    // *********************************** ClickHouse **************************************//
    /**
     * ClickHouseminPageStatement
     */
    public static final String CLICKHOUSE_PAGE_SQL = " LIMIT ? OFFSET ?";
}
