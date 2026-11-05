/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.oracle.logminer;

import org.realtimeetl.connector.oracle.OracleException;
import org.realtimeetl.sdk.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2023-12-09 20:21
 */
public class LogMiner {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Lock lock = new ReentrantLock();
    private final String username;
    private final String password;
    private final String url;
    private final String schema;
    private final String driverClassName;
    private volatile int queryTimeout = 300;

    // DynamicFetchSize Configure
    private final int minFetchSize = 500;
    private final int maxFetchSize = 5000;
    private volatile int currentFetchSize = 1000;

    // Dynamic hibernationTimeConfigure（ms）
    private final int minSleepMillis = 100;
    private final int maxSleepMillis = 1000;
    private volatile int currentSleepMillis = 1000;

    // Query Range Control
    private volatile long currentScnRange = 20000;
    private final long MIN_SCN_RANGE = 10000;
    private final long MAX_SCN_RANGE = 100000;

    // Performance statistics
    private volatile long lastQueryRows = 0;
    private volatile long consecutiveEmptyQueries = 0;
    private volatile long totalQueriesCount = 0;
    private volatile long totalRowsProcessed = 0;
    private volatile long totalEmptyQueries = 0;
    private volatile long lastStatsTime = System.currentTimeMillis();

    private volatile boolean connected = false;
    private Connection connection;
    private List<BigInteger> currentRedoLogSequences;
    private final TransactionalBuffer transactionalBuffer = new TransactionalBuffer();
    // AlreadySubmitBits
    private Long committedScn = 0L;
    // Initial bits
    private long startScn = 0;
    // Max.SCN，It's used to avoid any additional queries.
    private long processedScn = 0;
    private EventListener listener;
    private Worker worker;
    /**
     * LogMiner SessionYesNoAlreadyStart。To avoidredo Exhaustion.START/END Resultalert.log The boom.
     */
    private volatile boolean logMinerSessionStarted = false;

    public LogMiner(String username, String password, String url, String schema, String driverClassName) {
        this.username = username;
        this.password = password;
        this.url = url;
        this.schema = schema;
        this.driverClassName = driverClassName;
    }

    public void close() {
        connected = false;
        closeQuietly();
    }

    private void closeQuietly() {
        if (isValid()) {
            LogMinerHelper.endLogMiner(connection);
        }
        if (null != worker && !worker.isInterrupted()) {
            worker.interrupt();
            worker = null;
        }
        close(connection);
    }

