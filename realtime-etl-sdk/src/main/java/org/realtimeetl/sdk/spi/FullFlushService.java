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
package org.realtimeetl.sdk.spi;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Task;
import org.realtimeetl.sdk.schema.SchemaResolver;

import java.util.Map;

/**
 * FullPageLevelResultDropLibrary。Clusters and single machines achievedVariance
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-14
 */
public interface FullFlushService {

    /**
     * FullPageLevelResultDropLibrary。
     *
     * @param task                 Run a stateminOn Snippetid YesminPlanID）
     * @param result               StatementsResult
     * @param targetSchemaResolver Target databaseTypeParsing
     * @param targetFieldMap       Target fields
     */
    void flush(Task task, Result result, SchemaResolver targetSchemaResolver, Map<String, Field> targetFieldMap);
}
