/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.mysql.deserializer;

import com.github.shyiko.mysql.binlog.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class DatetimeV2Deserialize {

    public Serializable deserializeDatetimeV2(int meta, ByteArrayInputStream inputStream) throws IOException {
        long datetime = bigEndianLong(inputStream.read(5), 0, 5);
        int yearMonth = bitSlice(datetime, 1, 17, 40);
        int fsp = deserializeFractionalSeconds(meta, inputStream);

        // Extract DateTimeMinistriesmin
        int year = yearMonth / 13;
        int month = yearMonth % 13;
        int day = bitSlice(datetime, 18, 5, 40);
        int hour = bitSlice(datetime, 23, 5, 40);
        int minute = bitSlice(datetime, 28, 6, 40);
        int second = bitSlice(datetime, 34, 6, 40);
        int nano = fsp * 1000;

        // 检查是否为 MySQL 的零日期 (0000-00-00 00:00:00)
        // MySQL From5.7Start default zero date not allowedJava LocalDateTime Not supported
        // Characteristics of date zero:year=0 ormonth=0 orday=0
        if (year == 0 || month == 0 || day == 0) {
            // Back null For Taskforce Zero Date, Avoid ThrowingError
            return null;
        }

        // Additional security checks: ensure datesTimeValueWithin the limits of effectiveness
        if (month < 1 || day < 1 || day > 31 || hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
            // DateTimeValueIt's beyond the scope.Back null
            return null;
        }
        return toTimestamp(year, month, day, hour, minute, second, nano);
    }

    /**
     * Parsing old versionsDATETIME Binary FormatDATETIME2），Read it literally, avoid pressingGMT TurnUnix TimeThe puncture occurs.
     */
    public Serializable deserializeDatetime(ByteArrayInputStream inputStream) throws IOException {
        int[] parts = split(inputStream.readLong(8), 100, 6);
        int year = parts[5];
        int month = parts[4];
        int day = parts[3];
        int hour = parts[2];
        int minute = parts[1];
        int second = parts[0];
        if (year == 0 || month == 0 || day == 0) {
            return null;
        }
        if (month < 1 || day < 1 || day > 31 || hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59) {
            return null;
        }
        return toTimestamp(year, month, day, hour, minute, second, 0);
    }

    private Timestamp toTimestamp(int year, int month, int day, int hour, int minute, int second, int nano) {
        LocalDateTime time = LocalDateTime.of(year, month, day, hour, minute, second, nano);
        return Timestamp.valueOf(time);
    }

    private int[] split(long value, int divider, int length) {
        int[] result = new int[length];
        for (int i = 0; i < length - 1; i++) {
            result[i] = (int) (value % divider);
            value /= divider;
        }
        result[length - 1] = (int) value;
        return result;
    }

    private long bigEndianLong(byte[] bytes, int offset, int length) {
        long result = 0;
        for (int i = offset; i < (offset + length); i++) {
            byte b = bytes[i];
            result = (result << 8) | (b >= 0 ? (int) b : (b + 256));
        }
        return result;
    }

    private int deserializeFractionalSeconds(int meta, ByteArrayInputStream inputStream) throws IOException {
        int length = (meta + 1) / 2;
        if (length > 0) {
            int fraction = bigEndianInteger(inputStream.read(length), 0, length);
            return fraction * (int) Math.pow(100, 3 - length);
        }
        return 0;
    }

    private int bigEndianInteger(byte[] bytes, int offset, int length) {
        int result = 0;
        for (int i = offset; i < (offset + length); i++) {
            byte b = bytes[i];
            result = (result << 8) | (b >= 0 ? (int) b : (b + 256));
        }
        return result;
    }

    private int bitSlice(long value, int bitOffset, int numberOfBits, int payloadSize) {
        long result = value >> payloadSize - (bitOffset + numberOfBits);
        return (int) (result & ((1 << numberOfBits) - 1));
    }
}
