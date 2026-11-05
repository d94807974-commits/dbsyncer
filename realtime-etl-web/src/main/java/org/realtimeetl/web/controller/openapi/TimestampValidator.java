/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.web.controller.openapi;

import org.realtimeetl.common.cache.CacheConstant;
import org.realtimeetl.common.cache.CacheService;
import org.realtimeetl.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * TimeWindowand Nonce Verify, prevent.OpenAPI Restart the attack.
 *
 * @author The clouds.
 * @version 2.0.0
 */
@Component
public class TimestampValidator {

    private static final Logger logger = LoggerFactory.getLogger(TimestampValidator.class);

    /**
     * DefaultTimeWindow:±5 minutes（ms）
     */
    public static final long DEFAULT_TIME_WINDOW = 5 * 60 * 1000L;

    @Resource
    private CacheService cacheService;

    /**
     * AuthenticationTimePokeYesNoIt's working.Timewindow.
     *
     * @param timestamp  RequestTimePoke
     * @param timeWindow TimeWindowms）
     * @return YesNoValid.
     */
    public boolean validateTimestamp(long timestamp, long timeWindow) {
        long now = System.currentTimeMillis();
        long diff = Math.abs(now - timestamp);
        if (diff > timeWindow) {
            logger.warn("Timetamp authentication failed, current time: { 230, request time: }, margin: {} ms, allow window: {}ms",
                    now, timestamp, diff, timeWindow);
            return false;
        }
        return true;
    }

    /**
     * AuthenticationNonce YesNoRepeat.
     *
     * @param nonce     Random number
     * @param timestamp RequestTimePoke
     * @return true Effective;false Repeat orNoneEffect
     */
    public boolean validateNonce(String nonce, long timestamp) {
        if (StringUtil.isBlank(nonce)) {
            logger.warn("Nance is empty.");
            return false;
        }
        String key = CacheConstant.OPENAPI_NONCE_PREFIX + nonce;
        if (cacheService.containsKey(key)) {
            logger.warn("Repeated Nance: {", nonce);
            return false;
        }
        // Nonce Survive.TimeandTimeWindow is consistent and waited until expiry to allow reuse
        cacheService.put(key, timestamp, DEFAULT_TIME_WINDOW);
        return true;
    }

    /**
     * AuthenticationTimeStampNonce。
     */
    public boolean validate(long timestamp, String nonce, long timeWindow) {
        if (!validateTimestamp(timestamp, timeWindow)) {
            return false;
        }
        return validateNonce(nonce, timestamp);
    }

    /**
     * AuthenticationTimeStampNonce（DefaultTimewindow).
     */
    public boolean validate(long timestamp, String nonce) {
        return validate(timestamp, nonce, DEFAULT_TIME_WINDOW);
    }

}
