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
package org.realtimeetl.connector.http.enums;

/**
 * Content-TypeType
 *
 * @author Realtime ETL.
 * @version 1.0.0
 */
public enum ContentTypeEnum {

    /**
     * JSON
     */
    JSON("1", "application/json", "JSON format"),

    /**
     * Form
     */
    FORM_URLENCODED("2", "application/x-www-form-urlencoded", "Form format");

    private final String type;
    private final String code;
    private final String message;

    ContentTypeEnum(String type, String code, String message) {
        this.type = type;
        this.code = code;
        this.message = message;
    }

    /**
     * Based onvalue Get Enumeration
     *
     * @param type Content-Type Value
     * @return Element Object
     */
    public static ContentTypeEnum fromValue(String type) {
        if (type == null) {
            return null;
        }
        for (ContentTypeEnum contentType : values()) {
            if (contentType.getType().equalsIgnoreCase(type)) {
                return contentType;
            }
        }
        return null;
    }

    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
