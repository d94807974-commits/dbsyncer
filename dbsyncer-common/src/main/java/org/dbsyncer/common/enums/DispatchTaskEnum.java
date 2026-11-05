/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.common.enums;

/**
 * TasksMovement controlTypeEnumeration
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2025-06-13 00:00
 */
public enum DispatchTaskEnum {

    /* Count sync tasks */
    MAPPING_COUNT("Count sync tasks"),

    /* Match similar tables */
    MAPPING_MATCH_TABLE("Match similar tables"),

    /* Validation match similar tables */
    VALIDATE_SYNC_MATCH_TABLE("Validation match similar tables"),

    /* Count sync task tables */
    TABLE_GROUP_COUNT("Count sync task tables");

    DispatchTaskEnum(String name) {
        this.name = name;
    }

    private final String name;

    public String getName() {
        return name;
    }
}
