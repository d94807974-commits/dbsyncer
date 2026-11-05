package org.realtimeetl.parser.convert;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/8 22:55
 */
public interface Handler {

    /**
     * ValueConvert
     *
     * @param args Parameter
     * @param value Value
     * @return
     */
    Object handle(String args, Object value);
}
