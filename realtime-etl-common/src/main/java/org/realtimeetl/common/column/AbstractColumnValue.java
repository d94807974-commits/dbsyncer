/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.common.column;

import org.realtimeetl.common.util.StringUtil;

public abstract class AbstractColumnValue<T> implements ColumnValue {

    protected Object value;

    protected T getValue() {
        return (T) value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean isNull() {
        return value == null;
    }

    /**
     * InspectionValueYesNoFamiliesnull、Empty string or string"null"）
     */
    protected boolean isEmpty(String value) {
        return value == null || StringUtil.isBlank(value) || "null".equalsIgnoreCase(value);
    }
}
