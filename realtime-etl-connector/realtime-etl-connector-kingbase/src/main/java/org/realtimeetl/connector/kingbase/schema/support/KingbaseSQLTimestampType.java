/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.connector.kingbase.schema.support;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.TimestampType;

import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-06-25 23:26
 */
public final class KingbaseSQLTimestampType extends TimestampType {

    private enum TypeEnum {

        /**
         * StandardTimePoke
         */
        TIMESTAMP("timestamp"),
        /**
         * Time zoneTimePoke
         */
        TIMESTAMP_TZ("timestamptz"),
        /**
         * MySQL Works mode is commonTypeFirst Name
         */
        DATETIME("datetime");

        private final String value;

        TypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(TypeEnum::getValue).collect(Collectors.toSet());
    }

    @Override
    protected Timestamp merge(Object val, Field field) {
        if (val instanceof OffsetDateTime) {
            OffsetDateTime offsetDateTime = (OffsetDateTime) val;
            return Timestamp.from(offsetDateTime.toInstant());
        }
        if (val instanceof String) {
            Timestamp timestamp = DateFormatUtil.stringToTimestamp((String) val);
            if (timestamp != null) {
                return timestamp;
            }
        }
        return throwUnsupportedException(val, field);
    }
}