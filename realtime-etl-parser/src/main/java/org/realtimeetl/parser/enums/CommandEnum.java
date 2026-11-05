/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.parser.enums;

/**
 * Element command mode
 *
 * @author AE86
 * @version 1.0.0
 * @date 2020/04/24 14:19
 */
public enum CommandEnum {

    /**
     * Add
     */
    OPR_ADD,

    /**
     * Update
     */
    OPR_EDIT;

    /**
     * Judgement ConfirmationYesNoYesEditActions
     */
    public boolean isEdit() {
        return this == OPR_EDIT;
    }
}
