/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.controller.sso;

import org.dbsyncer.biz.BizException;
import org.dbsyncer.biz.PermissionService;
import org.dbsyncer.biz.SsoTicketService;
import org.dbsyncer.biz.model.WebSsoTicket;
import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.common.util.NetUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.web.config.WebAppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * CrossNodes Web SSO：AlreadyLoginNodesissuing documents,Objective CombinedNodesCreate local after consumptionSession。
 *
 * @author wuji
 * @version 1.0.0
 */
@Controller
@RequestMapping("/sso")
public class SsoController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SsoTicketService ssoTicketService;

    @Resource
    private PermissionService permissionService;

    /**
     * AlreadyLoginUserJump toObjective CombinedNodes（Carrying a short-term note Responses
     *
     * @param target   Objective CombinedNodes，Format{@code ip:port}（Don't.And...http://，NothenIt will.Firewall.Cut.）
     * @param redirect Objective CombinedRelative path, default /
     * @param response Response
     */
    @GetMapping("/redirect")
    public void redirect(@RequestParam("target") String target,
                         @RequestParam(value = "redirect", required = false, defaultValue = "/") String redirect,
                         HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || StringUtil.equals("anonymousUser", String.valueOf(authentication.getPrincipal()))) {
            sendLocalRedirect(request, response, WebAppConfig.LOGIN_PAGE);
            return;
        }
        try {
            if (!ssoTicketService.isAllowedTarget(target)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Target Multiple is not on the White List.");
                return;
            }
            String username = authentication.getName();
            String roleCode = joinAuthorities(authentication.getAuthorities());
            String ticket = ssoTicketService.issue(username, roleCode, target);
            String safeRedirect = ssoTicketService.sanitizeRedirect(redirect);
            // base IncludedObjective CombinedNodes context-path（andHere.server.servlet.context-path Unanimously)
            String base = ssoTicketService.normalizeTargetBase(target);
            String consumeUrl = base + "/sso/consume?ticket="
                    + URLEncoder.encode(ticket, StandardCharsets.UTF_8.name())
                    + "&redirect=" + URLEncoder.encode(safeRedirect, StandardCharsets.UTF_8.name());
            response.sendRedirect(consumeUrl);
        } catch (BizException e) {
            logger.warn("SSO direct failed: }", e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("SSO direct", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SSO Jump Failed");
        }
    }

    /**
     * Objective CombinedNodesConsumer bills and establishment of localLoginState.
     *
     * @param ticket   JWT Instruments
     * @param redirect Relative Path
     * @param request  Request
     * @param response Response
     */
    @GetMapping("/consume")
    public void consume(@RequestParam("ticket") String ticket,
                        @RequestParam(value = "redirect", required = false, defaultValue = "/") String redirect,
                        HttpServletRequest request,
                        HttpServletResponse response) throws IOException {
        WebSsoTicket ssoTicket = ssoTicketService.consume(ticket);
        if (ssoTicket == null) {
            sendLocalRedirect(request, response, WebAppConfig.LOGIN_PAGE);
            return;
        }
        establishSession(request, ssoTicket);
        sendLocalRedirect(request, response, ssoTicketService.sanitizeRedirect(redirect));
    }

    /**
     * Re-direction of the machine relative path, automatic fusion{@code request.getContextPath()}。
     */
    private void sendLocalRedirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(response.encodeRedirectURL(NetUtil.joinContextPath(request.getContextPath(), path)));
    }

    private void establishSession(HttpServletRequest request, WebSsoTicket ssoTicket) {
        // Objective CombinedNodesPress LocalRBAC ReloadPermissions，Avoid old papers.roleCode / ExpiryPermissionsCode
        String authorityString = permissionService.buildAuthorityString(ssoTicket.getUsername());
        if (StringUtil.isBlank(authorityString)) {
            authorityString = StringUtil.getIfBlank(ssoTicket.getRoleCode(), StringUtil.EMPTY);
        }
        Collection<? extends GrantedAuthority> authorities =
                AuthorityUtils.commaSeparatedStringToAuthorityList(authorityString);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(ssoTicket.getUsername(), null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        logger.info("POLICY: user=}, session=}", ssoTicket.getUsername(), session.getId());
    }

    private String joinAuthorities(Collection<? extends GrantedAuthority> authorities) {
        if (CollectionUtils.isEmpty(authorities)) {
            return StringUtil.EMPTY;
        }
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));
    }
}
