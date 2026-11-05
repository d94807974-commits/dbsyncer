package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.convert.AbstractHandler;

/**
 * Remove End Character
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/8 23:05
 */
public class RemStrLastHandler extends AbstractHandler {

    @Override
    protected Object convert(String args, Object value) {
        String s = String.valueOf(value);
        return StringUtil.substring(s, 0, s.length() - 1);
    }
}
