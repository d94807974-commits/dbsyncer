/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.controller.openapi;

import com.alibaba.fastjson2.JSON;
import org.dbsyncer.biz.SystemConfigService;
import org.dbsyncer.biz.impl.ApiKeyManager;
import org.dbsyncer.biz.impl.JwtSecretManager;
import org.dbsyncer.biz.vo.RestResult;
import org.dbsyncer.common.model.OpenApiData;
import org.dbsyncer.common.model.RsaConfig;
import org.dbsyncer.common.rsa.RsaManager;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.connector.http.constant.HttpConstant;
import org.dbsyncer.parser.model.SystemConfig;
import org.dbsyncer.web.controller.BaseController;
import org.dbsyncer.web.model.OpenApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.InvocableHandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.UrlPathHelper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OpenAPI controller for external system integration.
 *
 * @author 穿云
 * @version 2.0.0
 */
@RestController
@RequestMapping("/openapi")
public class OpenApiController extends BaseController implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private JwtSecretManager jwtSecretManager;

    @Resource
    private ApiKeyManager apiKeyManager;

    @Resource
    private RsaManager rsaManager;

    @Resource
    private TimestampValidator timestampValidator;

    @Resource
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Resource
    private ApplicationContext applicationContext;

    private final Map<String, InvocableHandlerMethod> handlers = new ConcurrentHashMap<>();
    /**
     * Allowed HTTP methods per path (empty means unrestricted); used for GET/POST checks.
     */
    private final Map<String, Set<RequestMethod>> handlerAllowedMethods = new ConcurrentHashMap<>();
    private final HandlerMethodArgumentResolverComposite resolvers = new HandlerMethodArgumentResolverComposite();
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    private static final String OPENAPI_V1_PREFIX = "/openapi/v1";

    /**
     * OpenAPI v1 entry point: forwards /openapi/v1/xxx to the internal controller path /xxx.
     * <p>
     * Examples:<br>
     * - /openapi/v1/connector/test → /connector/test<br>
     * - /openapi/v1/test → /test
     * </p>
     */
    @RequestMapping(value = "/v1/**")
    public Object gateway(HttpServletRequest request, HttpServletResponse response) {
        try {
            String lookupPath = getV1LookupPath(request);
            if (lookupPath == null) {
                return OpenApiResponse.fail(OpenApiErrorCode.BAD_REQUEST, "Invalid request path");
            }
            InvocableHandlerMethod invocableMethod = handlers.get(lookupPath);
            if (invocableMethod == null) {
                logger.warn("OpenAPI v1 no matching API: {}", lookupPath);
                return OpenApiResponse.fail(OpenApiErrorCode.NOT_FOUND, "API not found: " + lookupPath);
            }

            // Validate HTTP method against the target handler allow-list
            Set<RequestMethod> allowedMethods = handlerAllowedMethods.get(lookupPath);
            if (allowedMethods != null && !allowedMethods.isEmpty()) {
                RequestMethod requestMethod = parseRequestMethod(request.getMethod());
                if (requestMethod == null || !allowedMethods.contains(requestMethod)) {
                    logger.warn("OpenAPI v1 Method Not Allowed: path={}", lookupPath);
                    return OpenApiResponse.fail(OpenApiErrorCode.METHOD_NOT_ALLOWED, "Method Not Allowed");
                }
            }

            // Decrypt request payload
            String requestBody = readRequestBody(request);
            Assert.hasText(requestBody, "Request body must not be empty");
            // Parse as OpenApiRequest (includes timestamp and nonce)
            OpenApiData apiData = JsonUtil.jsonToObj(requestBody, OpenApiData.class);
            Assert.notNull(apiData, "Request data must not be empty.");
            // Validate timestamp and nonce before decrypting to avoid wasting work
            Long timestamp = apiData.getTimestamp();
            String nonce = apiData.getNonce();
            Assert.notNull(timestamp, "Timestamp is empty.");
            Assert.hasText(nonce, "Nonce is empty.");
            Assert.isTrue(timestampValidator.validate(timestamp, nonce), "Timestamp or nonce validation failed");

            // Parse encrypted request
            RsaConfig rsaConfig = systemConfigService.getSystemConfig().getRsaConfig();
            // Detect public-network scenario
            boolean isPublicNetwork = isPublicNetwork(request);
            String decryptedData = rsaManager.decrypt(rsaConfig, apiData, isPublicNetwork);
            Object json = JSON.parse(decryptedData);
            ServletWebRequest webRequest = getServletWebRequest(request, response, (String) json);
            ModelAndViewContainer mavContainer = new ModelAndViewContainer();
            mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
            Object result = invocableMethod.invokeForRequest(webRequest, mavContainer);
            // Encrypt response
            if (result instanceof RestResult) {
                RestResult restResult = (RestResult) result;
                if (restResult.getData() != null && restResult.isSuccess()) {
                    restResult.setData(rsaManager.encrypt(rsaConfig, restResult.getData(), isPublicNetwork));
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("OpenAPI execution failed", e);
            return OpenApiResponse.fail(OpenApiErrorCode.INTERNAL_ERROR, "Request processing failed: " + e.getMessage());
        }
    }

    private static ServletWebRequest getServletWebRequest(HttpServletRequest request, HttpServletResponse response, String decryptedData) {
        DecryptRequestWrapper finalRequest = new DecryptRequestWrapper(request, decryptedData);
        return new ServletWebRequest(finalRequest, response);
    }

    @Override
    public void afterPropertiesSet() {
        // Initialize handler mapping intercept
        initHandlerMapping();
        // Register argument resolvers
        resolvers.addResolvers(requestMappingHandlerAdapter.getArgumentResolvers());
    }

    private void initHandlerMapping() {
        Set<String> parsePackage = new HashSet<>();
        parsePackage.add("/connector/");
        parsePackage.add("/mapping/");
        parsePackage.add("/tableGroup/");
        RequestMappingHandlerMapping mapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        // Collect URL-to-handler mappings
        Map<RequestMappingInfo, HandlerMethod> map = mapping.getHandlerMethods();
        map.forEach((k, v) -> {
            PatternsRequestCondition condition = k.getPatternsCondition();
            assert condition != null;
            Object[] array = condition.getPatterns().toArray();
            boolean filter;
            for (String path : parsePackage) {
                filter = false;
                // Match packages to expose
                for (Object o : array) {
                    String patternPath = (String) o;
                    if (StringUtil.startsWith(patternPath, path)) {
                        handlers.compute(patternPath, (x, y) -> {
                            if (y == null) {
                                Object bean = applicationContext.getBean(v.getBeanType());
                                InvocableHandlerMethod invocableHandlerMethod = new InvocableHandlerMethod(bean, v.getMethod());
                                invocableHandlerMethod.setHandlerMethodArgumentResolvers(resolvers);
                                return invocableHandlerMethod;
                            }
                            return y;
                        });
                        // Record allowed HTTP methods for this path (empty = any method)
                        Set<RequestMethod> methods = k.getMethodsCondition().getMethods();
                        handlerAllowedMethods.put(patternPath, methods.isEmpty() ? Collections.emptySet() : new HashSet<>(methods));
                        filter = true;
                        break;
                    }
                }
                if (filter) {
                    break;
                }
            }

        });
        parsePackage.clear();
    }

    /**
     * Login endpoint — obtain a token.
     * POST /openapi/auth/login
     *
     * @param requestBody JSON body containing the API secret
     * @return token payload
     */
    @PostMapping("/auth/login")
    public OpenApiResponse<Map<String, String>> login(@RequestBody Map<String, String> requestBody) {
        try {
            String secret = requestBody.get("secret");
            if (StringUtil.isBlank(secret)) {
                return OpenApiResponse.fail(OpenApiErrorCode.BAD_REQUEST, "secret must not be empty");
            }

            // Validate API key
            SystemConfig systemConfig = systemConfigService.getSystemConfig();
            if (!apiKeyManager.validate(systemConfig.getApiKeyConfig(), secret)) {
                logger.error("Invalid credentials {}", secret);
                return OpenApiResponse.fail(OpenApiErrorCode.UNAUTHORIZED, "Invalid credentials");
            }

            // Load JWT secret (auto-generated if missing)
            Map<String, String> data = new HashMap<>();
            data.put("token", jwtSecretManager.generateToken()); // generate token
            data.put("expires", "7200"); // 2 hours, in seconds
            return OpenApiResponse.success("Login succeeded", data);
        } catch (Exception e) {
            logger.error("Login failed", e);
            return OpenApiResponse.fail(OpenApiErrorCode.INTERNAL_ERROR, "Login failed: " + e.getMessage());
        }
    }

    /**
     * Refresh-token endpoint.
     * POST /openapi/auth/refresh
     *
     * @param request HTTP request (token already resolved by the interceptor)
     * @return new token payload
     */
    @PostMapping("/auth/refresh")
    public OpenApiResponse<Map<String, String>> refreshToken(HttpServletRequest request) {
        try {
            // Read the previous token from the request header
            String oldToken = request.getHeader("Authorization");
            if (oldToken != null && oldToken.startsWith("Bearer ")) {
                oldToken = oldToken.substring(7);
            }
            if (StringUtil.isBlank(oldToken)) {
                return OpenApiResponse.fail(OpenApiErrorCode.UNAUTHORIZED, "Token must not be empty");
            }
            // Refresh token
            String newToken = jwtSecretManager.refreshToken(oldToken);
            if (StringUtil.isBlank(newToken)) {
                return OpenApiResponse.fail(OpenApiErrorCode.BAD_REQUEST, "Token is invalid or expired");
            }

            Map<String, String> data = new HashMap<>();
            data.put("token", newToken);
            data.put("expires", "7200");
            return OpenApiResponse.success("Token refreshed successfully", data);
        } catch (Exception e) {
            logger.error("Refresh token failed", e);
            return OpenApiResponse.fail(OpenApiErrorCode.INTERNAL_ERROR, "Refresh token failed: " + e.getMessage());
        }
    }

    /**
     * Convert a method string to {@link RequestMethod}; returns null if unsupported.
     */
    private RequestMethod parseRequestMethod(String method) {
        if (StringUtil.isBlank(method)) {
            return null;
        }
        try {
            return RequestMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Resolve the path after /openapi/v1 as the internal handler lookup key (e.g. /connector/test).
     */
    private String getV1LookupPath(HttpServletRequest request) {
        String uri = urlPathHelper.getLookupPathForRequest(request);
        if (StringUtil.isBlank(uri)) {
            return null;
        }
        if (!uri.startsWith(OPENAPI_V1_PREFIX)) {
            return null;
        }
        String path = uri.substring(OPENAPI_V1_PREFIX.length());
        return path.startsWith("/") ? path : "/" + path;
    }

    /**
     * Whether this call is treated as a public-network scenario.
     */
    private boolean isPublicNetwork(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getHeader(HttpConstant.PUBLIC_NETWORK_HEADER));
    }

}
