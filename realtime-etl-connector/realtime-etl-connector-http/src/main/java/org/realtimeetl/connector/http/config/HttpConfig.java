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
package org.realtimeetl.connector.http.config;

import org.realtimeetl.connector.http.util.HttpUtil;
import org.realtimeetl.sdk.model.ConnectorConfig;

/**
 * HttpConnectConfigure
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-02-02 00:01
 */
public class HttpConfig extends ConnectorConfig {

    private String url;

    private boolean enableEncrypt;

    private boolean publicNetwork;

    @Override
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isEnableEncrypt() {
        return enableEncrypt;
    }

    public void setEnableEncrypt(boolean enableEncrypt) {
        this.enableEncrypt = enableEncrypt;
    }

    public boolean isPublicNetwork() {
        return publicNetwork;
    }

    public void setPublicNetwork(boolean publicNetwork) {
        this.publicNetwork = publicNetwork;
    }

    @Override
    public String getPropertiesText() {
        return HttpUtil.toString(getProperties());
    }
}