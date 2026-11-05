package org.realtimeetl.parser.convert.handler;

import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.convert.AbstractHandler;

import org.springframework.util.Assert;

/**
 * Remove N characters from behind
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/8 23:05
 */
public class SubStrLastHandler extends AbstractHandler {

    @Override
    protected Object convert(String args, Object value) {
        Assert.isTrue(NumberUtil.isCreatable(args), "Parameters must be positive.");
        String s = String.valueOf(value);
        int size = NumberUtil.toInt(args);
        int length = s.length();
        return StringUtil.substring(s, length - size, length);
    }
}
