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
package org.realtimeetl.connector.http.model;

/**
 * HTTP Response Object
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-02-10 00:01
 */
public final class HttpResponse {

    /**
     * ResponseStatusCode
     */
    private int statusCode;

    /**
     * Response content
     */
    private String body;

    /**
     * YesNoSuccess
     */
    private boolean success;

    /**
     * ConstructFunctions
     *
     * @param statusCode StatusCode
     * @param body       Response
     */
    public HttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
        this.success = (statusCode >= 200 && statusCode < 300);
    }

    /**
     * AccessStatusCode
     *
     * @return StatusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * SettingsStatusCode
     *
     * @param statusCode StatusCode
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Get Response
     *
     * @return Response
     */
    public String getBody() {
        return body;
    }

    /**
     * SettingsResponse
     *
     * @param body Response
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * YesNoSuccess
     *
     * @return YesNoSuccess
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * SettingsYesNoSuccess
     *
     * @param success YesNoSuccess
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

}
