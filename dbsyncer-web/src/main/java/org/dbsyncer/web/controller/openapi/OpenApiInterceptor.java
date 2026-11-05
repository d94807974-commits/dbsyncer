/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.controller.openapi;

import org.dbsyncer.biz.SystemConfigService;
import org.dbsyncer.biz.impl.IpWhitelistManager;
import org.dbsyncer.biz.impl.JwtSecretManager;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.manager.impl.PreloadTemplate;
import org.dbsyncer.parser.model.SystemConfig;
import org.dbsyncer.web.model.OpenApiResponse;
import org.dbsyncer.web.util.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * OpenAPIInterceptor
 * ProcessingtokenAuthentication and encryption decryption
 * 
 * @author The clouds.
 * @version 1.0.0
 */
@Component
public class OpenApiInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(OpenApiInterceptor.class);
    
    // TokenRequest HeaderName
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private JwtSecretManager jwtSecretManager;

    @Resource
    private IpWhitelistManager ipWhitelistManager;

    @Resource
    private PreloadTemplate preloadTemplate;

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        try {
            // 1. ConfigureYesNoOn
            if (!preloadTemplate.isPreloadCompleted()) {
                writeErrorResponse(response, OpenApiErrorCode.SERVICE_UNAVAILABLE, "Service not available for the time being");
                return false;
            }
            SystemConfig systemConfig = systemConfigService.getSystemConfig();
            if (!systemConfig.isEnableOpenAPI()) {
                writeErrorResponse(response, OpenApiErrorCode.NOT_FOUND, "Unopen API");
                return false;
            }

            // 2. AuthenticationIP whitelist（Prioritization, AvoidNoneConsumption on requestResource）
            // AllOpenAPIInterfaceOther OrganiserIP whitelist，IncludingLoginInterface
            String clientIp = RequestUtil.getClientIp(request);
            // Fetch Request Path, AvoidUserRequest/.;That kind of characters go around.PermissionsThe judgment leads to a circumvention.PermissionsCheck risk.
            String requestPath = urlPathHelper.getLookupPathForRequest(request);
            if (!ipWhitelistManager.isAllowed(systemConfig.getIpWhitelistConfig(), clientIp)) {
                logger.warn(") Not in white list, denied access", clientIp, requestPath);
                writeErrorResponse(response, OpenApiErrorCode.FORBIDDEN, "The IP address is not on the white list.");
                return false;
            }

            // 3. Exclude authenticationInterface（Login、Refreshtoken）
            if (isAuthEndpoint(requestPath)) {
                return true;
            }
            
            // 4. AuthenticationToken
            String token = extractToken(request);
            if (!jwtSecretManager.verifyToken(token)) {
                writeErrorResponse(response, OpenApiErrorCode.UNAUTHORIZED, "Token is invalid or expired");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("OpenAPI interceptor failed.", e);
            writeErrorResponse(response, OpenApiErrorCode.INTERNAL_ERROR, "Request failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Judgement ConfirmationYesNoAs AuthenticationInterface
     */
    private boolean isAuthEndpoint(String path) {
        return path.endsWith("/openapi/auth/login") || path.endsWith("/openapi/auth/refresh");
    }

    /**
     * Extract from requestToken
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader(TOKEN_HEADER);
        if (StringUtil.isNotBlank(authHeader) && authHeader.startsWith(TOKEN_PREFIX)) {
            return authHeader.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * WritingErrorResponse
     */
    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        
        OpenApiResponse<Object> errorResponse = OpenApiResponse.fail(code, message);
        String json = JsonUtil.objToJson(errorResponse);
        
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
