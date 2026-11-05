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
package org.realtimeetl.connector.postgresql.enums;

public enum MessageTypeEnum {

    BEGIN, COMMIT, TABLE, INSERT, UPDATE, DELETE, RELATION, TRUNCATE, TYPE, ORIGIN, NONE;

    public static MessageTypeEnum getType(char type) {
        switch (type) {
            case 'B':
                return BEGIN;
            case 'C':
                return COMMIT;
            case 't':
                return TABLE;
            case 'I':
                return INSERT;
            case 'U':
                return UPDATE;
            case 'D':
                return DELETE;
            case 'R':
                return RELATION;
            case 'Y':
                return TYPE;
            case 'O':
                return ORIGIN;
            case 'T':
                return TRUNCATE;
            default:
                return NONE;
        }
    }
}