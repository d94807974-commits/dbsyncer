package org.realtimeetl.sdk.model;

import org.realtimeetl.common.util.JsonUtil;

import java.util.Properties;

/**
 * FieldProperties
 *
 * @author AE86
 * @version 1.0.0
 * @date 2019/9/30 15:10
 */
public class Field {

    /**
     * Field name，ID
     */
    private String name;

    /**
     * TypeName,INT
     */
    private String typeName;

    /**
     * TypeEncoded
     */
    private int type;

    /**
     * Primary key
     */
    private boolean pk;

    /**
     * FieldAlias
     */
    private String labelName;

    /**
     * Field/5/
     */
    private int columnSize;

    /**
     * FieldPercentage
     */
    private int ratio;

    /**
     * Extension Configuration
     */
    private Properties extInfo = new Properties();

    public Field() {
    }

    public Field(String name, String typeName, int type) {
        this.name = name;
        this.typeName = typeName;
        this.type = type;
    }

    public Field(String name, String typeName, int type, boolean pk) {
        this.name = name;
        this.typeName = typeName;
        this.type = type;
        this.pk = pk;
    }

    public Field(String name, String typeName, int type, boolean pk, int columnSize, int ratio) {
        this.name = name;
        this.typeName = typeName;
        this.type = type;
        this.pk = pk;
        this.columnSize = columnSize;
        this.ratio = ratio;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }

    public String getLabelName() {
        return labelName;
    }

    public void setLabelName(String labelName) {
        this.labelName = labelName;
    }

    public int getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(int columnSize) {
        this.columnSize = columnSize;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public Properties getExtInfo() {
        return extInfo;
    }

    public void setExtInfo(Properties extInfo) {
        this.extInfo = extInfo;
    }

    @Override
    public String toString() {
        return JsonUtil.objToJson(this);
    }
}