    public void start() throws SQLException {
        boolean locked = false;
        try {
            locked = lock.tryLock(5, TimeUnit.SECONDS);
            if (locked && !connected) {
                connected = true;
                connect();
            } else {
                logger.error("LogMiner is already started");
            }
        } catch (InterruptedException e) {
            throw new OracleException(e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    private Connection createConnection() throws SQLException {
        return DatabaseUtil.getConnection(driverClassName, url, username, password);
    }

    private Connection validateConnection() throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection(driverClassName, url, username, password);
            LogMinerHelper.setSessionParameter(conn);
            int version = conn.getMetaData().getDatabaseMajorVersion();
            // 19SupportcdbMode
            if (version == 19) {
                LogMinerHelper.setSessionContainerIfCdbMode(conn);
            }
            // Less than10Not supported
            else if (version < 10) {
                throw new IllegalArgumentException(String.format("Unsupported database version: %d(current) < 10", version));
            }
            // InspectionAccountPermissions
            LogMinerHelper.checkPermissions(conn, version);
        } catch (Exception e) {
            close(conn);
            throw e;
        }
        return conn;
    }

    private void connect() throws SQLException {
        this.connection = validateConnection();
        // Judgement ConfirmationYesNoFirst Reading
        if (startScn == 0) {
            startScn = LogMinerHelper.getCurrentScn(connection);
            restartLogMiner(startScn);
        } else {
            restartLogMiner(LogMinerHelper.getCurrentScn(connection));
        }
        logger.info("Start log miner, scn={}", startScn);
        worker = new Worker();
        worker.setName("log-miner-parser-" + url + "_" + worker.hashCode());
        worker.setDaemon(false);
        worker.start();
    }

    private void recover() {
        logger.error("Connection interrupted, attempting to reconnect");
        while (connected) {
            try {
                closeQuietly();
                connect();
                logger.info("Reconnect successfully");
                break;
            } catch (Exception e) {
                logger.error("Reconnect failed", e);
                sleepSeconds(5);
            }
        }
    }

    private void sleepSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            if (connected) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void restartLogMiner(long endScn) throws SQLException {
        LogMinerHelper.startLogMiner(connection, startScn, endScn);
        logMinerSessionStarted = true;
        currentRedoLogSequences = LogMinerHelper.getCurrentRedoLogSequences(connection);
    }

    private boolean redoLogSwitchOccurred() throws SQLException {
        final List<BigInteger> newSequences = LogMinerHelper.getCurrentRedoLogSequences(connection);
        if (!newSequences.equals(currentRedoLogSequences)) {
            currentRedoLogSequences = newSequences;
            return true;
        }
        return false;
    }

    /**
     * redo Switch Onlyadd NewLogDocumentation，Do Not RestartLogMiner Session.
     */
    private void addLogFilesIfNeeded(long endScn) throws SQLException {
        if (!logMinerSessionStarted) {
            restartLogMiner(endScn);
            return;
        }
        LogMinerHelper.addLogFiles(connection, startScn, endScn);
    }

    private void logMinerViewProcessor(ResultSet rs) throws SQLException {
        long rowCount = 0;
        while (rs.next()) {
            rowCount++;
            BigInteger scn = rs.getBigDecimal("SCN").toBigInteger();
            String tableName = rs.getString("TABLE_NAME");
            String segOwner = rs.getString("SEG_OWNER");
            int operationCode = rs.getInt("OPERATION_CODE");
            Timestamp changeTime = rs.getTimestamp("TIMESTAMP");
            String txId = rs.getString("XID");
            if (scn.longValue() > processedScn) {
                processedScn = scn.longValue();
            }
            // Commit
            if (operationCode == LogMinerHelper.LOG_MINER_OC_COMMIT) {
                // WillTransactionalBufferCurrent ServiceDML Transfer to consumer treatment
                transactionalBuffer.commit(txId, scn, committedScn);
                continue;
            }

            // Rollback
            if (operationCode == LogMinerHelper.LOG_MINER_OC_ROLLBACK) {
                // ClearTransactionalBufferCurrent Business
                if (transactionalBuffer.rollback(txId)) {
                    logger.debug("txId: {} rollback", txId);
                }
                continue;
            }

            // MISSING_SCN
            if (operationCode == LogMinerHelper.LOG_MINER_OC_MISSING_SCN) {
                logger.warn("Found MISSING_SCN");
                continue;
            }

            // DDL
            String redoSql = getRedoSQL(rs);
            if (operationCode == LogMinerHelper.LOG_MINER_OC_DDL) {
                updateCommittedScn(scn.longValue());
                listener.onEvent(new RedoEvent(scn.longValue(), operationCode, redoSql, segOwner, tableName, changeTime, txId));
                continue;
            }

            // DML
            if (operationCode == LogMinerHelper.LOG_MINER_OC_INSERT || operationCode == LogMinerHelper.LOG_MINER_OC_DELETE || operationCode == LogMinerHelper.LOG_MINER_OC_UPDATE) {
                // Internal maintenanceTransactionalBuffer，Each ArticleDMLRegistered toBufferMedium
                // By ServiceSubmitORThe rollback will decide what to do.
                if (redoSql != null) {
                    final RedoEvent event = new RedoEvent(scn.longValue(), operationCode, redoSql, segOwner, tableName, changeTime, txId);
                    // Transactional Commit Callback
                    TransactionalBuffer.CommitCallback commitCallback = (smallestScn, commitScn, counter) -> {
                        if (smallestScn == null || scn.compareTo(smallestScn) < 0) {
                            // CurrentSCN It's already done.Submit AND Less than all the beginnings in the service bufferSCN，So you can update it.offsetScn
                            startScn = scn.longValue();
                        }
                        if (counter == 0) {
                            updateCommittedScn(commitScn.longValue());
                        }
                        event.setScn(startScn < committedScn ? committedScn : startScn);
                        listener.onEvent(event);
                    };
                    // GenerateActionsUnique identification:scn + Table name + ActionsType + SQLContentshash
                    // To prevent duplication of processing resulting from repeated queries (differentity of the same subject matter)DMLCould be the same.SCN，So we can't just use it.SCNjudgement)
                    String operationId = scn + "_" + tableName + "_" + operationCode + "_" + redoSql.hashCode();
                    transactionalBuffer.registerCommitCallback(txId, scn, operationId, commitCallback);
                }
            }
        }
        // SaveNumber of lines for current round of queries for dynamic adjustments
        lastQueryRows = rowCount;
    }

    private void updateCommittedScn(long newScn) {
        committedScn = newScn > committedScn ? newScn : committedScn;
    }

    /**
     * Dynamic adjustmentsFetchSize
     * Optimizing strategy: only realFreeTime.Lower，Avoid miscalculation.
     */
    private void adjustFetchSize() {
        if (lastQueryRows == 0) {
            consecutiveEmptyQueries++;
            // Only when there are numerous and continuous empty queriesLower fetchSize
            if (consecutiveEmptyQueries > 20 && currentFetchSize > minFetchSize) {
                currentFetchSize = Math.max(minFetchSize, (int) (currentFetchSize * 0.8));
                logger.debug("Reduce fetchSize to {} after {} consecutive empty queries", currentFetchSize, consecutiveEmptyQueries);
            }
        } else {
            consecutiveEmptyQueries = 0;
            // There's data, adjusted according to the number of lines asked.
            if (lastQueryRows >= currentFetchSize * 0.9) {
                // Close to full load. Increase.fetchSize
                int newSize = Math.min(maxFetchSize, currentFetchSize * 2);
                if (newSize != currentFetchSize) {
                    currentFetchSize = newSize;
                    logger.info("Increase fetchSize to {} (utilization: {}%)", currentFetchSize, (lastQueryRows * 100 / currentFetchSize));
                }
            }
            // Removed too earlyLowerAnd the logic, the stability.fetchSize
        }
    }

    /**
     * Dynamic adjustmentsSCN Query Range
     * Self-adaptation based on backlog and query performance
     */
    private void adjustScnRange(long backlog, long queryDuration) {
        // Adjustment based on backlog
        if (backlog > MAX_SCN_RANGE * 5) {
            // Severe backlog, increasing the scope of inquiries
            long newRange = Math.min(MAX_SCN_RANGE, currentScnRange * 2);
            if (newRange != currentScnRange) {
                currentScnRange = newRange;
                logger.info("Increase SCN range to {} due to large backlog: {}", currentScnRange, backlog);
            }
        } else if (backlog < MAX_SCN_RANGE && currentScnRange > MIN_SCN_RANGE) {
            // There's not much backlog.LowerImproved real-time coverage
            long newRange = Math.max(MIN_SCN_RANGE, currentScnRange / 2);
            if (newRange != currentScnRange) {
                currentScnRange = newRange;
                logger.info("Decrease SCN range to {} (backlog: {})", currentScnRange, backlog);
            }
        }

        // Adjustment based on length of query
        if (queryDuration > 60000) {
            // Query Over1minutes，Zoom Out
            long newRange = Math.max(MIN_SCN_RANGE, (long) (currentScnRange * 0.8));
            if (newRange != currentScnRange) {
                currentScnRange = newRange;
                logger.warn("Decrease SCN range to {} due to slow query ({}ms)", currentScnRange, queryDuration);
            }
        }
    }

    /**
     * Dynamically adjusted hibernationTime
     * Flexiblely adjust the intervals of the rounds to the volume of data, balancing delays and delaysCPUOccupation
     */
    private void adjustSleepTime() {
        if (lastQueryRows == 0) {
            // NoneData, gradually increasing hibernationTime
            int newSleep = Math.min(maxSleepMillis, currentSleepMillis + 200);
            if (newSleep != currentSleepMillis) {
                currentSleepMillis = newSleep;
                logger.debug("Increase sleep time to {}ms (no data)", currentSleepMillis);
            }
        } else if (lastQueryRows >= currentFetchSize * 0.8) {
            // Close to full load. Shorter hibernation.Time，Rapid disposal of backlog
            if (currentSleepMillis > minSleepMillis) {
                currentSleepMillis = minSleepMillis;
                logger.debug("Reduce sleep time to {}ms (high load)", currentSleepMillis);
            }
        } else {
            // There's data, but not much. Moderate hibernation.
            int newSleep = Math.max(minSleepMillis, Math.min(maxSleepMillis, 500));
            if (Math.abs(newSleep - currentSleepMillis) > 100) {
                currentSleepMillis = newSleep;
                logger.debug("Adjust sleep time to {}ms (moderate load)", currentSleepMillis);
            }
        }
    }

    /**
     * Collection of performance statisticsInfo
     * EveryminutesOutput of one statistical report
     */
    private void collectStatistics(long currentScn, long queryDuration) {
        totalQueriesCount++;
        totalRowsProcessed += lastQueryRows;
        if (lastQueryRows == 0) {
            totalEmptyQueries++;
        }

        long now = System.currentTimeMillis();
        if (now - lastStatsTime > 60000) { // EveryminutesOutput once
            long avgRows = totalQueriesCount > 0 ? totalRowsProcessed / totalQueriesCount : 0;
            long emptyRate = totalQueriesCount > 0 ? totalEmptyQueries * 100 / totalQueriesCount : 0;
            long backlog = currentScn - startScn;

            logger.info("=== LogMiner Performance Stats (1min) ===");
            logger.info("Queries: {}, Rows: {}, Empty: {}", totalQueriesCount, totalRowsProcessed, totalEmptyQueries);
            logger.info("Avg rows/query: {}, Empty rate: {}%", avgRows, emptyRate);
            logger.info("Current fetchSize: {}, scnRange: {}, sleep: {}ms", currentFetchSize, currentScnRange, currentSleepMillis);
            logger.info("SCN backlog: {}, processing speed: {}/min", backlog, totalQueriesCount > 0 ? (totalRowsProcessed * 60 / totalQueriesCount) : 0);
            logger.info("Start SCN: {}, Current SCN: {}, Committed SCN: {}", startScn, currentScn, committedScn);
            logger.info("Buffer size: {}, Last query: {}ms", transactionalBuffer.isEmpty() ? 0 : "non-zero", queryDuration);
            logger.info("=========================================");

            // ResetStatistics (rolling window)
            totalQueriesCount = 0;
            totalRowsProcessed = 0;
            totalEmptyQueries = 0;
            lastStatsTime = now;
        }
    }

    private String getRedoSQL(ResultSet rs) throws SQLException {
        String redoSql = rs.getString("SQL_REDO");
        if (redoSql == null) {
            return null;
        }
        StringBuilder redoBuilder = new StringBuilder(redoSql);

        // https://docs.oracle.com/cd/B19306_01/server.102/b14237/dynviews_1154.htm#REFRN30132
        // Continuation SQL flag. Possible values are:
        // 0 = indicates SQL_REDO and SQL_UNDO is contained within the same row
        // 1 = indicates that either SQL_REDO or SQL_UNDO is greater than 4000 bytes in size and is continued in the next row returned by the view
        int csf = rs.getInt("CSF");

        while (csf == 1) {
            if (!rs.next()) {
                break;
            }
            // OnlySQL_UNDO It's a long way to go.SQL_REDO Yesnull，Ban Sustainappend(null) Writing volume"null"
            String piece = rs.getString("SQL_REDO");
            if (piece != null) {
                redoBuilder.append(piece);
            }
            csf = rs.getInt("CSF");
        }

        return redoBuilder.toString();
    }

    public void registerEventListener(EventListener listener) {
        this.listener = listener;
    }

    public long getStartScn() {
        return startScn;
    }

    public void setStartScn(long startScn) {
        this.startScn = startScn;
    }

    public interface EventListener {

        void onEvent(RedoEvent redoEvent);
    }

    public boolean isConnected() {
        return connected;
    }

    // Judgement ConfirmationConnectYesNoNormal
    private boolean isValid() {
        try {
            return connection != null && connection.isValid(queryTimeout);
        } catch (SQLException e) {
            return false;
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

    private long getNewStartScn(Long smallestUncommittedScn, long endScn) {
        long newStartScn;
        if (smallestUncommittedScn != null && committedScn > smallestUncommittedScn) {
            // 【Do you have the key protection?Submitservices, andcommittedScnIt's gone beyond the start of the business.SCN
            // IFRS:committedScn，Do you skip/59/736?SubmitFollow-up of mattersActions
            // We can only push it.processedScn+1（Maximum readSCN），You can't jump.
            if (processedScn > startScn) {
                newStartScn = processedScn + 1;
            } else {
                // No new data are available for this round.SubmitFollow-up of mattersActions
                newStartScn = startScn;
            }
        } else if (committedScn > startScn) {
            // Business.Submit，And no earlier.SubmitIt's blocked.
            // Safe ahead.committedScn
            newStartScn = committedScn;
        } else if (processedScn > startScn) {
            // The data was read in this round, but no.Submit
            // Push toprocessedScn+1，Repeat queries
            newStartScn = processedScn + 1;
        } else {
            // No data read.processedScn == startScn）
            // 【You can't just jump to it.endScn！Oracle LogMinerThere may be delays in filing data.V$LOGMNR_CONTENTS
            // Balance policy:
            // - If it's close,<10），Maybe.YesArchiving delayed. Conservative advance.
            // - If the distance is far>=10），Maybe.YesFilter ConditionsThe resulting empty window, moderate advance
            long distance = endScn - startScn;
            if (distance < 10) {
                // It's too close. It could be a delay in filing. It's only a small step forward.
                newStartScn = startScn + 1;
            } else {
                // It's far away, Acts are moderately advanced to the middle and balanced.andSecurity
                newStartScn = startScn + Math.min(distance / 2, 100);
            }
        }
        return newStartScn;
    }

    private PreparedStatement createStatement() throws SQLException {
        String minerViewQuery = LogMinerHelper.logMinerViewQuery(schema, username);
        PreparedStatement statement = connection.prepareStatement(minerViewQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        statement.setFetchDirection(ResultSet.FETCH_FORWARD);
        statement.setQueryTimeout(queryTimeout);
        return statement;
    }

    final class Worker extends Thread {

        @Override
        public void run() {
            PreparedStatement cachedStatement = null;
            ResultSet rs = null;
            try {
                cachedStatement = createStatement();
                while (!isInterrupted() && connected) {
                    // 1. InspectionConnectValidity
                    if (!isValid()) {
                        close(cachedStatement);
                        connection = createConnection();
                        cachedStatement = createStatement();
                    }

                    // CloseLast round.ResultSet（But it's reused.PreparedStatement）
                    close(rs);

                    // 2. OKQuery range (dynamic adjustments)SCN Range)
                    long currentScn = LogMinerHelper.getCurrentScn(connection);
                    long endScn = Math.min(currentScn, startScn + currentScnRange);

                    // 3. InspectionRedo Log Switch
                    if (redoLogSwitchOccurred()) {
                        logger.info("Switch to new redo log");
                        addLogFilesIfNeeded(endScn);
                    }

                    // 4. Dynamic adjustmentsBacklogOffsetSCN
                    long queryStartTime = System.currentTimeMillis();

                    long backlog;
                    try {
                        backlog = findBacklogCount(startScn, currentScn);
                        if (backlog > currentScnRange * 2) {
                            logger.warn("start SCN: {}, currentScn, {}, SCN backlog: {}, scnRange: {}, recommend increase MAX_SCN_RANGE", startScn, currentScn, backlog, currentScnRange);
                        }

                        // 5. QuestionLogMiner View (ReusePreparedStatement）
                        processedScn = startScn;

                        // 6. DynamicShit.Settings fetchSize
                        cachedStatement.setFetchSize(currentFetchSize);
                        cachedStatement.setString(1, String.valueOf(startScn));
                        cachedStatement.setString(2, String.valueOf(endScn));

                        rs = cachedStatement.executeQuery();
                        logMinerViewProcessor(rs);
                    } catch (SQLException e) {
                        if (e.getMessage().contains("ORA-00310")) {
                            logger.info("ORA-00310 restart log miner");
                            LogMinerHelper.endLogMiner(connection);
                            logMinerSessionStarted = false;
                            restartLogMiner(endScn);
                            continue;
                        } else if (e.getMessage().contains("ORA-01013")) {
                            // Query timed out, narrow the search range
                            logger.error("Query timeout after {}s, reducing SCN range", queryTimeout);
                            currentScnRange = Math.max(MIN_SCN_RANGE, currentScnRange / 2);
                            continue;
                        }
                        throw e;
                    }

                    long queryDuration = System.currentTimeMillis() - queryStartTime;

                    // 7. AdvanceSCN
                    Long smallestUncommittedScn = transactionalBuffer.getSmallestScn();
                    long newStartScn = getNewStartScn(smallestUncommittedScn, endScn);
                    if (newStartScn != startScn) {
                        startScn = newStartScn;
                    }

                    // When it appearsBacklogQuickly recommendSCN，Get the next one.SCN
                    if (currentScn - startScn > backlog * 100 || currentScn - startScn > currentScnRange * 2) {
                        newStartScn = findNextValidScn(startScn, connection);
                        if (newStartScn != startScn) {
                            startScn = newStartScn;
                        }
                    }

                    // 8. Performance statistics and dynamic adjustments
                    collectStatistics(currentScn, queryDuration);
                    adjustFetchSize();
                    adjustScnRange(backlog, queryDuration);
                    adjustSleepTime();

                    // 9. Dynamic hibernation
                    try {
                        TimeUnit.MILLISECONDS.sleep(currentSleepMillis);
                    } catch (InterruptedException e) {
                        if (connected) {
                            logger.error("Sleep interrupted: {}", e.getMessage());
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                if (connected) {
                    logger.error(e.getMessage(), e);
                    recover();
                }
            } finally {
                close(rs);
                close(cachedStatement);
            }
        }
    }

    public Long findNextValidScn(Long previousScn, Connection connection) throws SQLException {
        // AccessSQLQuery statement
        String sql = LogMinerHelper.getNextValidScnAfter();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // SettingsParameter：Import oneSCN
            pstmt.setLong(1, previousScn);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Long nextValidScn = rs.getLong("NEXT_VALID_SCN");
                    if (!rs.wasNull()) {
                        return nextValidScn;
                    }
                }
            }
        }
        return previousScn;
    }

    public Long findBacklogCount(Long startScn, Long currentScan) throws SQLException {
        // AccessSQLQuery statement
        String sql = LogMinerHelper.getBacklogCount();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // SettingsParameter：Import oneSCN
            pstmt.setLong(1, startScn);
            pstmt.setLong(2, currentScan);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("BACKLOG_COUNT");
                }
            }
        }
        return currentScan - startScn;
    }
}