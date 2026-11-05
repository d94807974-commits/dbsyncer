/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz.model;

/**
 * Web Console breachNodes SSO actuaireand OpenAPI TokenInfo minLeave.
 *
 * @author wuji
 * @version 1.0.0
 */
public class WebSsoTicket {

    /**
     * Username
     */
    private String username;

    /**
     * RoleEncoding (Comma)minI Linguistics.and UserInfo.roleCode Unanimously)
     */
    private String roleCode;

    /**
     * One-time notesID，It's for retrofitting.
     */
    private String jti;

    /**
     * Objective CombinedNodes host，Format{@code ip:httpPort}
     */
    private String targetHost;

    /**
     * IssuanceTime（ms）
     */
    private Long iat;

    /**
     * Expiration time（ms）
     */
    private Long exp;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getJti() {
        return jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public void setTargetHost(String targetHost) {
        this.targetHost = targetHost;
    }

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
