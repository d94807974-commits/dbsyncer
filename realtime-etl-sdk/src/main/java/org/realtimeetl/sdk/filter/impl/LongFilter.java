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
