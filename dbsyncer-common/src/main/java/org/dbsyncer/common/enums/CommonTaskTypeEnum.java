/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.common.enums;

import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.common.util.StringUtil;

/**
 * Task TypeEnumeration
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-03-22 19:52
 */
public enum CommonTaskTypeEnum {

    /**
     * Sync taskType
     */
    MAPPING("mapping", "org.dbsyncer.parser.model.Mapping"),

    /**
     * Data validation
     */
    VALIDATE_SYNC("validateSync", "org.dbsyncer.sdk.model.ValidateSyncTask"),

    /**
     * Database migration
     */
    DATABASE_SYNC("databaseSync", "org.dbsyncer.sdk.model.DatabaseSyncTask");

    /**
     * ConfigureType code（and {@code ConfigModel#type} / StorageTYPE Columns aligned)
     */
    private final String code;

    /**
     * CorrespondConfigModel Achieved class
     */
    private final Class<? extends ConfigModel> clazz;

    CommonTaskTypeEnum(String code, String className) {
        this.code = code;
        this.clazz = loadClass(className);
    }

    /**
     * PressNameorcode ParsingTask Type。
     *
     * @param typeStr Task TypeString (number of names or camels)code）
     * @return Task Type(a) Enumeration;NoneLegal RecognitionBack null
     */
    public static CommonTaskTypeEnum parse(String typeStr) {
        if (StringUtil.isBlank(typeStr)) {
            return null;
        }
        for (CommonTaskTypeEnum e : values()) {
            if (StringUtil.equals(typeStr, e.code)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Import ConfigurationSpecial: historical capitalTYPE Code as Currentcode；Run-time path does not depend on.
     *
     * @param typeStr ImportPackagetype
     * @return After regulationcode；SpaceBack
     */
    public static String normalizeImportType(String typeStr) {
        if (StringUtil.isBlank(typeStr)) {
            return typeStr;
        }
        if (StringUtil.equals(typeStr, "VALIDATE_SYNC")) {
            return VALIDATE_SYNC.code;
        }
        if (StringUtil.equals(typeStr, "DATABASE_SYNC")) {
            return DATABASE_SYNC.code;
        }
        return typeStr;
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends ConfigModel> loadClass(String className) {
        try {
            return (Class<? extends ConfigModel>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No task type realization category exists: " + className, e);
        }
    }

    public String getCode() {
        return code;
    }

    public Class<? extends ConfigModel> getClazz() {
        return clazz;
    }

}
