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
package org.realtimeetl.sdk.util;

/**
 * JDBC ConnectTools
 */
public final class ConnectionUtil {

    private ConnectionUtil() {
    }

    /**
     * Judgement ConfirmationYesNoYesConnectDisconnect CategoryError（Should not be recoveredConnectClosed meeting
     */
    public static boolean isBrokenConnection(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                String lower = message.toLowerCase();
                if (lower.contains("broken pipe")
                        || lower.contains("communications link failure")
                        || lower.contains("connection reset")
                        || lower.contains("connection closed")
                        || lower.contains("nontransientconnection")
                        || lower.contains("could not create connection")) {
                    return true;
                }
            }
            current = current.getCause();
        }
        return false;
    }
}
