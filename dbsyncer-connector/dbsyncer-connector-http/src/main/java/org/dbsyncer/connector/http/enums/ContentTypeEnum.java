/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.connector.http.enums;

/**
 * Content-TypeType
 *
 * @author The clouds.
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
