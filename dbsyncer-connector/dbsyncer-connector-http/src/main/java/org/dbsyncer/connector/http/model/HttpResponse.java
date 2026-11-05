/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.http.model;

/**
 * HTTP Response Object
 *
 * @author The clouds.
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
