/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.connector.oracle.schema.support;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.oracle.OracleException;
import org.realtimeetl.connector.oracle.schema.OracleBlobParameter;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.schema.support.BytesType;
import oracle.sql.BLOB;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2024-12-25 00:03
 */
public final class OracleBytesType extends BytesType {

    private enum TypeEnum {

        BLOB("BLOB"), RAW("RAW"), LONG_RAW("LONG RAW"), BFILE("BFILE");

        private final String value;

        TypeEnum(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static boolean isBlobType(String type) {
            return type != null && type.trim().toUpperCase(Locale.ROOT).contains("BLOB");
        }
    }

    @Override
    public Set<String> getSupportedTypeName() {
        return Arrays.stream(TypeEnum.values()).map(TypeEnum::getValue).collect(Collectors.toSet());
    }

    @Override
    protected byte[] getDefaultMergedVal(Field field) {
        // SQL NULL Holdnull，Do not group into empty arrays, avoid writingEMPTY_BLOB andVerify miscalculation
        return null;
    }

    @Override
    protected byte[] merge(Object val, Field field) {
        if (val instanceof OracleBlobParameter) {
            return ((OracleBlobParameter) val).getValue();
        }
        if (val instanceof byte[]) {
            return (byte[]) val;
        }
        if (val instanceof BLOB) {
            try {
                BLOB blob = (BLOB) val;
                return blob.getBytes(1, (int) blob.length());
            } catch (SQLException e) {
                throw new OracleException(e);
            }
        }
        if (val instanceof String) {
            String s = (String) val;
            // ProcessingOracle HEXTORAW Format: HEXTORAW('30303030303030303030303030303030')
            // Support multiple formats: HEXTORAW('...'), HEXTORAW("..."), HEXTORAW(...)
            if (s.trim().toUpperCase().startsWith("HEXTORAW(")) {
                // Extract the contents in brackets
                int startIdx = s.indexOf('(');
                int endIdx = s.lastIndexOf(')');
                if (startIdx >= 0 && endIdx > startIdx) {
                    String hexContent = s.substring(startIdx + 1, endIdx).trim();
                    // Remove quotation marks (single or double quotation marks)
                    if ((hexContent.startsWith("'") && hexContent.endsWith("'")) || (hexContent.startsWith("\"") && hexContent.endsWith("\""))) {
                        hexContent = hexContent.substring(1, hexContent.length() - 1);
                    }
                    return StringUtil.hexStringToByteArray(hexContent);
                }
            }
            // ProcessingEMPTY_BLOB() Format
            if ("EMPTY_BLOB()".equalsIgnoreCase(s.trim())) {
                return new byte[0];
            }
            // Processing pure hexadecimal strings (includes only)0-9, A-F, a-f）
            if (isHexString(s)) {
                return StringUtil.hexStringToByteArray(s);
            }
            // Normal StringConvertAs byte array
            return s.getBytes();
        }
        return throwUnsupportedException(val, field);
    }

    /**
     * CriterionYesNois a hexadecimal string
     */
    private boolean isHexString(String str) {
        if (StringUtil.isBlank(str)) {
            return false;
        }
        // Hexadecimal stringLengthI have to.YesEven
        if (str.length() % 2 != 0) {
            return false;
        }
        // InspectionYesNoInclude hexadecimal characters only
        for (char c : str.toCharArray()) {
            if (!((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected Object convert(Object val, Field field) {
        if (val instanceof OracleBlobParameter) {
            return val;
        }
        if (val instanceof String) {
            String s = (String) val;
            if (s.startsWith("HEXTORAW(")) {
                byte[] bytes = StringUtil.hexStringToByteArray(s.replace("HEXTORAW('", "").replace("')", ""));
                return wrapBlobIfNeeded(bytes, field);
            }
            if ("EMPTY_BLOB()".equals(s)) {
                // EmptyBLOB and SQL NULL Sector Entitiesmin：WritingEMPTY_BLOB，NotsetNull
                return wrapBlobIfNeeded(new byte[0], field);
            }
            return wrapBlobIfNeeded(s.getBytes(), field);
        }
        Object converted = super.convert(val, field);
        if (converted instanceof byte[]) {
            return wrapBlobIfNeeded((byte[]) converted, field);
        }
        return converted;
    }

    private Object wrapBlobIfNeeded(byte[] bytes, Field field) {
        if (TypeEnum.isBlobType(field == null ? null : field.getTypeName())) {
            return new OracleBlobParameter(bytes);
        }
        return bytes;
    }
}
