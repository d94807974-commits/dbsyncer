/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.web.security;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.UserProfile;
import org.realtimeetl.parser.model.SystemConfig;
import org.realtimeetl.parser.model.UserInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Login failedNumberandAccountLocked.
 * ExistsUserDropLibrary {@code realtime_etl_user.LOGIN_FAIL_COUNT / LOCK_UNTIL}，ClusterNodesTotalEnjoyment;
 * Non-existentUsernameStill using process cache limit flow (deception) to restartClear。
 *
 * @author wuji
 * @version 1.0.0
 */
@Service
public class LoginService {

    /**
     * ContinuousFailedUpper limit
     */
    public static final int MAX_ATTEMPTS = 3;

    @Resource
    private UserProfile userProfile;

    @Resource
    private SystemConfigProfile systemConfigProfile;

    /**
     * UnknownUsernameIn the processFailedCounting (Uncounted)Library）
     */
    private final ConcurrentHashMap<String, LoginState> unknownAttempts = new ConcurrentHashMap<>();

    /**
     * CurrentConfigureThe lock.ScheduledLongminutes），Illegal or non-existentConfigureBackSystem settingsDefault value。
     */
    public int getLockMinutes() {
        SystemConfig config = systemConfigProfile.getSystemConfig();
        int minutes = config == null ? 0 : config.getLoginLockMinutes();
        return minutes >= 1 && minutes <= 1440 ? minutes : new SystemConfig().getLoginLockMinutes();
    }

    /**
     * AccountYesNoLocked (expired and zero)Back false）。
     */
    public boolean isLocked(String username) {
        return resolveLockUntil(username) > System.currentTimeMillis();
    }

    /**
     * Remaining Locksminutes78 Number (upward, at least1；UnlockedBack 0）。
     */
    public long getRemainingLockMinutes(String username) {
        long lockUntil = resolveLockUntil(username);
        long now = System.currentTimeMillis();
        if (lockUntil <= now) {
            return 0L;
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(lockUntil - now);
        long rem = (lockUntil - now) % TimeUnit.MINUTES.toMillis(1);
        return rem > 0 ? minutes + 1 : Math.max(1L, minutes);
    }

    /**
     * Record it once.Login failed；Reaching the thresholdValuelock.
     */
    public LoginResult onFailure(String username) {
        int lockMinutes = getLockMinutes();
        if (StringUtil.isBlank(username)) {
            return new LoginResult(MAX_ATTEMPTS, false, lockMinutes, 0L);
        }
        Boolean locked = userProfile.recordLoginFailure(username, MAX_ATTEMPTS, getLockMillis(lockMinutes));
        if (locked == null) {
            return onUnknownFailure(username, lockMinutes);
        }
        UserInfo user = userProfile.getByUsername(username);
        int failCount = user == null ? 0 : user.getLoginFailCount();
        boolean isLocked = Boolean.TRUE.equals(locked);
        int remaining = Math.max(0, MAX_ATTEMPTS - failCount);
        long remainingLockMinutes = isLocked ? getRemainingLockMinutes(username) : 0L;
        return new LoginResult(remaining, isLocked, lockMinutes, remainingLockMinutes);
    }

    /**
     * Current LockNotice（Visited during locking periodLogin）。
     */
    public LoginResult lockedResult(String username) {
        long remaining = getRemainingLockMinutes(username);
        return new LoginResult(0, true, getLockMinutes(), remaining);
    }

    /**
     * Login succeeded，ClearFailedCountandLocked.
     */
    public void onSuccess(String username) {
        unlock(username);
    }

    /**
     * AdministratorUnlockorLogin succeededClear. Theirs.
     */
    public void unlock(String username) {
        if (StringUtil.isBlank(username)) {
            return;
        }
        if (!userProfile.updateLoginLock(username, 0, 0L)) {
            unknownAttempts.remove(username);
        }
    }

    private long getLockMillis(int lockMinutes) {
        return lockMinutes * 60 * 1000L;
    }

    private long resolveLockUntil(String username) {
        if (StringUtil.isBlank(username)) {
            return 0L;
        }
        UserInfo user = userProfile.getByUsername(username);
        if (user == null) {
            LoginState state = unknownAttempts.get(username);
            if (state == null) {
                return 0L;
            }
            long lockUntil = state.getLockUntilMillis();
            if (lockUntil <= 0) {
                return 0L;
            }
            long now = System.currentTimeMillis();
            if (now < lockUntil) {
                return lockUntil;
            }
            unknownAttempts.remove(username, state);
            return 0L;
        }
        long lockUntil = user.getLockUntil();
        if (lockUntil <= 0) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        if (now < lockUntil) {
            return lockUntil;
        }
        userProfile.updateLoginLock(username, 0, 0L);
        return 0L;
    }

    private LoginResult onUnknownFailure(String username, int lockMinutes) {
        long lockMillis = getLockMillis(lockMinutes);
        LoginState next = unknownAttempts.compute(username, (key, old) -> {
            long now = System.currentTimeMillis();
            if (old != null && old.getLockUntilMillis() > 0 && now < old.getLockUntilMillis()) {
                return old;
            }
            int failCount;
            if (old == null || (old.getLockUntilMillis() > 0 && now >= old.getLockUntilMillis())) {
                failCount = 1;
            } else {
                failCount = old.getFailCount() + 1;
            }
            long lockUntil = failCount >= MAX_ATTEMPTS ? now + lockMillis : 0L;
            return new LoginState(failCount, lockUntil);
        });
        boolean locked = next.getLockUntilMillis() > System.currentTimeMillis();
        int remaining = Math.max(0, MAX_ATTEMPTS - next.getFailCount());
        long remainingLockMinutes = locked ? getRemainingLockMinutes(username) : 0L;
        return new LoginResult(remaining, locked, lockMinutes, remainingLockMinutes);
    }
}
