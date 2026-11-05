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
package org.realtimeetl.common.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Local cache service (syntax alignment)Redis TTL），Here.Spring Bean The way it is injected.
 * <p>
 * Multi-businessTotalFor the same example, please use{@link CacheConstant} Prefix isolationkey。
 * <p>
 * LockandCache DataminTable storage,lockKey Yeah.andC Facilitieskey Same, different.Overwrite。
 *
 * @author Realtime ETL.
 * @version 1.0.0
 */
public interface CacheService {

    /**
     * Key does not exist ( Alignment)Redis TTL）
     */
    long TTL_NOT_EXISTS = -2L;

    /**
     * Key exists butNever Expiry（AlignmentRedis TTL）
     */
    long TTL_NO_EXPIRE = -1L;

    /**
     * Write 63/2s AfterwardsNever Expiry）。
     */
    void put(String key, Object value);

    /**
     * Write andSettingsExpiryms；{@code expireTimeMs <= 0} OrganisationNever Expiry。
     */
    void put(String key, Object value, long expireTimeMs);

    /**
     * Write andSettingsExpiration time。
     */
    void put(String key, Object value, long expireTime, TimeUnit unit);

    /**
     * AccessValue；Existing orExpiredBack null。
     */
    Object get(String key);

    /**
     * Get and convert to a specificType；TypeDo not matchBack null。
     */
    <T> T get(String key, Class<T> type);

    /**
     * OnlyRefresh TTL；Key does not exist orExpiredBack false。
     */
    boolean expire(String key, long expireTimeMs);

    /**
     * OnlyRefresh TTL。
     */
    boolean expire(String key, long expireTime, TimeUnit unit);

    /**
     * Survivors.Time（ms）：does not exist{@link #TTL_NOT_EXISTS}；Never Expiry {@link #TTL_NO_EXPIRE}；Nothen{@code >= 0}。
     */
    long ttl(String key);

    /**
     * RemoveExpiration time，ToNever Expiry。
     */
    boolean persist(String key);

    /**
     * Deletekey,BackAppealValue。
     */
    Object remove(String key);

    /**
     * KeysYesNoExistence and not expired.
     */
    boolean containsKey(String key);

    /**
     * ClearAll。
     */
    void clear();

    /**
     * Current number of entries (possibly containing expired items that Openings).
     */
    int size();

    /**
     * PresslockKey Blocked by locking (re-entry).
     */
    void lock(String lockKey);

    /**
     * Try locking it up. Now.Back。
     *
     * @return YesNoAccessSuccess
     */
    boolean tryLock(String lockKey);

    /**
     * OvertimeTimeTry locking inside.
     *
     * @return YesNoAccessSuccess；Waiting for break mark to resume andBack false
     */
    boolean tryLock(String lockKey, long waitTime, TimeUnit unit);

    /**
     * Release CurrentThreadsholding lock; ignored when not held.
     */
    void unlock(String lockKey);

    /**
     * Technically.Unlock。
     */
    default void executeWithLock(String lockKey, Runnable action) {
        lock(lockKey);
        try {
            action.run();
        } finally {
            unlock(lockKey);
        }
    }

    /**
     * Lock execution andBackResult，Auto after completionUnlock。
     */
    default <T> T executeWithLock(String lockKey, Supplier<T> action) {
        lock(lockKey);
        try {
            return action.get();
        } finally {
            unlock(lockKey);
        }
    }
}
