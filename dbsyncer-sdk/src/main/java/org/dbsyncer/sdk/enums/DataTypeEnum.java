/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

import org.dbsyncer.sdk.SdkException;

/**
 * Standard ContextType
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2024-11-23 22:39
 */
public enum DataTypeEnum {

    /** Text*/
    STRING,
    /** Integer*/
    BYTE, SHORT, INT, LONG,
    /** Floating Point Type*/
    DECIMAL, DOUBLE, FLOAT,
    /** Boolean*/
    BOOLEAN,
    /** Time */
    DATE, TIME, TIMESTAMP,
    /** Binary*/
    BYTES;

    public static DataTypeEnum getType(String type) {
        for (DataTypeEnum e : DataTypeEnum.values()) {
            if (e.name().equals(type)) {
                return e;
            }
        }
        throw new SdkException(String.format("DataTypeEnum type \"%s\" does not exist.", type));
    }
}
