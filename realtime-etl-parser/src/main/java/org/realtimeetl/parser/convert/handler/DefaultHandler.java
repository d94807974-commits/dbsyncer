package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.convert.Handler;

/**
 * Default value
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/10/8 23:02
 */
public class DefaultHandler implements Handler {

    @Override
    public Object handle(String args, Object value) {
        return null == value || StringUtil.isBlank(String.valueOf(value)) ? args : value;
    }
}
