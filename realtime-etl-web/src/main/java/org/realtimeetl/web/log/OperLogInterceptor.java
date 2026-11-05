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
package org.realtimeetl.web.log;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.web.util.RequestUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Read Method{@link OperLog}，PressModuleTypeWritingOperation logs。
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Component
public class OperLogInterceptor implements HandlerInterceptor {

    @Resource
    private LogService logService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)
                && !"DELETE".equalsIgnoreCase(method) && !"PATCH".equalsIgnoreCase(method)) {
            return;
        }
        OperLog ann = ((HandlerMethod) handler).getMethodAnnotation(OperLog.class);
        if (ann == null) {
            return;
        }
        try {
            writeLog(request, response, ann);
        } catch (Exception ignore) {
            // WriteLogFailedWithout prejudice to main process
        }
    }

    private void writeLog(HttpServletRequest request, HttpServletResponse response, OperLog ann) {
        LogType logType = LogType.findByType(ann.type());
        if (logType == null) {
            logType = LogType.SystemLog.INFO;
        }

        ContentCachingRequestWrapper cachedReq = asCachedRequest(request);
        ContentCachingResponseWrapper cachedResp = asCachedResponse(request);
        HttpServletRequest paramRequest = cachedReq != null ? cachedReq : request;

        Map<String, Object> requestParam = OperLogSupport.collectRequestParams(paramRequest);
        Object responseParam = OperLogSupport.collectResponseParam(cachedResp);
        int status = OperLogSupport.resolveStatus(responseParam, response.getStatus());

        String path = OperLogSupport.normalizePath(request);
        String message = resolveMessage(ann, logType, requestParam);
        long start = request.getAttribute(OperLogSupport.ATTR_START_MS) instanceof Long
                ? (Long) request.getAttribute(OperLogSupport.ATTR_START_MS) : System.currentTimeMillis();
        long costMs = Math.max(0L, System.currentTimeMillis() - start);

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("message", message);
        detail.put("requestUri", request.getMethod() + " " + path);
        detail.put("requestParam", requestParam);
        detail.put("responseParam", responseParam);
        detail.put("costMs", costMs);

        logService.log(logType, resolveUsername(), RequestUtil.getClientIp(request), status, detail);
    }

    private String resolveMessage(OperLog ann, LogType logType, Map<String, Object> requestParam) {
        String message = ann.message();
        if (StringUtil.isBlank(message)) {
            message = logType.getMessage() + logType.getName();
        }
        Object name = requestParam.get(ConfigConstant.CONFIG_MODEL_NAME);
        if (name != null && StringUtil.isNotBlank(String.valueOf(name))) {
            return message + ":" + name;
        }
        return message;
    }

    private ContentCachingRequestWrapper asCachedRequest(HttpServletRequest request) {
        Object attr = request.getAttribute(OperLogSupport.ATTR_CACHED_REQUEST);
        if (attr instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) attr;
        }
        if (request instanceof ContentCachingRequestWrapper) {
            return (ContentCachingRequestWrapper) request;
        }
        return null;
    }

    private ContentCachingResponseWrapper asCachedResponse(HttpServletRequest request) {
        Object attr = request.getAttribute(OperLogSupport.ATTR_CACHED_RESPONSE);
        if (attr instanceof ContentCachingResponseWrapper) {
            return (ContentCachingResponseWrapper) attr;
        }
        return null;
    }

    private String resolveUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return StringUtil.EMPTY;
        }
        String name = authentication.getName();
        return "anonymousUser".equals(name) ? StringUtil.EMPTY : StringUtil.getIfBlank(name, StringUtil.EMPTY);
    }
}
