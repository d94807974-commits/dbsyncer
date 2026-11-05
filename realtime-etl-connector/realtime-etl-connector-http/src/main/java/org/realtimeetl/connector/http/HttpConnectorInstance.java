/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.realtimeetl.connector.http.config.HttpConfig;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * HttpConnectorExamples
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-02-02 00:01
 */
public final class HttpConnectorInstance implements ConnectorInstance<HttpConfig, CloseableHttpClient> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * ConnectPool Manager
     */
    private PoolingHttpClientConnectionManager connectionManager;

    /**
     * Http Client
     */
    private CloseableHttpClient httpClient;

    /**
     * DefaultConnectTimeoutTime（ms）
     */
    private final int DEFAULT_CONNECTION_TIMEOUT = 30000;

    /**
     * Default Read TimeoutTime（ms）
     */
    private final int DEFAULT_SO_TIMEOUT = 30000;

    /**
     * Default for each routeMax connections
     */
    private final int DEFAULT_MAX_PER_ROUTE = 50;

    /**
     * Default Max totalConnectNumber
     */
    private final int DEFAULT_MAX_TOTAL = 500;

    private HttpConfig config;

    public HttpConnectorInstance(HttpConfig config) {
        this.config = config;
        try {
            initHttpClient();
        } catch (Exception e) {
            throw new HttpException(e);
        }
    }

    /**
     * InitializeHttpClient
     */
    private void initHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        if (httpClient == null) {
            // CreateConnectPool Manager
            // 1. TermsSSL Context andSocket Plant
            SSLContextBuilder builder = new SSLContextBuilder();
            // Trust all certificates
            builder.loadTrustMaterial(null, (TrustStrategy) (x509Certificates, s) -> true);
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);

            // 2. RegistrationHTTP andHTTPS Protocol Processor
            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", new PlainConnectionSocketFactory())
                    .register("https", sslConnectionSocketFactory)
                    .build();
            connectionManager = new PoolingHttpClientConnectionManager(registry);

            // ConfigureConnectJi.
            connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL);
            connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);

            // ConfigureConnection parameters
            RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                    .setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT)
                    .setSocketTimeout(DEFAULT_SO_TIMEOUT)
                    .setConnectionRequestTimeout(DEFAULT_CONNECTION_TIMEOUT);

            // BuildHttpClient
            HttpClientBuilder httpClientBuilder = HttpClients.custom()
                    .setConnectionManager(connectionManager)
                    .setDefaultRequestConfig(requestConfigBuilder.build());

            httpClient = httpClientBuilder.build();
        }
    }

    @Override
    public String getServiceUrl() {
        return config.getUrl();
    }

    @Override
    public HttpConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(HttpConfig config) {
        this.config = config;
    }

    @Override
    public CloseableHttpClient getConnection() {
        return httpClient;
    }

    /**
     * Close HttpClient
     */
    @Override
    public void close() {
        try {
            if (httpClient != null) {
                httpClient.close();
                logger.info("HttpClient closed successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to close HttpClient", e);
        }
        try {
            if (connectionManager != null) {
                connectionManager.close();
                logger.info("ConnectionManager closed successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to close ConnectionManager", e);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}