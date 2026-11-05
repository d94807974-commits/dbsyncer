/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.common.binlog;

import com.google.protobuf.ByteString;
import org.dbsyncer.common.column.AbstractColumnValue;
import org.dbsyncer.common.enums.BinlogByteEnum;
import org.dbsyncer.common.util.NumberUtil;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2022-06-30 22:39
 */
public class BinlogColumnValue extends AbstractColumnValue<ByteString> {

    public BinlogColumnValue(ByteString v) {
        setValue(v);
    }

    @Override
    public String asString() {
        return getValue().toStringUtf8();
    }

    @Override
    public byte[] asByteArray() {
        return getValue().toByteArray();
    }

    @Override
    public Byte asByte() {
        return getValue().byteAt(0);
    }

    @Override
    public Short asShort() {
        byte[] bytes = asByteArray();
        int expectedLength = BinlogByteEnum.SHORT.getByteLength();

        // If bytesLengthLess than expectedLength，Fill
        if (bytes.length < expectedLength) {
            byte[] paddedBytes = new byte[expectedLength];
            System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
            // The remaining bytes fill as0
            ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
            buffer.put(paddedBytes);
            buffer.flip();
            return buffer.asShortBuffer().get();
        }

        // If bytesLengthGreater than or equal to expectationLength，Only beforeexpectedLength Bytes
        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.put(bytes, 0, expectedLength);
        buffer.flip();
        return buffer.asShortBuffer().get();
    }

    @Override
    public Integer asInteger() {
        byte[] bytes = asByteArray();
        if (bytes.length == BinlogByteEnum.BYTE.getByteLength()) {
            return NumberUtil.toInt(asString());
        }
        if (bytes.length == BinlogByteEnum.SHORT.getByteLength()) {
            Short aShort = asShort();
            return new Integer(aShort);
        }

        ByteBuffer buffer = ByteBuffer.allocate(BinlogByteEnum.INTEGER.getByteLength());
        buffer.put(bytes, 0, buffer.capacity());
        buffer.flip();
        return buffer.asIntBuffer().get();
    }

    @Override
    public Long asLong() {
        ByteBuffer buffer = ByteBuffer.allocate(BinlogByteEnum.LONG.getByteLength());
        buffer.put(asByteArray(), 0, buffer.capacity());
        buffer.flip();
        return buffer.asLongBuffer().get();
    }

    @Override
    public Float asFloat() {
        byte[] bytes = asByteArray();
        int expectedLength = BinlogByteEnum.FLOAT.getByteLength();

        // If bytesLengthLess than expectedLength，Fill
        if (bytes.length < expectedLength) {
            byte[] paddedBytes = new byte[expectedLength];
            System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
            // The remaining bytes fill as0
            ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
            buffer.put(paddedBytes);
            buffer.flip();
            return buffer.asFloatBuffer().get();
        }

        // If bytesLengthGreater than or equal to expectationLength，Only beforeexpectedLength Bytes
        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.put(bytes, 0, expectedLength);
        buffer.flip();
        return buffer.asFloatBuffer().get();
    }

    @Override
    public Double asDouble() {
        byte[] bytes = asByteArray();
        int expectedLength = BinlogByteEnum.DOUBLE.getByteLength();

        // If bytesLengthLess than expectedLength，Fill
        if (bytes.length < expectedLength) {
            byte[] paddedBytes = new byte[expectedLength];
            System.arraycopy(bytes, 0, paddedBytes, 0, bytes.length);
            // The remaining bytes fill as0
            ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
            buffer.put(paddedBytes);
            buffer.flip();
            return buffer.asDoubleBuffer().get();
        }

        // If bytesLengthGreater than or equal to expectationLength，Only beforeexpectedLength Bytes
        ByteBuffer buffer = ByteBuffer.allocate(expectedLength);
        buffer.put(bytes, 0, expectedLength);
        buffer.flip();
        return buffer.asDoubleBuffer().get();
    }

    @Override
    public Boolean asBoolean() {
        ByteBuffer buffer = ByteBuffer.allocate(BinlogByteEnum.SHORT.getByteLength());
        buffer.put(asByteArray(), 0, buffer.capacity());
        buffer.flip();
        return buffer.asShortBuffer().get() == 1;
    }

    @Override
    public BigDecimal asBigDecimal() {
        return new BigDecimal(asString());
    }

    @Override
    public Date asDate() {
        return new Date(asLong());
    }

    @Override
    public Timestamp asTimestamp() {
        return new Timestamp(asLong());
    }

    @Override
    public Time asTime() {
        return new Time(asLong());
    }
}
