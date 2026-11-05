/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.controller.cluster;

import org.dbsyncer.biz.vo.RestResult;
import org.dbsyncer.common.util.HttpClientUtil;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.manager.impl.PreloadTemplate;
import org.dbsyncer.parser.SystemConfigProfile;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Verify INTRODUCTIONNodesMediumTotalTake the key, protect it.{@code /cluster/internal/**}。
 * <p>
 * Request to carry head{@link HttpClientUtil#CLUSTER_TOKEN_HEADER}。
 * {@code /cluster/ping} Only live detection, not within this filter.
 * <p>
 * JurisdictionSuccessNot Later{@code SecurityContext}，AvoidSpring Security YesNone Cookie Yes.NodesSwitch CreateHttpSession。
 *
 * @author wuji
 * @version 1.0.0
 */
public class ClusterInternalAuthFilter extends OncePerRequestFilter {

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private PreloadTemplate preloadTemplate;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Fetch Request Path, AvoidUserRequest/.;That kind of characters go around.PermissionsThe judgment leads to a circumvention.PermissionsCheck risk.
        String path = urlPathHelper.getLookupPathForRequest(request);
        if (StringUtil.isNotBlank(path)) {
            return !path.startsWith("/cluster/internal/");
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 1. ConfigureYesNoOn
        if (!preloadTemplate.isPreloadCompleted()) {
            return;
        }
        String clusterSecret = systemConfigProfile.getSystemConfig().getClusterSecret();
        if (StringUtil.isBlank(clusterSecret)) {
            reject(response, HttpServletResponse.SC_FORBIDDEN, "Cluster internal interface is not configured, access denied");
            return;
        }
        String provided = request.getHeader(HttpClientUtil.CLUSTER_TOKEN_HEADER);
        if (!matches(clusterSecret, provided)) {
            reject(response, HttpServletResponse.SC_UNAUTHORIZED, "The internal interface failed");
            return;
        }
        // Verify onlyTotaltaking keys;paths are already in placeSecurity ConfigureMediumpermitAll，Do Not WriteSecurityContext，NoConcerned about the situation in the country.
        filterChain.doFilter(request, response);
    }

    /**
     * ConstantTimeCompare, avoid shorttoken Total bytes detected.
     *
     * @param expected ConfigureYes.TotalTake the key
     * @param provided Request Header
     * @return YesNoConsistent
     */
    private boolean matches(String expected, String provided) {
        if (expected == null || provided == null) {
            return false;
        }
        byte[] left = expected.getBytes(StandardCharsets.UTF_8);
        byte[] right = provided.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }

    private void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(status);
        try (PrintWriter out = response.getWriter()) {
            out.write(JsonUtil.objToJson(RestResult.restFail(message, status)));
            out.flush();
        }
    }
}
