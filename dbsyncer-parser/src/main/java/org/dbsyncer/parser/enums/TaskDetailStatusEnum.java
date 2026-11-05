/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.enums;

import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.sdk.constant.ConfigConstant;

/**
 * TasksDetailsListStatusFiltersuccess / fail）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-07-31 18:20
 */
public enum TaskDetailStatusEnum {

    /**
     * Success（Corresponding indicator column= 0）
     */
    SUCCESS(ConfigConstant.META_SUCCESS),

    /**
     * Failed（Corresponding indicator column&gt; 0）
     */
    FAIL(ConfigConstant.META_FAIL);

    private final String code;

    TaskDetailStatusEnum(String code) {
        this.code = code;
    }

    /**
     * ParsingRequest parameters；Space orNoneTraditionally.Back {@code null}（Means no pressStatusAgriculture
     *
     * @param value success / fail（Ignore case)
     * @return Enumer Sexualitynull
     */
    public static TaskDetailStatusEnum from(String value) {
        if (StringUtil.isBlank(value)) {
            return null;
        }
        for (TaskDetailStatusEnum status : values()) {
            if (StringUtil.equalsIgnoreCase(status.code, value)) {
                return status;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }
}
