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
package org.realtimeetl.connector.elasticsearch.schema.support;

import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.StringType;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ES StringType
 * Support: keyword, text, string(deprecated), ip, object, geo_point, geo_shape
 *
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2026-01-11 22:21
 */
public final class ElasticsearchStringType extends StringType {

    private enum TypeEnum {

        KEYWORD("keyword"), WILDCARD("wildcard"), TEXT("text"), @Deprecated
        STRING("string"),
        STRING_UPPERCASE("STRING"), /*Bigger.string*/
        IP("ip"), IP_RANGE("ip_range"), INTEGER_RANGE("integer_range"), LONG_RANGE("long_range"), DOUBLE_RANGE("double_range"), FLOAT_RANGE("float_range"), DATE_RANGE("date_range"),
        COMPLETION("completion"), OBJECT("object"), FLATTENED("flattened"), /* 7.3+ Special data introducedType，For flattening GroupsFlattened Object）Storage. It is dedicated to dealing with dynamic, unpredictable structures.Field，By flattening the whole object to a keyValueThat's right.FieldExplosion problem.*/
        DENSE_VECTOR("dense_vector"), /* 7.0+ Intensive Vector Introduction, For storage andSearchCompetency, Support for modernityAI/ML Application vector similaritySearch。 */
        SHAPE("shape"), RANK_FEATURES("rank_features"), /* 7.8+ For feature ranking*/
        POINT("point"), GEO_POINT("geo_point"), GEO_SHAPE("geo_shape"), NESTED("nested");

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
    protected String merge(Object val, Field field) {
        if (val instanceof byte[]) {
            return new String((byte[]) val, StandardCharsets.UTF_8);
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

    @Override
    public Object convertValue(Object val, Field field) {
        if (val instanceof String) {
            // WillTypeCapitaliseReplaceSpace is underlined to matchTypeEnum
            String enumName = field.getTypeName().toUpperCase().replace(" ", "_");
            TypeEnum typeEnum = TypeEnum.valueOf(enumName);
            switch (typeEnum) {
                case OBJECT:
                case FLATTENED:
                case COMPLETION:
                case IP_RANGE:
                case INTEGER_RANGE:
                case LONG_RANGE:
                case DOUBLE_RANGE:
                case FLOAT_RANGE:
                case DATE_RANGE:
                case RANK_FEATURES:
                case GEO_POINT:
                case POINT:
                case NESTED:
                case DENSE_VECTOR:
                case KEYWORD:
                    return parseObject((String) val);
                default:
                    break;
            }
        }
        return super.convertValue(val, field);
    }

    private Object parseObject(String val) {
        JsonUtil.JsonType type = JsonUtil.getJsonType(val);
        switch (type) {
            case STRING:
                return val;
            case OBJECT:
                return JsonUtil.jsonToObj(val, Map.class);
            case ARRAY:
                return JsonUtil.jsonToObj(val, List.class);
            default:
                break;
        }
        return val;
    }
}
