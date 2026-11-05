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
package org.realtimeetl.sdk.filter;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.sdk.enums.FilterEnum;
import org.realtimeetl.sdk.enums.SortEnum;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.impl.IntFilter;
import org.realtimeetl.sdk.filter.impl.LongFilter;
import org.realtimeetl.sdk.filter.impl.StringFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2020-01-08 15:17
 */
public class Query {

    /**
     * {@link StorageEnum}
     */
    private StorageEnum type;

    /**
     * StorageminSnips. Yeah.{@link StorageEnum#TASK_DETAIL} YesTasks ID（{@code realtime_etl_task_detail_{taskId}}）；
     */
    private String taskId;

    private BooleanFilter booleanFilter = new BooleanFilter();

    /**
     * Query applications, no sorting, only totals
     */
    private boolean queryTotal;

    private int pageNum = 1;

    private int pageSize = 20;

    /**
     * Updated atand create default drop orderBack
     */
    private SortEnum sort = SortEnum.DESC;

    /**
     * CustomSortFieldList，Prefer non-empt Key Sorting Logic
     */
    private final List<OrderBy> orderByList = new ArrayList<>();

    /**
     * SELECT The white list.Field
     */
    private Set<String> selectFlied;

    /**
     * BackValueConvertDevice, limitDiskUse
     */
    private Map<String, FieldResolver> fieldResolverMap = new ConcurrentHashMap<>();

    public Query() {
    }

    public Query(int pageNum, int pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public void addFilter(String name, String value) {
        booleanFilter.add(new StringFilter(name, FilterEnum.EQUAL, value, false));
    }

    public void addFilter(String name, String value, boolean enableHighLightSearch) {
        booleanFilter.add(new StringFilter(name, FilterEnum.LIKE, value, enableHighLightSearch));
    }

    /**
     * StringConditionFilter(SupportEQUAL/IN/LIKE Wait.)。
     *
     * @param name       Field name
     * @param filterEnum Operators
     * @param value      FilterValue(IN Comma Spelling)
     */
    public void addFilter(String name, FilterEnum filterEnum, String value) {
        booleanFilter.add(new StringFilter(name, filterEnum, value, false));
    }

    public void addFilter(String name, int value) {
        booleanFilter.add(new IntFilter(name, value));
    }

    public void addFilter(String name, FilterEnum filterEnum, long value) {
        booleanFilter.add(new LongFilter(name, filterEnum, value));
    }

    public StorageEnum getType() {
        return type;
    }

    public void setType(StorageEnum type) {
        this.type = type;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public BooleanFilter getBooleanFilter() {
        return booleanFilter;
    }

    public void setBooleanFilter(BooleanFilter booleanFilter) {
        this.booleanFilter = booleanFilter;
    }

    public boolean isQueryTotal() {
        return queryTotal;
    }

    public void setQueryTotal(boolean queryTotal) {
        this.queryTotal = queryTotal;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public SortEnum getSort() {
        return sort;
    }

    public void setSort(SortEnum sort) {
        this.sort = sort;
    }

    public List<OrderBy> getOrderByList() {
        return orderByList;
    }

    /**
     * AddCustomSortField（Use specified sorting direction)
     *
     * @param fieldName Field name（Camel peak format, likecreateTime）
     * @param sort      Sort Direction
     */
    public void addOrderBy(String fieldName, SortEnum sort) {
        orderByList.add(new OrderBy(fieldName, sort));
    }

    /**
     * AddCustomSortField（UseQuery Global sorting direction)
     *
     * @param fieldName Field name（Camel peak format, likecreateTime）
     */
    public void addOrderBy(String fieldName) {
        orderByList.add(new OrderBy(fieldName, null));
    }

    public boolean hasCustomOrderBy() {
        return !CollectionUtils.isEmpty(orderByList);
    }

    /**
     * YesNoUse for the current queryCustom SELECT Listed (white or black).
     */
    public boolean hasSelectField() {
        return !CollectionUtils.isEmpty(selectFlied);
    }

    public void setSelectFlied(Set<String> selectFlied) {
        this.selectFlied = selectFlied;
    }

    public Set<String> getSelectFlied() {
        return selectFlied;
    }

    public Map<String, FieldResolver> getFieldResolverMap() {
        return fieldResolverMap;
    }

    public void setFieldResolverMap(Map<String, FieldResolver> fieldResolverMap) {
        this.fieldResolverMap = fieldResolverMap;
    }

    /**
     * SortFieldDescription, support eachFieldSeparately assign sorting direction
     */
    public static class OrderBy {

        private final String fieldName;

        /**
         * Yesnull OrganisationQuery Sort Global Directions
         */
        private final SortEnum sort;

        public OrderBy(String fieldName, SortEnum sort) {
            this.fieldName = fieldName;
            this.sort = sort;
        }

        public String getFieldName() {
            return fieldName;
        }

        public SortEnum getSort() {
            return sort;
        }
    }
}
