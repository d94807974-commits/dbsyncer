package org.realtimeetl.parser.enums;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.ParserException;
import org.realtimeetl.parser.convert.Handler;
import org.realtimeetl.parser.convert.handler.AppendHandler;
import org.realtimeetl.parser.convert.handler.BytesToStringHandler;
import org.realtimeetl.parser.convert.handler.ClearHandler;
import org.realtimeetl.parser.convert.handler.DateHandler;
import org.realtimeetl.parser.convert.handler.DefaultHandler;
import org.realtimeetl.parser.convert.handler.LongToTimestampHandler;
import org.realtimeetl.parser.convert.handler.NumberToStringHandler;
import org.realtimeetl.parser.convert.handler.PrependHandler;
import org.realtimeetl.parser.convert.handler.RemStrFirstHandler;
import org.realtimeetl.parser.convert.handler.RemStrLastHandler;
import org.realtimeetl.parser.convert.handler.ReplaceHandler;
import org.realtimeetl.parser.convert.handler.StringToFormatDateHandler;
import org.realtimeetl.parser.convert.handler.StringToTimestampHandler;
import org.realtimeetl.parser.convert.handler.SubStrFirstHandler;
import org.realtimeetl.parser.convert.handler.SubStrLastHandler;
import org.realtimeetl.parser.convert.handler.TimestampHandler;
import org.realtimeetl.parser.convert.handler.TimestampToChineseStandardTimeHandler;
import org.realtimeetl.parser.convert.handler.TimestampToDateHandler;
import org.realtimeetl.parser.convert.handler.TimestampToLongHandler;
import org.realtimeetl.parser.convert.handler.UUIDHandler;

/**
 * SupportedConvertType
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/9/19 23:56
 */
public enum ConvertEnum {

    /**
     * Default value
     */
    DEFAULT("DEFAULT", "Default value", 1, new DefaultHandler()),
    /**
     * System Timetamp
     */
    SYSTEM_TIMESTAMP("SYSTEM_TIMESTAMP", "System Timetamp", 0, new TimestampHandler()),
    /**
     * System DateDate
     */
    SYSTEM_DATE("SYSTEM_DATE", "System Date", 0, new DateHandler()),
    /**
     * Timestamp to Date.
     */
    TIMESTAMP_TO_DATE("TIMESTAMP_TO_DATE", "Timestamp to Date.", 0, new TimestampToDateHandler()),
    /**
     * TimestampTransfer to Chinese standardTime
     */
    TIMESTAMP_TO_CHINESE_STANDARD_TIME("TIMESTAMP_TO_CHINESE_STANDARD_TIME", "Timestamp turns yyyy-MM-dd HH:mm:ss", 0, new TimestampToChineseStandardTimeHandler()),
    /**
     * Timestamp turns Long
     */
    TIMESTAMP_TO_LONG("TIMESTAMP_TO_LONG", "Timestamp turns Long", 0, new TimestampToLongHandler()),
    /**
     * Long turns Timestamp
     */
    LONG_TO_TIMESTAMP("LONG_TO_TIMESTAMP", "Long turns Timestamp", 0, new LongToTimestampHandler()),
    /**
     * String to Timestamp
     */
    STRING_TO_TIMESTAMP("STRING_TO_TIMESTAMP", "String to Timestamp", 0, new StringToTimestampHandler()),
    /**
     * StringDate of transferCustomFormat
     */
    STRING_TO_FORMAT_DATE("STRING_TO_FORMAT_DATE", "Spring to Date Custom", 1, new StringToFormatDateHandler()),
    /**
     * Number to String
     */
    NUMBER_TO_STRING("NUMBER_TO_STRING", "Number to String", 0, new NumberToStringHandler()),
    /**
     * Byte [ ] Turning
     */
    BYTES_TO_STRING("BYTES_TO_STRING", "Byte [ ] Turning", 0, new BytesToStringHandler()),
    /**
     * Replace
     */
    REPLACE("REPLACE", "Replace", 2, new ReplaceHandler()),
    /**
     * Add to Front,For example:Chang Empower”App Authority123 => 123Chang Empower
     */
    PREPEND("PREPEND", "Add Before", 1, new PrependHandler()),
    /**
     * Add to Back,For example:Chang Empower”App Authority123 => Chang Empower123
     */
    APPEND("APPEND", "Add after", 1, new AppendHandler()),
    /**
     * UUID
     */
    UUID("UUID", "UUID", 0, new UUIDHandler()),
    /**
     * Operationally remove the first character
     */
    REM_STR_FIRST("REM_STR_FIRST", "Operationally remove the first character", 0, new RemStrFirstHandler()),
    /**
     * Remove End Character
     */
    REM_STR_LAST("REM_STR_LAST", "Remove End Character", 0, new RemStrLastHandler()),
    /**
     * Remove N characters from front
     */
    SUB_STR_FIRST("SUB_STR_FIRST", "Remove N characters from front", 1, new SubStrFirstHandler()),
    /**
     * Remove N characters from behind
     */
    SUB_STR_LAST("SUB_STR_LAST", "Remove N characters from behind", 1, new SubStrLastHandler()),
    /**
     * Clear
     */
    CLEAR("CLEAR", "Clear", 0, new ClearHandler());

    // ConvertEncoded
    private final String code;
    // ConvertName
    private final String name;
    // ParameterNumber
    private final int argNum;
    // ConvertAchieved
    private final Handler handler;

    ConvertEnum(String code, String name, int argNum, Handler handler) {
        this.code = code;
        this.name = name;
        this.argNum = argNum;
        this.handler = handler;
    }

    public static Handler getHandler(String code) throws ParserException {
        for (ConvertEnum e : ConvertEnum.values()) {
            if (StringUtil.equals(code, e.getCode())) {
                return e.getHandler();
            }
        }
        throw new ParserException(String.format("Handler code \"%s\" does not exist.", code));
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getArgNum() {
        return argNum;
    }

    public Handler getHandler() {
        return handler;
    }

}