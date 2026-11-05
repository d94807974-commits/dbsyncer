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
package org.realtimeetl.common.enums;

/**
 * Supported serialization/reverse sequence bytesType
 *
 * <pre>
 * Type     Length     Size MinimumValue     MaxValue
 * byte     1Byte    8-bit     -128       +127
 * short    2Byte    16-bit    -2^15      +2^15-1
 * int      4Byte    32-bit    -2^31      +2^31-1
 * long     8Byte    64-bit    -2^63      +2^63-1
 * float    4Byte    32-bit    IEEE754    IEEE754
 * double   8Byte    64-bit    IEEE754    IEEE754
 * char     2Byte    16-bit    Unicode 0  Unicode 2^16-1
 * boolean  8Byte    64-bit
 * </pre>
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2023/4/21 22:07
 */
public enum BinlogByteEnum {

    /**
     * 8Byte
     */
    LONG(8),
    /**
     * 8Byte
     */
    DOUBLE(8),
    /**
     * 4Byte
     */
    INTEGER(4),
    /**
     * 4Byte
     */
    FLOAT(4),
    /**
     * 2Byte
     */
    SHORT(2),
    /**
     * 1Byte
     */
    BYTE(1),;

    BinlogByteEnum(int byteLength) {
        this.byteLength = byteLength;
    }

    final int byteLength;

    public int getByteLength() {
        return byteLength;
    }
}
