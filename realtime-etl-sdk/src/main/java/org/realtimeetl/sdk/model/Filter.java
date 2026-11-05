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
package org.realtimeetl.sdk.model;

import org.realtimeetl.sdk.enums.FilterEnum;
import org.realtimeetl.sdk.enums.OperationEnum;

/**
 * FieldPropertiesCondition
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/9/30 15:10
 */
public class Filter {

    /**
     * Field name，ID
     */
    private String name;

    /**
     * @see OperationEnum
     */
    private String operation;

    /**
     * @see FilterEnum
     */
    private String filter;

    /**
     * Value
     */
    private String value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
