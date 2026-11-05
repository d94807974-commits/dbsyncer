/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.connector.mysql.schema.support;

import org.realtimeetl.connector.mysql.MySQLException;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.StringType;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-11-26 22:24
 */
public final class MySQLStringType extends StringType {

    private enum TypeEnum {
        CHAR, // FixedLength，Most255Character
        VARCHAR, // FixedLength，Most65535Character，64K
        TINYTEXT, // VariableLength，Most255Character
        TEXT, // VariableLength，Most65535Character，64K
        MEDIUMTEXT, // VariableLength，Most2Yes.24Sub--1Character，16M
        LONGTEXT, // VariableLength，Most2Yes.32Sub--1Character，4GB
        ENUM, // 2bytes, maximum accessible65535A different list.Value
        SET,//Element type
        JSON, GEOMETRY; // POINT, LINESTRING, POLYGON, MULTIPOINT, MULTILINESTRING, MULTIPOLYGON,
        // GEOMETRYCOLLECTION
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    protected String merge(Object val, Field field) {
        switch (TypeEnum.valueOf(field.getTypeName())) {
            case GEOMETRY:
                return deserializeGeometry((byte[]) val);
            case ENUM:
            case SET:
                return String.valueOf(val);
            default:
                break;
        }
        if (val instanceof byte[]) {
            return new String((byte[]) val, StandardCharsets.UTF_8);
        }
        return throwUnsupportedException(val, field);
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof String) {
            if (TypeEnum.valueOf(field.getTypeName()) == TypeEnum.GEOMETRY) {
                return serializeGeometry((String) val);
            }
            return val;
        }
        return super.convert(val, field);
    }

    private String deserializeGeometry(byte[] bytes) {
        try {
            byte[] geometryBytes = ByteBuffer.allocate(bytes.length - 4).order(ByteOrder.LITTLE_ENDIAN).put(bytes, 4, bytes.length - 4).array();
            WKBReader reader = new WKBReader();
            return reader.read(geometryBytes).toText();
        } catch (ParseException e) {
            throw new MySQLException(e);
        }
    }

    private byte[] serializeGeometry(String wellKnownText) {
        try {
            Geometry geometry = new WKTReader().read(wellKnownText);
            byte[] bytes = new WKBWriter(2, ByteOrderValues.LITTLE_ENDIAN).write(geometry);
            return ByteBuffer.allocate(bytes.length + 4).order(ByteOrder.LITTLE_ENDIAN).putInt(geometry.getSRID()).put(bytes).array();
        } catch (ParseException e) {
            throw new MySQLException(e);
        }
    }
}
