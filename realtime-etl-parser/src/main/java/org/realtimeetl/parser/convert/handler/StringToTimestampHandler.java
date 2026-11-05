package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.parser.convert.AbstractHandler;

/**
 * String rotationTimestamp
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2022/7/12 23:04
 */
public class StringToTimestampHandler extends AbstractHandler {

    @Override
    public Object convert(String args, Object value) {
        if (value instanceof String) {
            String s = (String) value;
            return DateFormatUtil.stringToTimestamp(s);
        }
        return value;
    }
}
