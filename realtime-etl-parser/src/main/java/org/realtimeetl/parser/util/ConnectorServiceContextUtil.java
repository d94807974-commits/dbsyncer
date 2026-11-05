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
package org.realtimeetl.parser.util;

import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.sdk.connector.DefaultConnectorServiceContext;
import org.realtimeetl.sdk.model.ValidateSyncTask;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-01-05 22:56
 */
public abstract class ConnectorServiceContextUtil {

    public static DefaultConnectorServiceContext buildConnectorServiceContext(Mapping mapping, boolean isSource) {
        return buildConnectorServiceContext(mapping.getId(), mapping.getSourceConnectorId(), mapping.getSourceDatabase(), mapping.getSourceSchema(),
                mapping.getTargetConnectorId(), mapping.getTargetDatabase(), mapping.getTargetSchema(), isSource);
    }

    public static DefaultConnectorServiceContext buildConnectorServiceContext(ValidateSyncTask task, boolean isSource) {
        return buildConnectorServiceContext(task.getId(), task.getSourceConnectorId(), task.getSourceDatabase(), task.getSourceSchema(),
                task.getTargetConnectorId(), task.getTargetDatabase(), task.getTargetSchema(), isSource);
    }

    public static DefaultConnectorServiceContext buildConnectorServiceContext(String uniqueId, String sourceConnectorId, String sourceDatabase, String sourceSchema,
                                                                              String targetConnectorId, String targetDatabase, String targetSchema, boolean isSource) {
        DefaultConnectorServiceContext context = new DefaultConnectorServiceContext();
        context.setMappingId(uniqueId);
        context.setCatalog(isSource ? sourceDatabase : targetDatabase);
        context.setSchema(isSource ? sourceSchema : targetSchema);
        context.setConnectorId(isSource ? sourceConnectorId : targetConnectorId);
        context.setSuffix(isSource ? ConnectorInstanceUtil.SOURCE_SUFFIX : ConnectorInstanceUtil.TARGET_SUFFIX);
        return context;
    }
}
