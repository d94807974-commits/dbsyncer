/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

/**
 * Synchronisation SupportedDDLYou can't.
 *
 * @version 1.0.0
 * @Author life
 * @Date 2023-09-24 14:24
 */
public enum DDLOperationEnum {
    /**
     * ChangeFieldProperties
     */
    ALTER_MODIFY,
    /**
     * InsertField
     */
    ALTER_ADD,
    /**
     * ChangeField nameClaims
     */
    ALTER_CHANGE,
    /**
     * DeleteField
     */
    ALTER_DROP;
}
