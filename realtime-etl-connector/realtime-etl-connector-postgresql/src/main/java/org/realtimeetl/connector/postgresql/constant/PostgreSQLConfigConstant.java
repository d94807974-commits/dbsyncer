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
package org.realtimeetl.connector.postgresql.constant;

import org.realtimeetl.connector.postgresql.enums.MessageDecoderEnum;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2025-12-29 00:21
 */
public final class PostgreSQLConfigConstant {

    /**
     * Plugin {@link MessageDecoderEnum}
     */
    public static final String PLUGIN_NAME = "pluginName";

    /**
     * Delete slot，Incremental sync: automatically delete Slot when stopping the driver
     */
    public static final String DROP_SLOT_ON_CLOSE = "dropSlotOnClose";
}
