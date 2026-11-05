/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.impl;

import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.MetaAggProfile;
import org.realtimeetl.parser.enums.CommandEnum;
import org.realtimeetl.parser.model.MetaAgg;
import org.realtimeetl.parser.util.ConfigModelUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.storage.StorageService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * {@link MetaAggProfile} Achieved.
 *
 * @author wuji
 * @version 1.0.0
 */
@Component
public class MetaAggProfileImpl implements MetaAggProfile {

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    private StorageService storageService;

    @Override
    public MetaAgg getByStatDate(String statDate) {
        if (StringUtil.isBlank(statDate)) {
            return null;
        }
        Query query = new Query(1, 1);
        query.setType(StorageEnum.META_DAILY);
        query.addFilter(ConfigConstant.META_DAILY_STAT_DATE, statDate);
        Paging paging = storageService.query(query);
        if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
            return null;
        }
        Object first = paging.getData().iterator().next();
        if (!(first instanceof Map)) {
            return null;
        }
        return ConfigModelUtil.parseFromRow((Map) first, MetaAgg.class);
    }

    @Override
    public List<MetaAgg> listRecent(int days) {
        int limit = days <= 0 ? 30 : days;
        List<MetaAgg> metaAggs = operationTemplate.queryList(StorageEnum.META_DAILY, new Query(), MetaAgg.class);
        if (CollectionUtils.isEmpty(metaAggs)) {
            return Collections.emptyList();
        }
        metaAggs.sort(Comparator.comparing(MetaAgg::getStatDate, Comparator.nullsLast(String::compareTo)));
        if (metaAggs.size() <= limit) {
            return metaAggs;
        }
        return new ArrayList<>(metaAggs.subList(metaAggs.size() - limit, metaAggs.size()));
    }

    @Override
    public void upsert(MetaAgg daily) {
        if (daily == null || StringUtil.isBlank(daily.getStatDate())) {
            return;
        }
        long now = System.currentTimeMillis();
        MetaAgg existing = getByStatDate(daily.getStatDate());
        if (existing == null) {
            daily.setCreateTime(now);
            daily.setUpdateTime(now);
            operationTemplate.execute(daily, CommandEnum.OPR_ADD);
            return;
        }
        existing.setTotal(daily.getTotal());
        existing.setSuccess(daily.getSuccess());
        existing.setFail(daily.getFail());
        existing.setUpdateTime(now);
        operationTemplate.execute(existing, CommandEnum.OPR_EDIT);
    }

    @Override
    public void removeBefore(String cutoffStatDate) {
        if (StringUtil.isBlank(cutoffStatDate)) {
            return;
        }
        List<MetaAgg> all = operationTemplate.queryList(StorageEnum.META_DAILY, new Query(), MetaAgg.class);
        if (CollectionUtils.isEmpty(all)) {
            return;
        }
        for (MetaAgg row : all) {
            if (row == null || StringUtil.isBlank(row.getId()) || StringUtil.isBlank(row.getStatDate())) {
                continue;
            }
            if (row.getStatDate().compareTo(cutoffStatDate) < 0) {
                storageService.remove(StorageEnum.META_DAILY, row.getId());
            }
        }
    }
}
