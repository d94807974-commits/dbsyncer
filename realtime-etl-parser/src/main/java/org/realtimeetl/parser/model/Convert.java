package org.realtimeetl.parser.model;

import org.realtimeetl.parser.enums.ConvertEnum;

/**
 * FieldConvert
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/1 14:04
 */
public class Convert {

    /**
     * Field nameClaims
     */
    private String name;

    /**
     * ConvertName
     * @see ConvertEnum
     */
    private String convertName;

    /**
     * ConvertModalities
     *
     * @see ConvertEnum
     */
    private String convertCode;

    /**
     * ConvertParameter
     *
     * @see ConvertEnum
     */
    private String args;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConvertName() {
        return convertName;
    }

    public void setConvertName(String convertName) {
        this.convertName = convertName;
    }

    public String getConvertCode() {
        return convertCode;
    }

    public void setConvertCode(String convertCode) {
        this.convertCode = convertCode;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }
}
