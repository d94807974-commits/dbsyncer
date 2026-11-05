/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.redis.constant;

/**
 * Redis ConnectorConstant
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-06
 */
public final class RedisConstant {

    private RedisConstant() {
    }

    /** Redis Stream Name（Source table） */
    public static final String STREAM = "stream";

    /** C Facilitieskey Pre/2008/5 PrefixTarget table） */
    public static final String KEY_PREFIX = "keyPrefix";

    /** CompositePrimary keyConnect2008-2012*/
    public static final String KEY_JOINER = "keyJoiner";

    /** Data Structure */
    public static final String DATA_STRUCTURE = "dataStructure";

    /** Stream Consumer group*/
    public static final String GROUP_ID = "groupId";

    /** Stream Consumer name */
    public static final String CONSUMER_NAME = "consumerName";

    /** Expiry Policy */
    public static final String EXPIRE_TYPE = "expireType";

    /** Expiration time（seconds） */
    public static final String EXPIRE_SECONDS = "expireSeconds";

    /** Never Expiry */
    public static final String EXPIRE_NEVER = "never";

    /** DueExpiry*/
    public static final String EXPIRE = "expire";

    /** String Data Structure */
    public static final String DATA_TYPE_STRING = "String";
}
