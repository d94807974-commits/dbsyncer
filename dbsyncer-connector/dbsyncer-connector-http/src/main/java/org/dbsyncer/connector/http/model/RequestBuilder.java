/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.http.model;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.connector.http.HttpException;
import org.dbsyncer.connector.http.enums.ContentTypeEnum;
import org.dbsyncer.connector.http.enums.HttpMethodEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpConnectorConfigureBuilder
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-02-11 00:01
 */
public final class RequestBuilder {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Client
     */
    private final CloseableHttpClient client;

    /**
     * RequestURL
     */
    private final String url;

    /**
     * Method of request
     */
    private final HttpMethodEnum method;

    /**
     * Request Header
     */
    private final Map<String, String> headers = new HashMap<>();

    /**
     * QuestionParameter
     */
    private final Map<String, String> queryParams = new HashMap<>();

    /**
     * RequestParameter（For FormsSubmit）
     */
    private final Map<String, String> bodyParams = new HashMap<>();

    /**
     * Requester(s)JSONString)
     */
    private String jsonBody;

    /**
     * Content-Type
     */
    private ContentTypeEnum contentType;

    /**
     * ConnectTimeoutTime（ms）
     */
    private Integer connectionTimeout = 30000;

    /**
     * Read timeoutTime（ms）
     */
    private Integer socketTimeout = 30000;

    /**
     * FromConnectPool AccessConnectTimeoutTime（ms）
     */
    private Integer connectionRequestTimeout = 30000;

    /**
     * Character encoding
     */
    private String charset = "UTF-8";

    /**
     * Try againNumber
     */
    private int retryTimes = 0;

    /**
     * ConstructFunctions
     *
     * @param client      Client
     * @param url         RequestURL
     * @param method      Method of request
     */
    public RequestBuilder(CloseableHttpClient client, String url, HttpMethodEnum method) {
        this.client = client;
        if (StringUtil.isBlank(url)) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        this.url = url;
        this.method = method;
    }

    /**
     * AddQuestionParameter
     *
     * @param key   ParameterFirst Name
     * @param value ParameterValue
     */
    public void addParam(String key, Object value) {
        if (key != null && value != null) {
            queryParams.put(key, String.valueOf(value));
        }
    }

    /**
     * AddMultiple queriesParameter
     *
     * @param params ParameterMap
     * @return Current Builder
     */
    public RequestBuilder addParams(Map<String, Object> params) {
        if (params != null) {
            params.forEach(this::addParam);
        }
        return this;
    }

    /**
     * AddRequestParameter（Forapplication/x-www-form-urlencoded）
     *
     * @param key   ParameterFirst Name
     * @param value ParameterValue
     */
    public void addBodyParam(String key, Object value) {
        if (key != null && value != null) {
            bodyParams.put(key, String.valueOf(value));
        }
    }

    /**
     * AddMultiple requestsParameter
     *
     * @param params ParameterMap
     */
    public void addBodyParams(Map<String, Object> params) {
        if (params != null) {
            params.forEach(this::addBodyParam);
        }
    }

    /**
     * SettingsRequest byJSON
     *
     * @param body Requested Object
     */
    public void setBodyAsJson(Object body) {
        if (body != null) {
            this.jsonBody = JsonUtil.objToJson(body);
            this.contentType = ContentTypeEnum.JSON;
        }
    }

    /**
     * SettingsRequest byJSONString
     *
     * @param jsonString JSONString
     */
    public void setBodyAsJsonString(String jsonString) {
        if (StringUtil.isNotBlank(jsonString)) {
            this.jsonBody = jsonString;
            this.contentType = ContentTypeEnum.JSON;
        }
    }

    /**
     * AddRequest Header
     *
     * @param key   HeadName
     * @param value HeadValue
     */
    public void addHeader(String key, Object value) {
        if (key != null && value != null) {
            headers.put(key, String.valueOf(value));
        }
    }

    /**
     * AddMultiple Request Headers
     *
     * @param headers Request HeaderMap
     * @return Current Builder
     */
    public RequestBuilder addHeaders(Map<String, String> headers) {
        if (headers != null) {
            headers.forEach(this::addHeader);
        }
        return this;
    }

    /**
     * Settings Content-Type
     *
     * @param contentType Content-Type
     */
    public void setContentType(ContentTypeEnum contentType) {
        if (contentType != null) {
            this.contentType = contentType;
        }
    }

    /**
     * SettingsCharacter encoding
     *
     * @param charset Character encoding
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * SettingsConnectTimeoutTime（ms）
     *
     * @param connectionTimeout ConnectTimeoutTime
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * SettingsRead timeoutTime（ms）
     *
     * @param socketTimeout Read timeoutTime
     */
    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    /**
     * SettingsFromConnectPool AccessConnectTimeoutTime（ms）
     *
     * @param connectionRequestTimeout ConnectRequest TimeoutTime
     */
    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    /**
     * SettingsTry againNumber
     *
     * @param retryTimes Try againNumber
     */
    public void setRetryTimes(int retryTimes) {
        this.retryTimes = Math.max(0, retryTimes);
    }

