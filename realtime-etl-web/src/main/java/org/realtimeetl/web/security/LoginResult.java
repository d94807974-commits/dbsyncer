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
package org.realtimeetl.web.security;

/**
 * Once.Login failedCount laterandLockStatus，Used for the assemblyNoticeThe case.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public final class LoginResult {

    private final int remainingAttempts;
    private final boolean locked;
    private final int lockMinutes;
    private final long remainingLockMinutes;

    public LoginResult(int remainingAttempts, boolean locked,
                       int lockMinutes, long remainingLockMinutes) {
        this.remainingAttempts = remainingAttempts;
        this.locked = locked;
        this.lockMinutes = lockMinutes;
        this.remainingLockMinutes = remainingLockMinutes;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public int getLockMinutes() {
        return lockMinutes;
    }

    public long getRemainingLockMinutes() {
        return remainingLockMinutes;
    }

    /**
     * PasswordErrorAnd not yet locked.
     */
    public String badCredentialsMessage() {
        return String.format("Account or password error, %d opportunities remaining; %d consecutive failures will lock %d minutes", remainingAttempts, LoginService.MAX_ATTEMPTS, lockMinutes);
    }

    /**
     * This time.Failedis then triggered or is already in the locking period.
     */
    public String lockedMessage() {
        long minutes = Math.max(1L, remainingLockMinutes);
        return String.format("Account 380, %d minutes left, please try again later or contact the administrator to unlock", minutes);
    }
}
