/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.enums;

import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.sdk.SdkException;

/**
 * Sync Enumeration
 *
 * @author AE86
 * @version 1.0.0
 * @date 2023-11-16 00:51
 */
public enum ModelEnum {

    /**
     * Full
     */
    FULL("full", "Full"),
    /**
     * Incremental
     */
    INCREMENT("increment", "Incremental"),
    /**
     * Full + incremental (Full + incremental)
     */
    FULL_INCREMENT("fullIncrement", "Full + incremental");

    private final String code;
    private final String name;

    ModelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static ModelEnum getModelEnum(String code) throws SdkException {
        for (ModelEnum e : ModelEnum.values()) {
            if (StringUtil.equals(code, e.getCode())) {
                return e;
            }
        }
        throw new SdkException(String.format("Model code \"%s\" does not exist.", code));
    }

    public static boolean isFull(String model) {
        return StringUtil.equals(FULL.getCode(), model) || StringUtil.equals(FULL_INCREMENT.getCode(), model);
    }

    public static boolean isIncrement(String code) {
        return StringUtil.equals(INCREMENT.getCode(), code);
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
