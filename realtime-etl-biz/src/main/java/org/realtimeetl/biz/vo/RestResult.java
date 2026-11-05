/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz.vo;

import org.realtimeetl.common.util.StringUtil;

import java.io.Serializable;

/**
 * 请求响应对象
 *
 * @author AE86
 * @version 1.0.0
 * @date 2017Year3Intern30p.m.2:26:19
 */
public class RestResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * YesNoSuccess
     */
    private boolean success;

    /**
     * Data Fact
     */
    private T data;

    /**
     * Message
     */
    private String message;

    /**
     * StatusCode
     */
    private int status;

    public RestResult(boolean success, T data, String message, int status) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.status = status;
    }

    public static RestResult restFail(String message) {
        return new RestResult(false, null, message, 500);
    }

    public static RestResult restFail(String message, int status) {
        return new RestResult(false, null, message, status);
    }

    public static RestResult restSuccess(Object data) {
        return new RestResult(true, data, StringUtil.EMPTY, 200);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RestResult [success=" + success + ", data=" + data + ", message=" + message + ", status=" + status + "]";
    }
}
