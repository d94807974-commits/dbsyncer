package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.convert.AbstractHandler;

/**
 * Operationally remove the first character
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/10/8 23:05
 */
public class RemStrFirstHandler extends AbstractHandler {

    @Override
    protected Object convert(String args, Object value) {
        return StringUtil.substring(String.valueOf(value), 1);
    }
}
