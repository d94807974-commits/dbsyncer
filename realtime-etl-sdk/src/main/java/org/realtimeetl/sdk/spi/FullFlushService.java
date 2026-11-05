/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
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
 * @author wuji
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
