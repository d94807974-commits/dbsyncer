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
package org.realtimeetl.biz.vo;

import org.realtimeetl.common.util.StringUtil;

import java.io.Serializable;

/**
 * 请求响应对象
 *
 * @author Realtime ETL
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
