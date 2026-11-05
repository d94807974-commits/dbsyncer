/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.dbsyncer.sdk.filter;

import org.dbsyncer.common.util.CollectionUtils;
import org.dbsyncer.sdk.enums.FilterEnum;
import org.dbsyncer.sdk.enums.SortEnum;
import org.dbsyncer.sdk.enums.StorageEnum;
import org.dbsyncer.sdk.filter.impl.IntFilter;
import org.dbsyncer.sdk.filter.impl.LongFilter;
import org.dbsyncer.sdk.filter.impl.StringFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author AE86
 * @Version 1.0.0
 * @Date 2020-01-08 15:17
 */
public class Query {

    /**
     * {@link StorageEnum}
     */
    private StorageEnum type;

    /**
     * StorageminSnips. Yeah.{@link StorageEnum#TASK_DETAIL} YesTasks ID（{@code dbsyncer_task_detail_{taskId}}）；
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
