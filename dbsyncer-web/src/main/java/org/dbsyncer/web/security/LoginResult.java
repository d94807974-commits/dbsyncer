/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.security;

/**
 * Once.Login failedCount laterandLockStatus，Used for the assemblyNoticeThe case.
 *
 * @author wuji
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
