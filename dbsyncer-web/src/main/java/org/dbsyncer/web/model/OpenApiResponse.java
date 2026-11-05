/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.model;

/**
 * OpenAPIHarmonized Response Format
 *
 * @author The clouds.
 * @version 1.0.0
 */
public class OpenApiResponse<T> {

    /**
     * YesNoSuccess
     */
    private Boolean success;

    /**
     * Response code
     */
    private Integer code;

    /**
     * Reply Message
     */
    private String message;

    /**
     * Response Data
     */
    private T data;

    /**
     * TimePoke
     */
    private Long timestamp;

    public OpenApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }

    public OpenApiResponse(Boolean success, Integer code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * SuccessResponse
     */
    public static <T> OpenApiResponse<T> success(T data) {
        return new OpenApiResponse<>(true, 200, "success", data);
    }

    /**
     * SuccessResponse (with message)
     */
    public static <T> OpenApiResponse<T> success(String message, T data) {
        return new OpenApiResponse<>(true, 200, message, data);
    }

    /**
     * FailedResponse
     */
    public static <T> OpenApiResponse<T> fail(Integer code, String message) {
        return new OpenApiResponse<>(false, code, message, null);
    }

    /**
     * FailedResponseDefault 400ErrorCode)
     */
    public static <T> OpenApiResponse<T> fail(String message) {
        return new OpenApiResponse<>(false, 400, message, null);
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
