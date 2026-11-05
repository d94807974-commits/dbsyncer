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
package org.realtimeetl.biz.vo;

import org.realtimeetl.sdk.enums.FilterEnum;
import org.realtimeetl.sdk.enums.OperationEnum;
import org.realtimeetl.sdk.enums.QuartzFilterEnum;

import java.util.List;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2020-01-19 17:20
 */
public class ConditionVO {

    /**
     * Condition
     */
    private List<OperationEnum> operation;

    /**
     * System Parameters(Time Configuration)
     */
    private List<QuartzFilterEnum> quartzFilter;

    /**
     * Operators
     */
    private List<FilterEnum> filter;

    public ConditionVO(List<OperationEnum> operation, List<QuartzFilterEnum> quartzFilter, List<FilterEnum> filter) {
        this.operation = operation;
        this.quartzFilter = quartzFilter;
        this.filter = filter;
    }

    public List<OperationEnum> getOperation() {
        return operation;
    }

    public void setOperation(List<OperationEnum> operation) {
        this.operation = operation;
    }

    public List<QuartzFilterEnum> getQuartzFilter() {
        return quartzFilter;
    }

    public void setQuartzFilter(List<QuartzFilterEnum> quartzFilter) {
        this.quartzFilter = quartzFilter;
    }

    public List<FilterEnum> getFilter() {
        return filter;
    }

    public void setFilter(List<FilterEnum> filter) {
        this.filter = filter;
    }
}