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
package org.realtimeetl.connector.http.constant;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-02-12 01:04
 */
public class HttpConstant {

    public static final String API = "api";

    public static final String METHOD = "method";

    public static final String CONTENT_TYPE = "contentType";

    public static final String PARAMS = "params";

    public static final String EXTRACT_PATH = "extractPath";

    public static final String EXTRACT_TOTAL = "extractTotal";

    public static final String WRITE_PATH = "writePath";

    /** Connection parameters（ConnectorConfigure properties） */
    // ConnectTimeoutms）
    public static final String CONNECTION_TIMEOUT_MS = "connection.timeout.ms";
    // Read Timeoutms）
    public static final String SOCKET_TIMEOUT_MS = "socket.timeout.ms";
    // FromConnectPool AccessConnectTimeoutms）
    public static final String CONNECTION_REQUEST_TIMEOUT_MS = "connection.request.timeout.ms";
    // Try againNumber
    public static final String RETRY_TIMES = "retry.times";
    // Character encoding
    public static final String CHARSET = "charset";

    /** System Variables*/
    // PageNumber
    public static final String PAGE_INDEX = "$pageIndex$";
    // Page/5/
    public static final String PAGE_SIZE = "$pageSize$";
    // CursorParameter
    public static final String CURSOR = "$cursor$";

    /**
     * HarmonizationIncrementalParameter：command to store "static variables"key（Time/Date etc.checkLastPoint Placeholder for writingReplaceValue，JSON SequencedMap）
     */
    public static final String HTTP_INCREMENT_VARS = "HTTP_INCREMENT_VARS";

    // Is it a public network?scene (viaConfigureor request a judgement)
    public static final String PUBLIC_NETWORK_HEADER = "X-Public-Network";

}