    /**
     * Enforcement of requests
     *
     * @return HTTPResponse
     */
    public HttpResponse execute() {
        try {
            // ICALLYURL
            String requestUrl = buildUrl();

            // Create RequestConfigure
            RequestConfig requestConfig = createRequestConfig();

            // By UndertakingTypeCreate Request
            HttpRequestBase httpRequest = createHttpRequest(requestUrl, requestConfig);

            // SettingsRequest Header
            setRequestHeaders(httpRequest);

            // SettingsRequest body (forPOST/PUT）
            if (httpRequest instanceof HttpEntityEnclosingRequestBase) {
                setRequestBody((HttpEntityEnclosingRequestBase) httpRequest);
            }

            // Execute requests (withTry again）
            CloseableHttpResponse httpResponse = executeWithRetry(httpRequest);

            // Processing responses
            HttpResponse response = handleResponse(httpResponse);

            if (!response.isSuccess()) {
                String errMsg = StringUtil.isNotBlank(response.getBody()) ? response.getBody() : "Request failed, status: " + response.getStatusCode();
                throw new HttpException(errMsg);
            }
            return response;
        } catch (Exception e) {
            logger.error("HTTP request failed: {} {}", method, url, e);
            throw new HttpException("HTTP request failed: " + e.getMessage());
        }
    }

    /**
     * Build CompleteURL（Include queriesParameter）
     *
     * @return FullURL
     */
    private String buildUrl() {
        if (queryParams.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        if (!url.contains("?")) {
            sb.append("?");
        } else {
            sb.append("&");
        }

        return buildQueryString(sb, queryParams);
    }

    /**
     * Build query string
     *
     * @param sb     StringBuilder
     * @param params ParameterMap
     * @return Query String
     */
    private String buildQueryString(StringBuilder sb, Map<String, String> params) {
        try {
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(entry.getKey(), charset));
                sb.append("=");
                sb.append(URLEncoder.encode(entry.getValue(), charset));
                first = false;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Encoding error", e);
        }
        return sb.toString();
    }

    /**
     * Create RequestConfigure
     *
     * @return RequestConfig
     */
    private RequestConfig createRequestConfig() {
        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setConnectTimeout(connectionTimeout);
        builder.setSocketTimeout(socketTimeout);
        builder.setConnectionRequestTimeout(connectionRequestTimeout);
        return builder.build();
    }

    /**
     * TermsHTTP Requested
     *
     * @param requestUrl    RequestURL
     * @param requestConfig RequestConfigure
     * @return HttpRequestBase
     */
    private HttpRequestBase createHttpRequest(String requestUrl, RequestConfig requestConfig) {
        switch (method) {
            case GET:
                HttpGet httpGet = new HttpGet(requestUrl);
                httpGet.setConfig(requestConfig);
                return httpGet;
            case POST:
                HttpPost httpPost = new HttpPost(requestUrl);
                httpPost.setConfig(requestConfig);
                return httpPost;
            case PUT:
                HttpPut httpPut = new HttpPut(requestUrl);
                httpPut.setConfig(requestConfig);
                return httpPut;
            case DELETE:
                HttpDelete httpDelete = new HttpDelete(requestUrl);
                httpDelete.setConfig(requestConfig);
                return httpDelete;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }

    /**
     * SettingsRequest Header
     *
     * @param request HTTPRequest
     */
    private void setRequestHeaders(HttpRequestBase request) {
        // SettingsDefault Request Header
        if (!headers.containsKey("User-Agent")) {
            headers.put("User-Agent", "HttpClientUtil/4.5.13");
        }
        if (!headers.containsKey("Accept")) {
            headers.put("Accept", "*/*");
        }

        // AddAll requests headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
    }

    /**
     * SettingsRequest
     *
     * @param request HTTPRequest
     * @throws UnsupportedEncodingException Unsupported EncodingError
     */
    private void setRequestBody(HttpEntityEnclosingRequestBase request) throws IOException {
        HttpEntity entity = null;

        // If there is LocatedJSON body，Use PriorityJSON
        if (StringUtil.isNotBlank(jsonBody)) {
            ContentType contentType = ContentType.create(ContentTypeEnum.JSON.getCode(), charset);
            entity = new StringEntity(jsonBody, contentType);
            request.setHeader("Content-Type", contentType.toString());
        }
        // IfSettingsYeah.bodyParams，Use FormSubmit
        else if (!bodyParams.isEmpty()) {
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            entity = new UrlEncodedFormEntity(params, charset);
        }

        if (entity != null) {
            request.setEntity(entity);
        }
    }

    /**
     * And...Try againImplementation
     *
     * @param request HTTPRequest
     * @return CloseableHttpResponse
     * @throws Exception Error
     */
    private CloseableHttpResponse executeWithRetry(HttpRequestBase request) throws Exception {
        CloseableHttpResponse httpResponse = null;
        int actualRetryTimes = 0;
        IOException lastException = null;

        while (actualRetryTimes <= retryTimes) {
            try {
                httpResponse = client.execute(request);
                break;
            } catch (SocketTimeoutException e) {
                lastException = e;
                actualRetryTimes++;
                if (actualRetryTimes > retryTimes) {
                    break;
                }
                logger.warn("Socket timeout, retrying ({}/{})...", actualRetryTimes, retryTimes);
                Thread.sleep(100); // Try againWaiting before100ms
            }
        }

        if (lastException != null && actualRetryTimes > retryTimes) {
            throw lastException;
        }

        return httpResponse;
    }

    /**
     * Processing responses
     *
     * @param httpResponse HTTPResponsex
     * @return HttpResponse
     * @throws IOException IOError
     */
    private HttpResponse handleResponse(CloseableHttpResponse httpResponse) throws IOException {
        try {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            HttpEntity entity = httpResponse.getEntity();
            String body = entity != null ? EntityUtils.toString(entity, charset) : null;
            // Consumption of physical content to ensureConnectIt can be released.
            EntityUtils.consumeQuietly(entity);
            return new HttpResponse(statusCode, body);
        } finally {
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }
}
