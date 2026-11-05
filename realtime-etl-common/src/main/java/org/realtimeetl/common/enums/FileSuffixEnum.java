package org.realtimeetl.common.enums;

import org.realtimeetl.common.util.StringUtil;

/**
 * DocumentationFormat
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2021/2/23 23:21
 */
public enum FileSuffixEnum {

    /**
     * jar
     */
    JAR("jar"),

    /**
     * JSON
     */
    JSON("json"),

    /**
     * ZIP ConfigurePackage
     */
    ZIP("zip");

    private final String name;

    FileSuffixEnum(String name) {
        this.name = name;
    }

    /**
     * AccessDocumentationType
     *
     * @param suffix
     * @return
     */
    public static FileSuffixEnum getFileSuffix(String suffix) {
        for (FileSuffixEnum e : FileSuffixEnum.values()) {
            if (StringUtil.equals(suffix, e.getName())) {
                return e;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
