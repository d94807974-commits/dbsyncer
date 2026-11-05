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
package org.realtimeetl.connector.postgresql.decoder;

import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.listener.event.RowChangedEvent;
import org.realtimeetl.sdk.spi.ConnectorService;

import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;

import java.nio.ByteBuffer;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2022-04-10 22:36
 */
public interface MessageDecoder {

    default void postProcessBeforeInitialization(ConnectorService connectorService, DatabaseConnectorInstance connectorInstance, String database) {
    }

    boolean skipMessage(ByteBuffer buffer, LogSequenceNumber startLsn, LogSequenceNumber lastReceiveLsn);

    RowChangedEvent processMessage(ByteBuffer buffer);

    String getSlotName();

    String getOutputPlugin();

    void withSlotOption(ChainedLogicalStreamBuilder builder);

    void setTaskId(String taskId);

    void setConfig(DatabaseConfig config);

    void setDatabase(String database);

    void setSchema(String schema);
}
