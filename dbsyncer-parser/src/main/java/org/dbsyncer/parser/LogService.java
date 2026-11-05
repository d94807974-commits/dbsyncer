package org.dbsyncer.parser;

import java.util.Map;

/**
 * @version 1.0.0
 * @Author AE86
 * @Date 2020-05-21 23:18
 */
public interface LogService {

    void log(LogType logType);

    void log(LogType logType, String msg);

    void log(LogType logType, String format, Object... args);

    /**
     * Writing tapeOperator/IP/StatusYes.Log（JSON It's a summary.
     *
     * @param logType  LogType
     * @param username Operator/entry account
     * @param ip       ClientIP
     * @param status   StatusYeah, see you.{@link org.dbsyncer.common.enums.StatusEnum}
     * @param msg      Summary; emptylogType.message
     */
    void log(LogType logType, String username, String ip, int status, String msg);

    /**
     * Writing tapeOperator/IP/StatusStructureLog（detail Sequence developmentJSON）。
     *
     * @param logType  LogType
     * @param username Operator/entry account
     * @param ip       ClientIP
     * @param status   StatusYeah, see you.{@link org.dbsyncer.common.enums.StatusEnum}
     * @param detail   Structured contentmessage / requestUri Wait.
     */
    void log(LogType logType, String username, String ip, int status, Map<String, Object> detail);
}
