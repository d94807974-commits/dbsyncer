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
package org.realtimeetl.connector.redis;

import org.realtimeetl.connector.redis.config.RedisConfig;
import org.realtimeetl.connector.redis.util.RedisUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * RedisConnectorExamples
 */
public final class RedisConnectorInstance implements ConnectorInstance<RedisConfig, JedisPool> {

    private RedisConfig config;
    private final JedisPool pool;

    public RedisConnectorInstance(RedisConfig config) {
        this.config = config;
        this.pool = RedisUtil.createPool(config);
        ping();
    }

    @Override
    public String getServiceUrl() {
        return config.getUrl();
    }

    @Override
    public RedisConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(RedisConfig config) {
        this.config = config;
    }

    @Override
    public JedisPool getConnection() {
        return pool;
    }

    public Jedis borrowJedis() {
        return pool.getResource();
    }

    public void ping() {
        try (Jedis jedis = borrowJedis()) {
            jedis.ping();
        }
    }

    @Override
    public void close() {
        if (pool != null && !pool.isClosed()) {
            pool.close();
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
