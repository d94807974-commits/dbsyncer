/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.sdk.filter.impl;

import org.realtimeetl.sdk.enums.FilterTypeEnum;
import org.realtimeetl.sdk.filter.AbstractFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Run-timeJDBC {@code IN} List：Generate {@code col IN (?,?,?)}，And bind in order.{@link #getBindValues()}。
 * @author wuji
 */
public class InFilter extends AbstractFilter {

    private final List<Object> bindValues;

    /**
     * @param columnName ListingandTableFieldUnanimously;ConnectorUnderlined/quoted)
     * @param bindValues and {@code IN} Medium{@code ?} One-on-one binding.Value（Yesnull OrganisationSQL NULL）
     */
    public InFilter(String columnName, List<Object> bindValues) {
        setName(columnName);
        this.bindValues = bindValues == null ? Collections.emptyList() : new ArrayList<>(bindValues);
    }

    public List<Object> getBindValues() {
        return bindValues;
    }

    @Override
    public FilterTypeEnum getFilterTypeEnum() {
        return FilterTypeEnum.STRING;
    }
}
