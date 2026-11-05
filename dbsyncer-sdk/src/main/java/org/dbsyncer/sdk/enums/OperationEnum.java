/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

/**
 * ConditionExpressionType
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/9/26 23:21
 */
public enum OperationEnum {

    AND("and", "AND"), OR("or", "OR"), SQL("sql", "Custom SQL condition");

    private final String name;
    private final String message;

    OperationEnum(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public static boolean isAnd(String name) {
        return AND.getName().equals(name);
    }

    public static boolean isOr(String name) {
        return OR.getName().equals(name);
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }
}
