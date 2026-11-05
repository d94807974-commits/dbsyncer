/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.postgresql.enums;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.postgresql.PostgreSQLException;
import org.realtimeetl.connector.postgresql.decoder.MessageDecoder;
import org.realtimeetl.connector.postgresql.decoder.impl.PgOutputMessageDecoder;
import org.realtimeetl.connector.postgresql.decoder.impl.TestDecodingMessageDecoder;

/**
 * @Author AE86
 * @Version 1.0.0
 * @Date 2022-04-10 22:36
 */
public enum MessageDecoderEnum {

    /**
     * Plugin：TEST_DECODING
     */
    TEST_DECODING("test_decoding", TestDecodingMessageDecoder.class),

    /**
     * Plugin：PG_OUTPUT
     */
    PG_OUTPUT("pgoutput", PgOutputMessageDecoder.class);

    private final String type;
    private final Class<?> clazz;

    MessageDecoderEnum(String type, Class<?> clazz) {
        this.type = type;
        this.clazz = clazz;
    }

    public static MessageDecoder getMessageDecoder(String type) throws PostgreSQLException, IllegalAccessException, InstantiationException {
        for (MessageDecoderEnum e : MessageDecoderEnum.values()) {
            if (StringUtil.equals(type, e.getType())) {
                return (MessageDecoder) e.getClazz().newInstance();
            }
        }
        return (MessageDecoder) TEST_DECODING.getClazz().newInstance();
    }

    public String getType() {
        return type;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
