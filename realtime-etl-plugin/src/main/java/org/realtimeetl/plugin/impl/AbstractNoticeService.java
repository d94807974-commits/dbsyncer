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
