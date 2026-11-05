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
package org.realtimeetl.web.util;

import org.realtimeetl.common.util.StringUtil;

import javax.servlet.http.HttpServletRequest;

/**
 * HTTP Request tool.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public abstract class RequestUtil {

    /**
     * 获取客户端真实 IP（考虑反向代理）。
     *
     * @param request HTTP Request
     * @return ClientIP，NoneDuring Law ResolutionBackEmpty string
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return StringUtil.EMPTY;
        }
        String ip = firstValidIp(request.getHeader("X-Forwarded-For"));
        if (StringUtil.isNotBlank(ip)) {
            int index = ip.indexOf(',');
            return index != -1 ? ip.substring(0, index).trim() : ip.trim();
        }
        ip = firstValidIp(request.getHeader("X-Real-IP"));
        if (StringUtil.isNotBlank(ip)) {
            return ip;
        }
        ip = firstValidIp(request.getHeader("Proxy-Client-IP"));
        if (StringUtil.isNotBlank(ip)) {
            return ip;
        }
        ip = firstValidIp(request.getHeader("WL-Proxy-Client-IP"));
        if (StringUtil.isNotBlank(ip)) {
            return ip;
        }
        ip = firstValidIp(request.getHeader("HTTP_CLIENT_IP"));
        if (StringUtil.isNotBlank(ip)) {
            return ip;
        }
        ip = firstValidIp(request.getHeader("HTTP_X_FORWARDED_FOR"));
        if (StringUtil.isNotBlank(ip)) {
            return ip;
        }
        String remote = request.getRemoteAddr();
        return StringUtil.isBlank(remote) ? StringUtil.EMPTY : remote;
    }

    private static String firstValidIp(String ip) {
        if (StringUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            return StringUtil.EMPTY;
        }
        return ip.trim();
    }
}
