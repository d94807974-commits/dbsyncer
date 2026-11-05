/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.security;

/**
 * Login failedCountandLock deadlineTime（Within processStatus）。
 *
 * @author wuji
 * @version 1.0.0
 */
public class LoginState {

    private final int failCount;

    /**
     * Lock deadlineTimeEmbracems）；Unlocked0
     */
    private final long lockUntilMillis;

    public LoginState(int failCount, long lockUntilMillis) {
        this.failCount = failCount;
        this.lockUntilMillis = lockUntilMillis;
    }

    public int getFailCount() {
        return failCount;
    }

    public long getLockUntilMillis() {
        return lockUntilMillis;
    }
}
