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

import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.github.shyiko.mysql.binlog.event.deserialization.DeleteRowsEventDataDeserializer;
import com.github.shyiko.mysql.binlog.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2025-04-12 15:21
 */
public final class DeleteDeserializer extends DeleteRowsEventDataDeserializer {

    private final DatetimeV2Deserialize datetimeV2Deserialize = new DatetimeV2Deserialize();
    private final TimeDeserialize timeDeserialize = new TimeDeserialize();
    private final JsonBinaryDeserialize jsonBinaryDeserialize = new JsonBinaryDeserialize();

    public DeleteDeserializer(Map<Long, TableMapEventData> tableMapEventByTableId) {
        super(tableMapEventByTableId);
    }

    protected byte[] deserializeJson(int meta, ByteArrayInputStream inputStream) throws IOException {
        return jsonBinaryDeserialize.deserializeJson(meta, inputStream);
    }

    @Override
    protected Serializable deserializeDatetime(ByteArrayInputStream inputStream) throws IOException {
        return datetimeV2Deserialize.deserializeDatetime(inputStream);
    }

    @Override
    protected Serializable deserializeDatetimeV2(int meta, ByteArrayInputStream inputStream) throws IOException {
        return datetimeV2Deserialize.deserializeDatetimeV2(meta, inputStream);
    }

    @Override
    protected Serializable deserializeTime(ByteArrayInputStream inputStream) throws IOException {
        return timeDeserialize.deserializeTime(inputStream);
    }

    @Override
    protected Serializable deserializeTimeV2(int meta, ByteArrayInputStream inputStream) throws IOException {
        return timeDeserialize.deserializeTimeV2(meta, inputStream);
    }
}
