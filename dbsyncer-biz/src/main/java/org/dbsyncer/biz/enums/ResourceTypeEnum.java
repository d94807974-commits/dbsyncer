/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz.enums;

import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.sdk.constant.ConfigConstant;

/**
 * ACL / Business groupAvailableResource type（Encodedand {@link ConfigConstant} (d) Unanimously.
 *
 * @author wuji
 * @version 1.0.0
 */
public enum ResourceTypeEnum {

    /**
     * Business group（Inheritance of leaves in group; inactiongrantOwner Objective Combined）
     */
    GROUP(ConfigConstant.GROUP, "Business group", false),

    /**
     * Connector
     */
    CONNECTOR(ConfigConstant.CONNECTOR, "Connector", true),

    /**
     * Sync task
     */
    MAPPING(ConfigConstant.MAPPING, "Sync task", true),

    /**
     * Revised verification tasks
     */
    VALIDATE_SYNC(ConfigConstant.VALIDATE_SYNC, "Data validation", true),

    /**
     * Library Migration Task
     */
    DATABASE_SYNC(ConfigConstant.DATABASE_SYNC, "Database migration", true);

    private final String code;
    private final String name;
    /**
     * YesNoLeaf.Resource（direct instruction; automatic as creatorAuthorizationObjective Combined）
     */
    private final boolean leaf;

    ResourceTypeEnum(String code, String name, boolean leaf) {
        this.code = code;
        this.name = name;
        this.leaf = leaf;
    }

    /**
     * Parsing by code;UnknownBack {@code null}。
     */
    public static ResourceTypeEnum get(String code) {
        if (StringUtil.isBlank(code)) {
            return null;
        }
        for (ResourceTypeEnum type : values()) {
            if (StringUtil.equals(type.code, code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * YesNoSupportedResource typecode.
     */
    public static boolean isSupported(String code) {
        return get(code) != null;
    }

    /**
     * YesNoLeaf.ResourceEncoding (excludingGroup）。
     */
    public static boolean isLeaf(String code) {
        ResourceTypeEnum type = get(code);
        return type != null && type.leaf;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public boolean isGroup() {
        return this == GROUP;
    }
}
