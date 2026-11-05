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
package org.realtimeetl.connector.mysql.deserializer;

import com.github.shyiko.mysql.binlog.event.deserialization.json.JsonBinary;
import com.github.shyiko.mysql.binlog.io.ByteArrayInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2022-08-30 00:22
 */
public final class JsonBinaryDeserialize {

    private static final Logger logger = LoggerFactory.getLogger(JsonBinaryDeserialize.class);

    private static final byte[] EMPTY_JSON_BYTES = "".getBytes(StandardCharsets.UTF_8);

    /**
     * Read and parseJSON Columnbinary ContentsUTF-8 text.
     *
     * @param meta        LengthFieldBytes occupied
     * @param inputStream binlog EventStream
     * @return JSON TextUTF-8 bytes; empty or BasedFailedTimeBackEmpty string
     * @throws IOException Readbinlog StreamFailedTime Throw
     */
    public byte[] deserializeJson(int meta, ByteArrayInputStream inputStream) throws IOException {
        int blobLength = inputStream.readInteger(meta);
        byte[] bytes = inputStream.read(blobLength);
        if (bytes == null || bytes.length == 0) {
            logger.warn("JSON binary is empty, fallback to empty string");
            return EMPTY_JSON_BYTES;
        }
        try {
            return new JsonBinary(bytes).getString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            // JsonBinary.getString Maybe.IOException Wrap it up.RuntimeException（LikeEOFException）
            logger.warn("Failed to parse JSON binary (len={}), fallback to empty string: {}",
                    bytes.length, e.toString());
            return EMPTY_JSON_BYTES;
        }
    }
}
