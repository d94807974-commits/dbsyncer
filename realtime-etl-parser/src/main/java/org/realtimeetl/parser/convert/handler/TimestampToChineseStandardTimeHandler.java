package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.parser.convert.AbstractHandler;

import java.sql.Timestamp;

/**
 * TimestampTransfer to Chinese standardTime
 *
 * @author AE86
 * @version 1.0.0
 * @date 2021/12/20 23:04
 */
public class TimestampToChineseStandardTimeHandler extends AbstractHandler {

    @Override
    public Object convert(String args, Object value) {
        if (value instanceof Timestamp) {
            Timestamp t = (Timestamp) value;
            value = DateFormatUtil.timestampToString(t);
        }
        return value;
    }
}
