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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.realtimeetl.common.model.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Based onApache HttpClient It's easy.HTTP Tools, only sealedGET / POST。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-09-11
 */
public abstract class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * NodesInternal controlsTotalTake the key request header.
     */
    public static final String CLUSTER_TOKEN_HEADER = "X-Cluster-Token";

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 30_000;
    private static final int DEFAULT_MAX_TOTAL = 200;
    private static final int DEFAULT_MAX_PER_ROUTE = 50;
    private static final int DEFAULT_RETRY_COUNT = 3;

    private static final CloseableHttpClient CLIENT = createClient();

    private HttpClientUtil() {
    }

    /**
     * 构建节点间调用头。token 为空时返回空 Map。
     */
    public static Map<String, String> clusterTokenHeaders(String token) {
        if (StringUtil.isBlank(token)) {
            return Collections.emptyMap();
        }
        Map<String, String> headers = new LinkedHashMap<>(2);
        headers.put(CLUSTER_TOKEN_HEADER, token);
        return Collections.unmodifiableMap(headers);
    }

    public static HttpResult get(String url) throws Exception {
        return get(url, null, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    public static HttpResult get(String url,Map<String, String> headers) throws Exception {
        return get(url, headers, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    public static HttpResult get(String url, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        return get(url, null, connectTimeoutMs, readTimeoutMs);
    }

    public static HttpResult get(String url, Map<String, String> headers, int connectTimeoutMs, int readTimeoutMs)
            throws Exception {
        HttpGet request = new HttpGet(url);
        applyConfig(request, headers, connectTimeoutMs, readTimeoutMs);
        return execute(request);
    }

    public static HttpResult post(String url, String body, ContentType contentType) throws Exception {
        return post(url, body, contentType, null, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    public static HttpResult post(String url, String body, Map<String, String> headers) throws Exception {
        return post(url, body, ContentType.APPLICATION_JSON, headers, DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS);
    }

    public static HttpResult post(String url, String body, ContentType contentType, Map<String, String> headers,
                                  int connectTimeoutMs, int readTimeoutMs) throws Exception {
        HttpPost request = new HttpPost(url);
        applyConfig(request, headers, connectTimeoutMs, readTimeoutMs);
        if (body != null) {
            request.setEntity(new StringEntity(body, contentType == null
                    ? ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)
                    : contentType));
        }
        return execute(request);
    }

    /**
     * POST Binary text, response still read in text.
     *
     * @param url              Address
     * @param body             Text
     * @param contentType      ContentsType，Empty bytes
     * @param headers          Request Header
     * @param connectTimeoutMs ConnectTimeout
     * @param readTimeoutMs    Read timeout
     * @return StatusCodeandReply to Text
     * @throws Exception TransferFailed
     */
    public static HttpResult post(String url, byte[] body, ContentType contentType, Map<String, String> headers,
                                  int connectTimeoutMs, int readTimeoutMs) throws Exception {
        HttpPost request = new HttpPost(url);
        applyConfig(request, headers, connectTimeoutMs, readTimeoutMs);
        if (body != null) {
            ContentType type = contentType == null ? ContentType.APPLICATION_OCTET_STREAM : contentType;
            request.setEntity(new ByteArrayEntity(body, type));
        }
        return execute(request);
    }

    /**
     * GET Binary text. Not200 Throw{@link IOException}。
     *
     * @param url              Address
     * @param headers          Request Header
     * @param connectTimeoutMs ConnectTimeout
     * @param readTimeoutMs    Read timeout
     * @return Response bytes
     * @throws Exception TransferFailedorStatusNo, no, no.Yes 200
     */
    public static byte[] getBytes(String url, Map<String, String> headers, int connectTimeoutMs, int readTimeoutMs)
            throws Exception {
        HttpGet request = new HttpGet(url);
        applyConfig(request, headers, connectTimeoutMs, readTimeoutMs);
        try (CloseableHttpResponse response = CLIENT.execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            byte[] body = response.getEntity() == null ? new byte[0] : EntityUtils.toByteArray(response.getEntity());
            if (status != 200) {
                throw new IOException("HTTP " + status);
            }
            return body;
        }
    }

    private static void applyConfig(HttpRequestBase request, Map<String, String> headers,
                                    int connectTimeoutMs, int readTimeoutMs) {
        request.setConfig(RequestConfig.custom()
                .setConnectTimeout(Math.max(connectTimeoutMs, 0))
                .setSocketTimeout(Math.max(readTimeoutMs, 0))
                .setConnectionRequestTimeout(Math.max(connectTimeoutMs, 0))
                .build());
        if (headers == null || headers.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey() != null && header.getValue() != null) {
                request.setHeader(header.getKey(), header.getValue());
            }
        }
    }

    private static HttpResult execute(HttpUriRequest request) throws Exception {
        try (CloseableHttpResponse response = CLIENT.execute(request)) {
            int status = response.getStatusLine().getStatusCode();
            String body = response.getEntity() == null
                    ? StringUtil.EMPTY
                    : EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return new HttpResult(status, body);
        }
    }

    private static CloseableHttpClient createClient() {
        try {
            TrustStrategy trustAll = (chain, authType) -> true;
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, trustAll).build();
            SSLConnectionSocketFactory sslSocketFactory =
                    new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

            PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
            connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL);
            connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
            connectionManager.setValidateAfterInactivity(5_000);

            RequestConfig defaultConfig = RequestConfig.custom()
                    .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MS)
                    .setSocketTimeout(DEFAULT_READ_TIMEOUT_MS)
                    .setConnectionRequestTimeout(DEFAULT_CONNECT_TIMEOUT_MS)
                    .build();

            CloseableHttpClient client = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(defaultConfig)
                    // IO ErrorTry again；requestSentRetryEnabled=true Allow repeatablePOST Try again
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(DEFAULT_RETRY_COUNT, true))
                    .evictExpiredConnections()
                    .evictIdleConnections(30, TimeUnit.SECONDS)
                    .build();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> closeQuietly(client), "http-client-shutdown"));
            return client;
        } catch (Exception e) {
            throw new IllegalStateException("Initializing HtpClit failed", e);
        }
    }

    private static void closeQuietly(CloseableHttpClient client) {
        try {
            client.close();
        } catch (Exception e) {
            logger.warn("Failed to close HttpClit: }", e.getMessage());
        }
    }
}
