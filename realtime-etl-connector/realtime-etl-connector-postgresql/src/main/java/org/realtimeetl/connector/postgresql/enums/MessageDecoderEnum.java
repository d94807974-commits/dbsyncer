/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.connector.postgresql.enums;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.postgresql.PostgreSQLException;
import org.realtimeetl.connector.postgresql.decoder.MessageDecoder;
import org.realtimeetl.connector.postgresql.decoder.impl.PgOutputMessageDecoder;
import org.realtimeetl.connector.postgresql.decoder.impl.TestDecodingMessageDecoder;

/**
 * @Author Realtime ETL
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
