/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.mongodb.schema.support;

import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.dbsyncer.common.util.DateFormatUtil;
import org.dbsyncer.common.util.JsonUtil;
import org.dbsyncer.sdk.model.Field;
import org.dbsyncer.sdk.schema.support.StringType;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-06-06 20:00
 */
public final class MongoDBStringType extends StringType {

    private enum TypeEnum {
        STRING("string"), OBJECT_ID("objectId"), OBJECT("object"), ARRAY("array"), BINARY("binData");

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

    /**
     * Read side integration:Binary/byte[] Keep Asbyte[]，and MySQL BLOB Standard Field Alignment Avoid Verificationbyte[] vs String。
     * FieldTypeAlways.string（It's an empty collection of metadata.typeName=binData Judgement.
     */
    @Override
    public Object mergeValue(Object val, Field field) {
        if (val == null) {
            return getDefaultMergedVal(field);
        }
        if (val instanceof Binary) {
            return ((Binary) val).getData();
        }
        if (val instanceof byte[]) {
            return val;
        }
        if (TypeEnum.BINARY.getValue().equals(field.getTypeName()) && val instanceof String) {
            return ((String) val).getBytes(StandardCharsets.UTF_8);
        }
        return super.mergeValue(val, field);
    }

    @Override
    protected String merge(Object val, Field field) {
        if (val instanceof String) {
            return (String) val;
        }
        if (val instanceof ObjectId) {
            return val.toString();
        }
        if (val instanceof Number) {
            return val.toString();
        }
        if (val instanceof Timestamp) {
            return DateFormatUtil.timestampToString((Timestamp) val);
        }
        if (val instanceof Date) {
            return DateFormatUtil.dateToString((Date) val);
        }
        if (val instanceof java.util.Date) {
            return DateFormatUtil.dateToString((java.util.Date) val);
        }
        if (val instanceof Boolean) {
            return val.toString();
        }
        if (val instanceof Map || val instanceof List) {
            return JsonUtil.objToJson(val);
        }
        return throwUnsupportedException(val, field);
    }

    /**
     * Before writingConvert：MySQL BLOB/BINARY Wait.byte[] In the end,NoneCommentTarget fieldsDeclare asstring And...Yes binData，
     * WritingBSON Binary，Avoid Fatheringbyte[] ConvertString Caused data damage.
     */
    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof Binary) {
            return val;
        }
        if (val instanceof byte[]) {
            return new Binary((byte[]) val);
        }
        if (TypeEnum.BINARY.getValue().equals(field.getTypeName())) {
            if (val instanceof String) {
                return new Binary(((String) val).getBytes(StandardCharsets.UTF_8));
            }
            return throwUnsupportedException(val, field);
        }
        return super.convert(val, field);
    }
}
