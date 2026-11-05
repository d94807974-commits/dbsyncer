/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.elasticsearch.enums;

import org.realtimeetl.common.util.StringUtil;

import org.elasticsearch.ElasticsearchException;

import java.sql.Types;

/**
 * ESFieldType
 *
 * @author AE86
 * @version 1.0.0
 * @Date 2023-08-26 21:13
 */
public enum ESFieldTypeEnum {

    // CharacterType
    KEYWORD("keyword", Types.VARCHAR), TEXT("text", Types.LONGVARCHAR),
    /**
     * ES 5.XNo more supportstring, BytextorkeywordReplace
     */
    @Deprecated
    STRING("string", Types.VARCHAR),

    // NumbersType
    /**
     * Source databaseMediumTypeYesunsigned int，Recommendedlong
     */
    INTEGER("integer", Types.INTEGER),
    /**
     * bitOnly one. Recommended.boolean
     */
    LONG("long", Types.BIGINT),
    /**
     * Source databaseMediumTypeYesunsigned tinyint orunsigned smallint，Recommendedinteger
     */
    SHORT("short", Types.INTEGER), VERSION("version", Types.INTEGER), BYTE("byte", Types.BIT),
    /**
     * As a guarantee.Outlook salute，Recommendedtext
     */
    DOUBLE("double", Types.DOUBLE), FLOAT("float", Types.FLOAT), HALF_FLOAT("half_float", Types.FLOAT), SCALED_FLOAT("scaled_float", Types.FLOAT), BOOLEAN("boolean", Types.BOOLEAN),

    // DateType
    /**
     * Default format:"strict_date_optional_time||epoch_millis"
     * "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"  “2021/01/01 12:10:30” or “2021-01-01”
     */
    DATE("date", Types.DATE),

    // ScopeType
    INTEGER_RANGE("integer_range", Types.INTEGER), FLOAT_RANGE("float_range", Types.FLOAT), LONG_RANGE("long_range", Types.BIGINT), DOUBLE_RANGE("double_range", Types.DOUBLE),
    DATE_RANGE("date_range", Types.DATE),

    // OtherType
    /**
     * CompensatoryobjectTypeInsufficient, format appearslistFire!objectIt'll be...array："test": [{"a":"b},{}]
     */
    NESTED("nested", Types.OTHER), OBJECT("object", Types.VARCHAR), IP("ip", Types.VARCHAR), TOKEN_COUNT("token_count", Types.BIGINT), GEO_POINT("geo_point", Types.VARCHAR),
    GEO_SHAPE("geo_shape", Types.VARCHAR), BINARY("binary", Types.BINARY);

    private final String code;
    private final int type;

    ESFieldTypeEnum(String code, int type) {
        this.code = code;
        this.type = type;
    }

    public static int getType(String code) throws ElasticsearchException {
        for (ESFieldTypeEnum e : ESFieldTypeEnum.values()) {
            if (StringUtil.equals(e.getCode(), code)) {
                return e.getType();
            }
        }
        return Types.VARCHAR;
    }

    public String getCode() {
        return code;
    }

    public int getType() {
        return type;
    }
}
