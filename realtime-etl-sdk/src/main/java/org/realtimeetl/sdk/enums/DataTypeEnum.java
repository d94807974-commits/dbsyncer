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
package org.realtimeetl.sdk.enums;

import org.realtimeetl.sdk.SdkException;

/**
 * Standard ContextType
 *
 * @author Realtime ETL.
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
