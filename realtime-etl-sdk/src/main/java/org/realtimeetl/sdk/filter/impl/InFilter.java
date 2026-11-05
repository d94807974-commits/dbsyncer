/**
 * Copyright 2026 Realtime ETL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.realtimeetl.sdk.filter.impl;

import org.realtimeetl.sdk.enums.FilterTypeEnum;
import org.realtimeetl.sdk.filter.AbstractFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Run-timeJDBC {@code IN} List：Generate {@code col IN (?,?,?)}，And bind in order.{@link #getBindValues()}。
 * @author Realtime ETL
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
