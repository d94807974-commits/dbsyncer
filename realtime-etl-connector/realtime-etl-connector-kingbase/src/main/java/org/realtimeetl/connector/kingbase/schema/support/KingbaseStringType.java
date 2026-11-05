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
package org.realtimeetl.connector.kingbase.schema.support;

import com.kingbase8.util.KBobject;
import org.realtimeetl.common.util.UUIDUtil;
import org.realtimeetl.connector.postgresql.PostgreSQLException;
import org.realtimeetl.connector.postgresql.schema.PostgreSQLSchemaResolver;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.StringType;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.postgresql.geometric.PGpoint;
import org.postgresql.jdbc.PgArray;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-06 18:30
 */
public final class KingbaseStringType extends StringType {

    private enum TypeEnum {

        // Standard StringType
        UUID("uuid"), VARCHAR("varchar"), TEXT("text"), CHAR("char"), BPCHAR("bpchar"), NAME("name"),
        // JSON Type
        JSON("json"), JSONB("jsonb"),
        // GeometryType
        POINT("point"),
        // PostGIS ExtensionType
        GEOMETRY("geometry"), GEOGRAPHY("geography"),
        // NetworkType
        INET("inet"), CIDR("cidr"), MACADDR("macaddr"), MACADDR8("macaddr8"),
        // TextSearchType
        TSQUERY("tsquery"), TSVECTOR("tsvector"),
        // ScopeType
        TSRANGE("tsrange"), TSTZRANGE("tstzrange"), DATERANGE("daterange"), INT4RANGE("int4range"), INT8RANGE("int8range"), NUMRANGE("numrange"),
        // BitstringType
        BIT("bit"), BIT_VARYING("bit varying"), VARBIT("varbit"),
        // OtherType
        CITEXT("citext"), XML("xml"), PG_LSN("pg_lsn"), TXID_SNAPSHOT("txid_snapshot");
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
        return Arrays.stream(KingbaseStringType.TypeEnum.values()).map(KingbaseStringType.TypeEnum::getValue).collect(Collectors.toSet());
    }

    @Override
    protected String merge(Object val, Field field) {
        if (val instanceof UUID) {
            return val.toString();
        }
        if (val instanceof PGpoint) {
            PGpoint pgPoint = (PGpoint) val;
            return String.format("POINT (%f %f)", pgPoint.x, pgPoint.y);
        }
        if (val instanceof PGobject) {
            PGobject pgObject = (PGobject) val;
            return pgObject.getValue();
        }
        if (val instanceof Boolean) {
            return ((Boolean) val) ? "1" : "0";
        }
        if (val instanceof PgArray) {
            return val.toString();
        }
        if (val instanceof String && PostgreSQLSchemaResolver.isArrayType(field.getTypeName())) {
            return (String) val;
        }
        if (val instanceof KBobject) {
            return val.toString();
        }
        return throwUnsupportedException(val, field);
    }

    @Override
    public Object getDefaultConvertedVal(Field field) {
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType(PostgreSQLSchemaResolver.normalizeTypeName(field.getTypeName()));
            pgObject.setValue(null);
            return pgObject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object convert(Object val, Field field) {
        // NormativeTypeName, handle tape.schema Prefix(s)"public"."geometry"）Situation
        String rawTypeName = PostgreSQLSchemaResolver.normalizeTypeName(field.getTypeName());
        if (PostgreSQLSchemaResolver.isArrayType(rawTypeName)) {
            if (val instanceof String) {
                return toPgObject(rawTypeName, (String) val);
            }
            if (val instanceof PgArray) {
                return val;
            }
        }
        // WillTypeCapitaliseReplaceSpace is underlined to matchTypeEnum
        String enumName = rawTypeName.toUpperCase().replace(" ", "_");
        KingbaseStringType.TypeEnum typeEnum = KingbaseStringType.TypeEnum.valueOf(enumName);
        // BIT TypeNeed for special treatment: supportInteger、Boolean、Number、String Wait for multiple inputs
        if (typeEnum == KingbaseStringType.TypeEnum.BIT) {
            return convertToBit(val);
        }

        if (val instanceof String) {
            String strVal = (String) val;
            switch (typeEnum) {
                case UUID:
                    try {
                        return UUIDUtil.fromString(strVal);
                    } catch (IllegalArgumentException e) {
                        // UUID FormatNoneTimeBack null，Avoid ThrowingError
                        return null;
                    }
                case POINT:
                    return toPoint(strVal);
                case VARCHAR:
                case TEXT:
                case CHAR:
                case BPCHAR:
                case NAME:
                    // Standard StringType，DirectBack
                    return val;
                // BelowTypeI need it.PGobject Packaging
                case JSON:
                case JSONB:
                case GEOMETRY:
                case GEOGRAPHY:
                case CITEXT:
                case XML:
                case INET:
                case CIDR:
                case MACADDR:
                case MACADDR8:
                case TSQUERY:
                case TSVECTOR:
                case TSRANGE:
                case TSTZRANGE:
                case DATERANGE:
                case INT4RANGE:
                case INT8RANGE:
                case NUMRANGE:
                case BIT_VARYING:
                case VARBIT:
                case PG_LSN:
                case TXID_SNAPSHOT:
                    return toPgObject(typeEnum.getValue(), strVal);
                default:
                    return val;
            }
        }
        return super.convert(val, field);
    }

    /**
     * WillValueConvertYesPostgreSQL bit Type（PGobject Packaging).
     * SupportInteger、Boolean、Number、String Waiting for input.ConvertYesbit String.
     */
    private Object convertToBit(Object val) {
        String bitStr;
        if (val instanceof Boolean) {
            bitStr = ((Boolean) val) ? "1" : "0";
        } else if (val instanceof Number) {
            // Integer、Short、Long etc.: non0 Yes"1"，0 Yes"0"
            bitStr = ((Number) val).intValue() != 0 ? "1" : "0";
        } else if (val instanceof String) {
            String s = (String) val;
            // String"true"/"1" Change to"1"，OtherConvert to"0"
            if ("true".equalsIgnoreCase(s) || "1".equals(s) || "t".equalsIgnoreCase(s)) {
                bitStr = "1";
            } else {
                bitStr = "0";
            }
        } else {
            throw new PostgreSQLException(String.format("Cannot convert %s to bit type, val [%s]", val.getClass(), val));
        }
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType(KingbaseStringType.TypeEnum.BIT.getValue());
            pgObject.setValue(bitStr);
            return pgObject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Object toPgObject(String type, String value) {
        try {
            PGobject pgObject = new PGobject();
            pgObject.setType(type);
            pgObject.setValue(value);
            return pgObject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Object toPoint(String val) {
        try {
            PGpoint pgPoint = new PGpoint();
            WKTReader reader = new WKTReader();
            Geometry geom = reader.read(val);
            if (geom instanceof Point) {
                Point point = (Point) geom;
                pgPoint.x = point.getX();
                pgPoint.y = point.getY();
            }
            return pgPoint;
        } catch (ParseException e) {
            throw new PostgreSQLException(e);
        }
    }
}
