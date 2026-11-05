package org.realtimeetl.parser.enums;

/**
 * ParserParameterEnumeration
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/04/29 10:19
 */
public enum ParserEnum {

    /**
     * Cursor
     */
    CURSOR("cursor", 0),

    /**
     * PageNumber
     */
    PAGE_INDEX("pageIndex", 1),

    /**
     * ImplementedMap relationIndex
     */
    TABLE_GROUP_INDEX("tableGroupIndex", 0),

    /**
     * FullMultiple progress
     */
    TABLE_PROGRESS("tableProgress", 0),

    /**
     * Full + incrementalPhase: full(FullMedium) / increment(IncrementalMedium)
     */
    FULL_INCREMENT_PHASE("fullIncrementPhase", 0);

    /**
     * Encoded
     */
    private final String code;

    /**
     * Default value
     */
    private final int defaultValue;

    ParserEnum(String code, int defaultValue) {
        this.code = code;
        this.defaultValue = defaultValue;
    }

    public String getCode() {
        return code;
    }

    public int getDefaultValue() {
        return defaultValue;
    }
}
