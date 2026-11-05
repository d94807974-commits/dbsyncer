/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.elasticsearch.schema.support;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.TimestampType;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ES DateType
 * Support: date
 * Default format:"strict_date_optional_time||epoch_millis"
 *
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2026-01-11 22:21
 */
public final class ElasticsearchTimestampType extends TimestampType {

    private enum TypeEnum {

        DATE("date");

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
        if (val instanceof String) {
            String format = (String) field.getExtInfo().get("format");
            if (format != null) {
                try {
                    if (format.contains(DateFormatUtil.PATTERN_YYYY_MM_DD_HH_MM_SS)) {
                        return DateFormatUtil.stringToTimestamp((String) val, DateTimeFormatter.ofPattern(format));
                    }
                    if (format.equals(DateFormatUtil.PATTERN_YYYY_MM_DD)) {
                        return new Timestamp(DateFormatUtil.stringToDate((String) val).getTime());
                    }
                } catch (ParseException | DateTimeException e) {
                    // Try to figure it out.
                }
            }
            return DateFormatUtil.stringToTimestamp((String) val);
        }

        if (val instanceof java.util.Date) {
            return new Timestamp(((java.util.Date) val).getTime());
        }

        if (val instanceof Long) {
            return new Timestamp((Long) val);
        }

        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        // Based onConfigureConvertTimeFormat
        String format = (String) field.getExtInfo().get("format");
        // ES date TypeI can only identify.java.util.Date
        if (val instanceof Timestamp) {
            return formatDate((Timestamp) val, format);
        }

        if (val instanceof Date) {
            Date date = (Date) val;
            return formatDate(new Timestamp(date.getTime()), format);
        }

        // If it's...Yes java.util.Date，DirectBack
        if (val instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) val;
            return formatDate(new Timestamp(date.getTime()), format);
        }

        if (val instanceof Long) {
            return formatDate(new Timestamp((Long) val), format);
        }

        if (val instanceof String) {
            return formatDate(DateFormatUtil.stringToTimestamp((String) val), format);
        }
        return throwUnsupportedException(val, field);
    }

    private Object formatDate(Timestamp timestamp, String format) {
        if (StringUtil.isNotBlank(format)) {
            return DateFormatUtil.timestampToString(timestamp, DateTimeFormatter.ofPattern(format));
        }
        return new java.util.Date(timestamp.getTime());
    }
}
