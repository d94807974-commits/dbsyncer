/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.parser.convert.AbstractHandler;

import java.text.ParseException;
import java.time.format.DateTimeFormatter;

/**
 * String rotationCustomFormat
 *
 * @author wuji
 */
public class StringToFormatDateHandler extends AbstractHandler {

    @Override
    public Object convert(String args, Object value) throws ParseException {

        if (value instanceof String) {
            String s = (String) value;
            return DateFormatUtil.stringToDate(s, DateTimeFormatter.ofPattern(args));
        }
        return value;
    }
}
