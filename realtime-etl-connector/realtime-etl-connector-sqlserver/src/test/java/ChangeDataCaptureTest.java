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
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.connector.sqlserver.cdc.Lsn;
import org.realtimeetl.connector.sqlserver.model.SqlServerChangeTable;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.sqlserver.jdbc.SQLServerException;

import com.microsoft.sqlserver.jdbc.SQLServerException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2022-05-22 22:56
 * @Link https://www.red-gate.com/simple-talk/sql/learn-sql-server/introduction-to-change-data-capture-cdc-in-sql-server-2008/
 */
public class ChangeDataCaptureTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String STATEMENTS_PLACEHOLDER = "#";
    private static final String GET_DATABASE_NAME = "SELECT db_name()";
    private static final String GET_DATABASE_VERSION = "SELECT @@VERSION AS 'SQL Server Version'";
    private static final String GET_TABLE_LIST = "SELECT NAME FROM SYS.TABLES WHERE SCHEMA_ID = SCHEMA_ID('#') AND IS_MS_SHIPPED = 0";
    private static final String IS_SERVER_AGENT_RUNNING = "EXEC master.#.xp_servicecontrol N'QUERYSTATE', N'SQLSERVERAGENT'";
    private static final String IS_DB_CDC_ENABLED = "SELECT is_cdc_enabled FROM sys.databases WHERE name = '#'";
    private static final String IS_TABLE_CDC_ENABLED = "SELECT COUNT(*) FROM sys.tables tb WHERE tb.is_tracked_by_cdc = 1 AND tb.name='#'";
    private static final String ENABLE_DB_CDC = "IF EXISTS(select 1 from sys.databases where name = '#' AND is_cdc_enabled=0) EXEC sys.sp_cdc_enable_db";
    private static final String ENABLE_TABLE_CDC = "IF EXISTS(select 1 from sys.tables where name = '#' AND is_tracked_by_cdc=0) EXEC sys.sp_cdc_enable_table @source_schema = N'%s', @source_name = N'#', @role_name = NULL, @supports_net_changes = 0";
    private static final String DISABLE_TABLE_CDC = "EXEC sys.sp_cdc_disable_table @source_schema = N'%s', @source_name = N'#', @capture_instance = 'all'";

    private static final String AT_TIME_ZONE_UTC = " AT TIME ZONE 'UTC'";
    private static final String GET_ALL_CHANGES_FOR_TABLE = "SELECT sys.fn_cdc_map_lsn_to_time([__$start_lsn])#, * FROM cdc.[fn_cdc_get_all_changes_#](?, ?, N'all update old') order by [__$start_lsn] ASC, [__$seqval] ASC, [__$operation] ASC";
    private static final String GET_TABLES_CDC_ENABLED = "EXEC sys.sp_cdc_help_change_data_capture";
    private static final String GET_MAX_LSN = "SELECT sys.fn_cdc_get_max_lsn()";
    private static final String GET_MIN_LSN = "SELECT sys.fn_cdc_get_min_lsn('#')";
    private static final String GET_INCREMENT_LSN = "SELECT sys.fn_cdc_increment_lsn(?)";

    private String realDatabaseName;
    private String getAllChangesForTable;
    private Connection connection = null;
    private Set<String> tables;
    private String schema;

    /**
     * <p>cdc.captured_columns – This succeededBackCapture ColumnsListYes.Result。</p>
     * <p>cdc.change_tables – This succeededBackAllEnableThe catch table.List。</p>
     * <p>cdc.ddl_history – 此表包含自启用捕获数据以来所有 DDL 更改的历史记录。</p>
     * <p>cdc.index_columns – This table containsandChange table relatedIndex。</p>
     * <p>cdc.lsn_time_mapping – MapLSNPart Number(Unique Serial Number ID, Increase) andTime。</p>
     * <p>cdc.fn_cdc_get_all_changes_MY_USER - Available for specific accessTimeof theEvent</p>
     * <p>sys.fn_cdc_map_time_to_lsn - TableYesNoYes Notably.tran_end_timeValueGreater than or equal to designationTimeAll right. For example, we can use this query.OKMontenegroYesNoCompleted pre-designatedTimeSubmitChanges</p>
     * <p>sys.fn_cdc_get_max_lsn</p>
     * <p>sys.sp_cdc_cleanup_change_table Interval with Default3days3, 2, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 3, 3, 4, 3, 3, 3, 4, 4, favoured, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 10, 3LogData Fact</p>
     *
     * @throws SQLException
     */
    @Test
    public void testConnect() throws SQLException, InterruptedException {
        ChangeDataCaptureTest cdc = new ChangeDataCaptureTest();
        cdc.start();

        // AccessDatabase name test
        realDatabaseName = cdc.queryAndMap(GET_DATABASE_NAME, rs->rs.getString(1));
        logger.info("Database name: }", realDatabaseName);
        // As per https://www.mssqltips.com/sqlservertip/1140/how-to-tell-what-sql-server-version-you-are-running/
        // Always beginning with 'Microsoft SQL Server NNNN' but only in case SQL Server is standalone
        String version = cdc.queryAndMap(GET_DATABASE_VERSION, rs->rs.getString(1));
        boolean supportsAtTimeZone = false;
        if (version.startsWith("Microsoft SQL Server ")) {
            supportsAtTimeZone = 2016 < Integer.valueOf(version.substring(21, 25));
        }
        logger.info("Database version: }", version);
        tables = cdc.queryAndMapList(GET_TABLE_LIST.replace(STATEMENTS_PLACEHOLDER, realDatabaseName), rs-> {
            Set<String> table = new LinkedHashSet<>();
            while (rs.next()) {
                table.add(rs.getString(1));
            }
            return table;
        });
        logger.info("All tables: }", tables);
        // AccessAgentServicesStatus Stopped. Running.
        boolean enabledServerAgent = cdc.queryAndMap(IS_SERVER_AGENT_RUNNING.replace(STATEMENTS_PLACEHOLDER, realDatabaseName), rs->"Running.".equals(rs.getString(1)));
        logger.info("Whether to start Agent service: }", enabledServerAgent);
        Assert.assertTrue("The agent server is not running", enabledServerAgent);
        boolean enabledCDC = cdc.queryAndMap(IS_DB_CDC_ENABLED.replace(STATEMENTS_PLACEHOLDER, realDatabaseName), rs->rs.getBoolean(1));
        logger.info("Whether or not to enable the CDC library [{}]:", realDatabaseName, enabledCDC);
        if (!enabledCDC) {
            cdc.execute(ENABLE_DB_CDC.replace(STATEMENTS_PLACEHOLDER, realDatabaseName));
            // make sure DB has cdc-enabled before proceeding
            TimeUnit.SECONDS.sleep(3);
        }

        // RegistrationCDCTable
        tables.forEach(table-> {
            try {
                boolean enabledTableCDC = cdc.queryAndMap(IS_TABLE_CDC_ENABLED.replace(STATEMENTS_PLACEHOLDER, table), rs->rs.getInt(1) > 0);
                logger.info("Whether to enable the CDC table [{}]: {", table, enabledTableCDC);
                if (!enabledTableCDC) {
                    cdc.execute(String.format(ENABLE_TABLE_CDC.replace(STATEMENTS_PLACEHOLDER, table), schema));
                    Lsn minLsn = cdc.queryAndMap(GET_MIN_LSN.replace(STATEMENTS_PLACEHOLDER, table), rs->new Lsn(rs.getBytes(1)));
                    logger.info("Enable CDC table [{}]: {", table, minLsn.isAvailable());
                }
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        });

        // SupportUTC
        getAllChangesForTable = GET_ALL_CHANGES_FOR_TABLE.replaceFirst(STATEMENTS_PLACEHOLDER, Matcher.quoteReplacement(supportsAtTimeZone ? AT_TIME_ZONE_UTC : ""));

        // ReadIncremental
        Set<SqlServerChangeTable> changeTables = cdc.queryAndMapList(GET_TABLES_CDC_ENABLED, rs-> {
            final Set<SqlServerChangeTable> tables = new HashSet<>();
            while (rs.next()) {
                SqlServerChangeTable changeTable = new SqlServerChangeTable(
                        // schemaName
                        rs.getString(1),
                        // tableName
                        rs.getString(2),
                        // captureInstance
                        rs.getString(3),
                        // changeTableObjectId
                        rs.getInt(4),
                        // startLsn
                        rs.getBytes(6),
                        // stopLsn
                        rs.getBytes(7),
                        // capturedColumns
                        rs.getString(15));
                logger.info(changeTable.toString());
                tables.add(changeTable);
            }
            return tables;
        });
        logger.info("Number of listening tables: } ", changeTables.size());

        if (!CollectionUtils.isEmpty(changeTables)) {
            AtomicInteger count = new AtomicInteger(0);
            Lsn lastLsn = cdc.queryAndMap(GET_MAX_LSN, rs->new Lsn(rs.getBytes(1)));

            while (true && count.getAndAdd(1) < 30) {
                Lsn stopLsn = cdc.queryAndMap(GET_MAX_LSN, rs->new Lsn(rs.getBytes(1)));
                if (!stopLsn.isAvailable()) {
                    logger.warn("No maximum LSN recorded in the database; please ensure that the SQL Server Agent is running");
                    cdc.pause();
                    continue;
                }

                if (stopLsn.compareTo(lastLsn) <= 0) {
                    cdc.pause();
                    continue;
                }

                Lsn startLsn = getIncrementLsn(cdc, lastLsn);
                changeTables.forEach(changeTable-> {
                    try {
                        final String query = getAllChangesForTable.replace(STATEMENTS_PLACEHOLDER, changeTable.getCaptureInstance());
                        logger.info("Getting changes for table {} in range[{}, {}]", changeTable.getTableName(), startLsn, stopLsn);

                        cdc.queryAndMapList(query, statement-> {
                            statement.setBytes(1, startLsn.getBinary());
                            statement.setBytes(2, stopLsn.getBinary());
                        }, rs-> {
                            int columnCount = rs.getMetaData().getColumnCount();
                            List<List<Object>> data = new ArrayList<>(columnCount);
                            List<Object> row = null;
                            while (rs.next()) {
                                row = new ArrayList<>(columnCount);
                                for (int i = 1; i <= columnCount; i++) {
                                    row.add(rs.getObject(i));
                                }
                                logger.info("rows:{}", row);
                                data.add(row);
                            }
                            return data;
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                lastLsn = stopLsn;
            }
        }

        // LogoutCDCTable
        for (String table : tables) {
            cdc.execute(String.format(DISABLE_TABLE_CDC.replace(STATEMENTS_PLACEHOLDER, table), schema));
        }
        cdc.close();
    }

    public void start() throws SQLException {
        String username = "sa";
        String password = "123";
        String url = "jdbc:sqlserver://127.0.0.1:1433;DatabaseName=test";
        schema = "dbo";
        connection = DriverManager.getConnection(url, username, password);
        if (connection != null) {
            DatabaseMetaData dm = (DatabaseMetaData) connection.getMetaData();
            logger.info("Driver name: " + dm.getDriverName());
            logger.info("Driver version: " + dm.getDriverVersion());
            logger.info("Product name: " + dm.getDatabaseProductName());
            logger.info("Product version: " + dm.getDatabaseProductVersion());
        }
    }

    public void close() {
        if (null != connection) {
            close(connection);
        }
    }

    private void close(AutoCloseable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    private Lsn getIncrementLsn(ChangeDataCaptureTest cdc, Lsn lastLsn) {
        return cdc.queryAndMap(GET_INCREMENT_LSN, statement->statement.setBytes(1, lastLsn.getBinary()), rs->Lsn.valueOf(rs.getBytes(1)));
    }

    private void pause() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
    }

    private void execute(String... sqlStatements) throws SQLException {
        Statement statement = connection.createStatement();
        try {
            for (String sqlStatement : sqlStatements) {
                if (sqlStatement != null) {
                    logger.info("executing '{}'", sqlStatement);
                    statement.execute(sqlStatement);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close(statement);
        }
    }

    public interface ResultSetMapper<T> {

        T apply(ResultSet rs) throws SQLException;
    }

    public interface StatementPreparer {

        void accept(PreparedStatement statement) throws SQLException;
    }

    public <T> T queryAndMap(String sql, ResultSetMapper<T> mapper) {
        return queryAndMap(sql, null, mapper);
    }

    public <T> T queryAndMap(String sql, StatementPreparer statementPreparer, ResultSetMapper<T> mapper) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        T apply = null;
        try {
            ps = connection.prepareStatement(sql);
            if (null != statementPreparer) {
                statementPreparer.accept(ps);
            }
            rs = ps.executeQuery();
            if (rs.next()) {
                apply = mapper.apply(rs);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close(rs);
            close(ps);
        }
        return apply;
    }

    public <T> T queryAndMapList(String sql, ResultSetMapper<T> mapper) {
        return queryAndMapList(sql, null, mapper);
    }

    public <T> T queryAndMapList(String sql, StatementPreparer statementPreparer, ResultSetMapper<T> mapper) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        T apply = null;
        try {
            ps = connection.prepareStatement(sql);
            if (null != statementPreparer) {
                statementPreparer.accept(ps);
            }
            rs = ps.executeQuery();
            apply = mapper.apply(rs);
        } catch (SQLServerException e) {
            // For process orFunctions cdc.fn_cdc_get_all_changes_ ... ProvidedInsufficient number of parameters。
            logger.warn(e.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            close(rs);
            close(ps);
        }
        return apply;
    }

}