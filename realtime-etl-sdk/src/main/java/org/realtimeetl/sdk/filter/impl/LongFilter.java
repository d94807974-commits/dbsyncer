/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.sdk.filter.impl;

import org.realtimeetl.sdk.enums.FilterEnum;
import org.realtimeetl.sdk.enums.FilterTypeEnum;
import org.realtimeetl.sdk.filter.AbstractFilter;

import java.util.Objects;

public class LongFilter extends AbstractFilter {

    public LongFilter(String name, FilterEnum filterEnum, long value) {
        setName(name);
        setFilter(filterEnum.getName());
        setValue(Objects.toString(value));
    }

    @Override
    public FilterTypeEnum getFilterTypeEnum() {
        return FilterTypeEnum.LONG;
    }
}
