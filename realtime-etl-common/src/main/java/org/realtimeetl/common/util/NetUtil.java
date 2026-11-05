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
package org.realtimeetl.common.util;

/**
 * AddressandNodes Web Root/47.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-18
 */
public abstract class NetUtil {

    private NetUtil() {
    }

    /**
     * CollapseNodes Web Root AddressNoneIt's a slash.{@code server.servlet.context-path}。
     *
     * @param ip          IP
     * @param port        Port
     * @param ssl         YesNo HTTPS（Correspond{@code server.ssl.enabled}）
     * @param contextPath Access context path, e. g.{@code /}、{@code /realtime-etl}；Empty or{@code /} Time does not spell
     * @return Like{@code http://ip:port/realtime-etl}；Space at Illegal
     */
    public static String buildWebRootUrl(String ip, int port, boolean ssl, String contextPath) {
        if (StringUtil.isBlank(ip) || port <= 0) {
            return StringUtil.EMPTY;
        }
        StringBuilder url = new StringBuilder();
        url.append(ssl ? "https://" : "http://").append(ip).append(':').append(port);
        String path = normalizeContextPath(contextPath);
        if (StringUtil.isNotBlank(path)) {
            url.append(path);
        }
        return url.toString();
    }

    /**
     * Normativecontext-path：Empty or{@code /} BackEmpty strings;NoI promise.{@code /} andNoneSlash.
     *
     * @param contextPath Original Path
     * @return Path after Normalization
     */
    public static String normalizeContextPath(String contextPath) {
        if (StringUtil.isBlank(contextPath)) {
            return StringUtil.EMPTY;
        }
        String path = contextPath.trim();
        if ("/".equals(path)) {
            return StringUtil.EMPTY;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    /**
     * Scroll application relative paths tocontext-path After{@code sendRedirect}）。
     * <p>
     * {@code sendRedirect("/x")} relative packaging root, excludingcontext-path；Obvious fusion is required.
     *
     * @param contextPath {@link javax.servlet.http.HttpServletRequest#getContextPath()}
     * @param path        Apply internal paths, e.g.{@code /login.html}、{@code /}
     * @return And...context-path Path to
     */
    public static String joinContextPath(String contextPath, String path) {
        String ctx = normalizeContextPath(contextPath);
        String p = StringUtil.isBlank(path) ? "/" : path.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        if (StringUtil.isBlank(ctx)) {
            return p;
        }
        if (p.equals(ctx) || p.startsWith(ctx + "/")) {
            return p;
        }
        return "/".equals(p) ? ctx + "/" : ctx + p;
    }

}
