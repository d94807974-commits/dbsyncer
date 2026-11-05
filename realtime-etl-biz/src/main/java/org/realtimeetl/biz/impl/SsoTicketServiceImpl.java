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
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.SsoTicketService;
import org.realtimeetl.biz.model.WebSsoTicket;
import org.realtimeetl.common.util.NetUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.common.util.UUIDUtil;
import org.realtimeetl.sdk.spi.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Web Console breachNodes SSO：Short-term bills issued, white lists verified, one-time consumption.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Service
public class SsoTicketServiceImpl implements SsoTicketService {

    private static final Logger logger = LoggerFactory.getLogger(SsoTicketServiceImpl.class);

    /**
     * InstrumentsValid until（ms）
     */
    private static final long TICKET_TTL_MS = 30_000L;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Resource
    private JwtSecretManager jwtSecretManager;

    @Resource
    private ClusterService clusterService;

    /**
     * Consumptionjti → Expiration time，It's reset.
     */
    private final Map<String, Long> usedJti = new ConcurrentHashMap<>();

    /**
     * Issuance of jumper.
     *
     * @param username Username
     * @param roleCode Role
     * @param target   Objective CombinedNodes，Recommendations{@code ip:port}；It's compatible.{@code http(s)://ip:port}
     * @return JWT Instruments
     */
    @Override
    public String issue(String username, String roleCode, String target) {
        if (StringUtil.isBlank(username) || StringUtil.isBlank(target)) {
            throw new BizException("SSO parameter incomplete");
        }
        String targetHost = resolveTargetHost(target);
        long now = System.currentTimeMillis();
        WebSsoTicket ticket = new WebSsoTicket();
        ticket.setUsername(username);
        ticket.setRoleCode(StringUtil.getIfBlank(roleCode, StringUtil.EMPTY));
        ticket.setJti(UUIDUtil.getUUID());
        ticket.setTargetHost(targetHost);
        ticket.setIat(now);
        ticket.setExp(now + TICKET_TTL_MS);
        try {
            return jwtSecretManager.signPayload(ticket);
        } catch (Exception e) {
            logger.error("Failed to issue SSO instrument", e);
            throw new BizException("Failed to issue SSO instrument");
        }
    }

    /**
     * Validation and Originally Consumed Documents (one-time).
     *
     * @param rawTicket JWT
     * @return Effective instruments;FailedBack null
     */
    @Override
    public WebSsoTicket consume(String rawTicket) {
        purgeExpiredJti();
        if (StringUtil.isBlank(rawTicket)) {
            return null;
        }
        WebSsoTicket ticket;
        try {
            ticket = jwtSecretManager.verifyPayload(rawTicket, WebSsoTicket.class);
        } catch (Exception e) {
            logger.warn("SSO instrument check exception: {", e.getMessage());
            return null;
        }
        if (ticket == null || StringUtil.isBlank(ticket.getUsername()) || StringUtil.isBlank(ticket.getJti())) {
            return null;
        }
        Long exp = ticket.getExp();
        if (exp == null || exp < System.currentTimeMillis()) {
            logger.warn("SSO instrument expired, jti=}", ticket.getJti());
            return null;
        }
        Long prev = usedJti.putIfAbsent(ticket.getJti(), exp);
        if (prev != null) {
            logger.warn("SSO Instrument Replay, jti=}", ticket.getJti());
            return null;
        }
        return ticket;
    }

    /**
     * Objective Combined URL YesNoClusterNodes。
     *
     * @param target Objective Combined URL
     * @return true verifie
     */
    @Override
    public boolean isAllowedTarget(String target) {
        String nodeId = resolveTargetHost(target);
        return clusterService.getNode(nodeId) != null;
    }

    /**
     * Regulate into{@code http(s)://ip:port[/context-path]}（Nonetail slash; protocolandPath Follower
     * {@code server.ssl.enabled}、{@code server.servlet.context-path}）。
     *
     * @param target Objective Combined URL
     * @return Root Address
     */
    @Override
    public String normalizeTargetBase(String target) {
        String host = resolveTargetHost(target);
        int idx = host.lastIndexOf(':');
        if (idx <= 0) {
            throw new BizException("Invalid target URL");
        }
        String ip = host.substring(0, idx);
        int port = Integer.parseInt(host.substring(idx + 1));
        return NetUtil.buildWebRootUrl(ip, port, sslEnabled, contextPath);
    }

    /**
     * Only relative paths are allowed to prevent open re-direction.
     *
     * @param redirect Jump Path
     * @return Secure Path
     */
    @Override
    public String sanitizeRedirect(String redirect) {
        if (StringUtil.isBlank(redirect)) {
            return "/";
        }
        String path = redirect.trim();
        if (!path.startsWith("/") || path.startsWith("//") || path.contains("://")) {
            return "/";
        }
        return path;
    }

    private String resolveTargetHost(String target) {
        if (StringUtil.isBlank(target)) {
            throw new BizException("Destination address is empty");
        }
        String trimmed = target.trim();
        try {
            URL url = new URL(trimmed.contains("://") ? trimmed : "http://" + trimmed);
            String host = url.getHost();
            int port = url.getPort();
            if (port < 0) {
                port = url.getDefaultPort();
            }
            if (StringUtil.isBlank(host) || port <= 0) {
                throw new BizException("Invalid target URL");
            }
            return host + ":" + port;
        } catch (MalformedURLException e) {
            throw new BizException("Invalid target URL");
        }
    }

    private void purgeExpiredJti() {
        long now = System.currentTimeMillis();
        usedJti.entrySet().removeIf(e -> e.getValue() == null || e.getValue() < now);
    }
}
