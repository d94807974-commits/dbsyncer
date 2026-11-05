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

import org.realtimeetl.biz.enums.SafeInfoEnum;
import org.realtimeetl.common.enums.StatusEnum;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Operation logsParameterCollectionandDesensitive.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public abstract class OperLogSupport {

    public static final String ATTR_CACHED_REQUEST = "realtime-etl.operLog.cachedRequest";
    public static final String ATTR_CACHED_RESPONSE = "realtime-etl.operLog.cachedResponse";
    public static final String ATTR_START_MS = "realtime-etl.operLog.startMs";

    public static Map<String, Object> collectRequestParams(HttpServletRequest request) {
        Map<String, Object> params = new LinkedHashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (!CollectionUtils.isEmpty(parameterMap)) {
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String[] values = entry.getValue();
                if (values == null || values.length == 0) {
                    params.put(entry.getKey(), StringUtil.EMPTY);
                } else if (values.length == 1) {
                    params.put(entry.getKey(), values[0]);
                } else {
                    params.put(entry.getKey(), values);
                }
            }
        }
        if (request instanceof ContentCachingRequestWrapper && params.isEmpty()) {
            ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) request;
            String body = readBody(wrapper.getContentAsByteArray(), resolveBodyCharset(wrapper.getCharacterEncoding()));
            if (StringUtil.isNotBlank(body)) {
                Object parsed = tryParseJson(body);
                if (parsed instanceof Map) {
                    params.putAll((Map<String, Object>) parsed);
                } else {
                    params.put("body", body);
                }
            }
        }
        return sanitize(params);
    }

    public static Object collectResponseParam(ContentCachingResponseWrapper response) {
        if (response == null) {
            return Collections.emptyMap();
        }
        return sanitizeResponse(readBody(response.getContentAsByteArray(), resolveBodyCharset(response.getCharacterEncoding())));
    }

    public static int resolveStatus(Object responseParam, int httpStatus) {
        if (responseParam instanceof Map) {
            Object success = ((Map<?, ?>) responseParam).get("success");
            if (success instanceof Boolean) {
                return StatusEnum.of(Boolean.TRUE.equals(success)).getCode();
            }
        }
        return StatusEnum.of(httpStatus < 400).getCode();
    }

    public static String normalizePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (StringUtil.isNotBlank(context) && uri.startsWith(context)) {
            uri = uri.substring(context.length());
        }
        return StringUtil.isBlank(uri) ? "/" : uri;
    }

    /**
     * SequencedPasswordNo record specified
     */
    public static Map<String, Object> sanitize(Map<String, Object> source) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.emptyMap();
        }
        Map<String, Object> target = new HashMap<>(source.size());
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (SafeInfoEnum.contains(key)) {
                target.put(key, "***");
            } else if (entry.getValue() instanceof Map) {
                target.put(key, sanitize((Map<String, Object>) entry.getValue()));
            } else {
                target.put(key, entry.getValue());
            }
        }
        return target;
    }

    private static Object sanitizeResponse(String body) {
        if (StringUtil.isBlank(body)) {
            return Collections.emptyMap();
        }
        Object parsed = tryParseJson(body);
        if (parsed instanceof Map) {
            return sanitize((Map<String, Object>) parsed);
        }
        return body;
    }

    private static Object tryParseJson(String body) {
        String trimmed = body.trim();
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
            return body;
        }
        try {
            if (trimmed.startsWith("{")) {
                return JsonUtil.parseMap(trimmed);
            }
            return JsonUtil.jsonToArray(trimmed, Object.class);
        } catch (Exception e) {
            return body;
        }
    }

    private static String readBody(byte[] bytes, String encoding) {
        if (bytes == null || bytes.length == 0) {
            return StringUtil.EMPTY;
        }
        Charset charset = StandardCharsets.UTF_8;
        if (StringUtil.isNotBlank(encoding)) {
            try {
                charset = Charset.forName(encoding);
            } catch (Exception ignore) {
                // keep utf-8
            }
        }
        return new String(bytes, charset);
    }

    /**
     * Servlet Default Encoding AsISO-8859-1，andThis systemUTF-8 JSON Back when inconsistentUTF-8，We're going to have to avoid Chinese.
     */
    private static String resolveBodyCharset(String encoding) {
        if (StringUtil.isBlank(encoding) || "ISO-8859-1".equalsIgnoreCase(encoding)) {
            return StandardCharsets.UTF_8.name();
        }
        return encoding;
    }
}
