/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.model;

/**
 * TokenInfo
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-02-07 00:02
 */
public class TokenInfo {

    /**
     * IssuanceTime
     */
    private Long iat;

    /**
     * Valid until
     */
    private Long exp;

    public Long getIat() {
        return iat;
    }

    public void setIat(Long iat) {
        this.iat = iat;
    }

    public Long getExp() {
        return exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

}
