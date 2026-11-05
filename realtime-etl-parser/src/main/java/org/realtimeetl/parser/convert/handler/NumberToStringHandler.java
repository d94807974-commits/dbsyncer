package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.parser.convert.AbstractHandler;

/**
 * Number to String
 *
 * @author AE86
 * @version 1.0.0
 * @date 2022/7/20 23:04
 */
public class NumberToStringHandler extends AbstractHandler {

    @Override
    public Object convert(String args, Object value) {
        return String.valueOf(value);
    }
}
