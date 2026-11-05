/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.plugin.impl;

import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.realtimeetl.common.config.AppConfig;
import org.realtimeetl.common.model.HttpResult;
import org.realtimeetl.common.util.HttpClientUtil;
import org.realtimeetl.sdk.notice.NoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNoticeService implements NoticeService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AppConfig appConfig;

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    protected void send(String url, String message) {
        try {
            HttpResult result = HttpClientUtil.post(url, message, ContentType.APPLICATION_JSON);
            if (!result.isOk()) {
                throw new IllegalArgumentException(result.getBody());
            }
            logger.info("Send message:{}, result:{}", message, result.getBody());
        } catch (HttpHostConnectException e) {
            throw new IllegalArgumentException("Network connections are abnormal and unconnected");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof HttpHostConnectException) {
                throw new IllegalArgumentException("Network connections are abnormal and unconnected");
            }
            throw new RuntimeException(e);
        }
    }
}
