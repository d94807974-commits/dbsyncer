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
package org.realtimeetl.biz.model;

/**
 * Web Console breachNodes SSO actuaireand OpenAPI TokenInfo minLeave.
 *
 * @author Realtime ETL
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
