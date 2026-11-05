/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.redis;

/**
 * RedisConnectorError
 */
public class RedisException extends RuntimeException {

    public RedisException(String message) {
        super(message);
    }

    public RedisException(Throwable cause) {
        super(cause);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }
}
