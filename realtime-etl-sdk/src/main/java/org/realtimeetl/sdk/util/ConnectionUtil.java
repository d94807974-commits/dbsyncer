/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
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
