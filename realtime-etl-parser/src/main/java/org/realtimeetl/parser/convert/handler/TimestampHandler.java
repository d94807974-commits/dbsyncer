package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.parser.convert.Handler;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * System Timetamp
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/8 23:03
 */
public class TimestampHandler implements Handler {

    @Override
    public Object handle(String args, Object value) {
        return new Timestamp(Instant.now().toEpochMilli());
    }
}
