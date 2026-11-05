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
package org.realtimeetl.sdk.connector.database.ds;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.util.DatabaseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleDataSource implements DataSource, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Get from Cache QueueConnectNumber
    private final int MAX_PULL_TIME = 20;
    // Effective detectionTime（seconds），Default10s
    private final int VALID_TIMEOUT_SECONDS = 10;
    // CreateConnectTry againNumber
    private final int CREATE_RETRY_TIMES = 3;
    // CreateConnectTry againIntervalms）
    private final long CREATE_RETRY_INTERVAL_MS = 500;
    // AccessConnectLock
    private final ReentrantLock lock = new ReentrantLock();
    private String driverClassName;
    private String url;
    private Properties properties;
    // ConnectPool Queue
    private BlockingQueue<SimpleConnection> pool;
    // LiveConnectNumber
    private AtomicInteger activeNum;
    // Max connections
    private int maxActive;
    // ConnectValid until(ms)
    private long keepAlive;
    // YesNoYesOracleConnect
    private boolean oracleDriver;

    public SimpleDataSource(String driverClassName, String url, Properties properties, int maxActive, long keepAlive) {
        this.driverClassName = driverClassName;
        this.url = url;
        this.properties = properties;
        this.maxActive = maxActive;
        this.keepAlive = keepAlive;
        oracleDriver = StringUtil.equals(driverClassName, "oracle.jdbc.OracleDriver");
        activeNum = new AtomicInteger(0);
        pool = new LinkedBlockingQueue<>(maxActive);
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            lock.lock();
            // If CurrentConnectNumber greater than or equal toMax connections
            if (activeNum.get() >= maxActive) {
                throw new SdkException(String.format("Database connections exceeded ceiling %d,url=%s", maxActive, url));
            }
            int time = MAX_PULL_TIME;
            while (time-- > 0) {
                SimpleConnection poll = pool.poll();
                if (null == poll) {
                    return createConnection();
                }
                // ConnectNoneExpiry or expiry, DirectCloseConnect
                if (!poll.isValid(VALID_TIMEOUT_SECONDS) || isExpired(poll)) {
                    closeQuietly(poll);
                    continue;
                }
                // BackCacheConnect
                return poll;
            }

            // The bottom line. Make sure you get it.Connect
            return createConnection();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        throw new SdkException("Unsupported method.");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public void close() {
        // ClearConnectICDCloseAllConnect，Avoiding all the timeUpdateGather!
        SimpleConnection connection;
        while ((connection = pool.poll()) != null) {
            closeQuietly(connection);
        }
    }

    public void close(Connection connection) {
        if (connection instanceof SimpleConnection) {
            SimpleConnection simpleConnection = (SimpleConnection) connection;
            if (isExpired(simpleConnection) || isUnavailable(simpleConnection)) {
                closeQuietly(simpleConnection);
                return;
            }
            // RecoveryConnect；Into the pool.FailedI have to.Close，AvoidConnectOutside the pool and 2016 will not be recovered
            if (!pool.offer(simpleConnection)) {
                closeQuietly(simpleConnection);
            }
        }
    }

    /**
     * Discard Not AvailableConnect，Avoid recovery toConnectJi.
     */
    public void discard(Connection connection) {
        if (connection instanceof SimpleConnection) {
            closeQuietly((SimpleConnection) connection);
        }
    }

    /**
     * ClosePhysicsConnectAnd recover.LiveCount.
     */
    private void closeQuietly(SimpleConnection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (Exception e) {
            String msg = String.format("Closing connection failed, url=%s", url);
            logger.error(msg, e);
        } finally {
            activeNum.decrementAndGet();
        }
    }

    /**
     * ConnectYesNoExpiry
     */
    private boolean isExpired(SimpleConnection connection) {
        return connection.getActiveTime() + keepAlive < Instant.now().toEpochMilli();
    }

    private boolean isUnavailable(SimpleConnection connection) {
        try {
            return connection.isClosed() || !connection.isValid(VALID_TIMEOUT_SECONDS);
        } catch (SQLException e) {
            return true;
        }
    }

    /**
     * Create NewConnect
     */
    private SimpleConnection createConnection() {
        SQLException lastException = null;
        for (int i = 0; i < CREATE_RETRY_TIMES; i++) {
            try {
                SimpleConnection simpleConnection = new SimpleConnection(DatabaseUtil.getConnection(driverClassName, url, properties), oracleDriver);
                activeNum.incrementAndGet();
                return simpleConnection;
            } catch (SQLException e) {
                lastException = e;
                if (i < CREATE_RETRY_TIMES - 1) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(CREATE_RETRY_INTERVAL_MS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new SdkException(e);
                    }
                }
            }
        }
        throw new SdkException(lastException);
    }
}
