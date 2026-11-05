package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.parser.convert.Handler;

/**
 * Add Before
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/10/8 23:04
 */
public class PrependHandler implements Handler {

    @Override
    public Object handle(String args, Object value) {
        if (null == value) {
            return args;
        }
        return new StringBuilder().append(args).append(value).toString();
    }
}
