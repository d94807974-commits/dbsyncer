package org.realtimeetl.parser.strategy;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.SchemaResolver;

import java.util.Map;

/**
 * RecordsSync dataPolicy
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/9/29 22:38
 */
public interface FlushStrategy {

    /**
     * RecordsFullSync data
     *
     * @param result SyncResult
     * @param targetSchemaResolver Target fieldsParser
     * @param targetFieldMap Destination Table FieldsGather!
     */
    void flushFullData(Result result, SchemaResolver targetSchemaResolver, Map<String, Field> targetFieldMap);

    /**
     * RecordsIncrementalSync data
     *
     * @param result SyncResult
     * @param targetSchemaResolver Target fieldsParser
     * @param targetFieldMap Destination Table FieldsGather!
     */
    void flushIncrementData(Result result, SchemaResolver targetSchemaResolver, Map<String, Field> targetFieldMap);

    /**
     * Write OnlySuccess/Failed12 to storage, no.Refresh Meta Count.
     * <p>For individual summarySuccess/FailedCount path usage, avoidand Meta Double count Obtained.
     * FailedDetailed「Recording Synchronising Failed Data」Switch control;SuccessAt the same time.On「Record full volume data」and「Record synchronised successful data」。
     *
     * @param result               SyncResult（RequiredSettings taskId，DetailsminTable keys)
     * @param targetSchemaResolver Target fieldsParser
     * @param targetFieldMap       Destination Table FieldsGather!
     */
    void writeStorageOnly(Result result, SchemaResolver targetSchemaResolver, Map<String, Field> targetFieldMap);
}
