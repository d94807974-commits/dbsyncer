/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.starrocks.schema.support;

import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.schema.support.StringType;

import java.util.Arrays;
import java.util.Base64;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-07 03:00
 */
public final class StarRocksStringType extends StringType {

    private enum TypeEnum {
        STRING, JSON, HLL, BITMAP, ARRAY, MAP, STRUCT, PERCENTILE, VARCHAR;

        public String getValue() {
            return name();
        }
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(TypeEnum::getValue).collect(Collectors.toSet());
    }

    @Override
    protected String merge(Object val, Field field) {
        if (val == null) {
            return null;
        }
        if (val instanceof String) {
            return (String) val;
        }
        if (val instanceof byte[]) {
            // Objective CombinedWhen it is a string column,byte[] TurnBase64，Avoidnew String(bytes) Sp 1(b) or"[B@..."
            return Base64.getEncoder().encodeToString((byte[]) val);
        }
        return JsonUtil.objToJsonSafe(val);
    }

    /**
     * Before writingConvert：STRING/VARCHAR Wait.Objective CombinedLiner, copy.byte[] ClockBase64，Ban SustainObject.toString ♪ Turn into ♪"[B@xxxx]"。
     */
    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof byte[]) {
            return Base64.getEncoder().encodeToString((byte[]) val);
        }
        if (val instanceof String) {
            return val;
        }
        return super.convert(val, field);
    }
}
