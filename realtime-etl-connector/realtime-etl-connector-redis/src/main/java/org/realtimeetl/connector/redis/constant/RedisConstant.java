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
package org.realtimeetl.connector.redis.constant;

/**
 * Redis ConnectorConstant
 *
 * @author Realtime ETL.
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
