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
package org.realtimeetl.common.model;

import org.realtimeetl.common.util.StringUtil;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-09-12 13:43
 */
public class HttpResult {

    private final int statusCode;
    private final String body;

    public HttpResult(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body == null ? StringUtil.EMPTY : body;
    }

    /**
     * @return HTTP StatusCodeYesNoYes200
     */
    public boolean isOk() {
        return statusCode == 200;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
