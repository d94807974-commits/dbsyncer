/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.elasticsearch.cdc;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.elasticsearch.ElasticsearchConnector;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.enums.QuartzFilterEnum;
import org.realtimeetl.sdk.listener.AbstractQuartzListener;
import org.realtimeetl.sdk.listener.QuartzFilter;
import org.realtimeetl.sdk.model.Filter;
import org.realtimeetl.sdk.model.Point;
import org.realtimeetl.sdk.model.TableGroupQuartzCommand;
import org.elasticsearch.ElasticsearchException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CDC-ESScheduledListener
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2021-09-01 20:35
 */
public final class ESQuartzListener extends AbstractQuartzListener {

    @Override
    public Map<String, String> captureSnapshot() {
        List<TableGroupQuartzCommand> cmdList = getCommands();
        if (CollectionUtils.isEmpty(cmdList)) {
            return Collections.emptyMap();
        }
        Map<String, String> captured = new HashMap<>();
        for (int i = 0; i < cmdList.size(); i++) {
            collectBeginQuartzPoint(cmdList.get(i), i, captured);
        }
        return captured;
    }

    /**
     * Full + incremental：Freeze at current timebegin CategorySystem Parameters（Like$timestamp_begin$），FullAfterIncrementalKeep pulling from that level.
     */
    private void collectBeginQuartzPoint(TableGroupQuartzCommand cmd, int index, Map<String, String> captured) {
        Map<String, String> command = cmd.getCommand();
        String filterJson = command.get(ConnectorConstant.OPERTION_QUERY_FILTER);
        if (StringUtil.isBlank(filterJson)) {
            return;
        }
        List<Filter> filters = JsonUtil.jsonToArray(filterJson, Filter.class);
        if (CollectionUtils.isEmpty(filters)) {
            return;
        }
        Set<String> seenBeginPlaceholder = new HashSet<>();
        for (Filter f : filters) {
            String placeholder = f.getValue();
            QuartzFilterEnum filterEnum = QuartzFilterEnum.getQuartzFilterEnum(placeholder);
            if (filterEnum == null) {
                continue;
            }
            if (!seenBeginPlaceholder.add(placeholder)) {
                throw new ElasticsearchException(String.format("Multiple system parameters found for %s.", placeholder));
            }
            if (!filterEnum.getQuartzFilter().begin()) {
                continue;
            }
            QuartzFilter quartzFilter = filterEnum.getQuartzFilter();
            String key = index + filterEnum.getType();
            String value = quartzFilter.toString(quartzFilter.getObject());
            captured.put(key, value);
            snapshot.put(key, value);
        }
    }

    @Override
    protected Point checkLastPoint(TableGroupQuartzCommand cmd, int index) {
        // InspectionYesNoExistenceSystem Parameters
        Map<String, String> command = cmd.getCommand();
        String filterJson = command.get(ConnectorConstant.OPERTION_QUERY_FILTER);
        if (StringUtil.isBlank(filterJson)) {
            return new Point(command, new ArrayList<>());
        }
        List<Filter> filters = JsonUtil.jsonToArray(filterJson, Filter.class);
        if (CollectionUtils.isEmpty(filters)) {
            return new Point(command, new ArrayList<>());
        }

        // ExistenceSystem Parameters，Replace
        Point point = new Point();
        Set<String> set = new HashSet<>();
        for (Filter f : filters) {
            if (set.contains(f.getValue())) {
                throw new ElasticsearchException(String.format("Multiple system parameters found for %s.", f.getValue()));
            }
            QuartzFilterEnum filterEnum = QuartzFilterEnum.getQuartzFilterEnum(f.getValue());
            if (null != filterEnum) {
                // Mark against weight
                set.add(f.getValue());

                final QuartzFilter quartzFilter = filterEnum.getQuartzFilter();

                // CreateParameterIndexkey
                final String key = index + filterEnum.getType();

                // Organisation Authorities
                if (quartzFilter.begin()) {
                    if (!snapshot.containsKey(key)) {
                        f.setValue(quartzFilter.toString(quartzFilter.getObject()));
                        snapshot.put(key, f.getValue());
                        continue;
                    }

                    // Read HistoryIncrementalPoints 71
                    f.setValue((String) snapshot.get(key));
                    point.setBeginKey(key);
                    point.setBeginValue(quartzFilter.toString(quartzFilter.getObject()));
                    continue;
                }
                // End Location(Refresh)
                f.setValue(quartzFilter.toString(quartzFilter.getObject()));
                point.setBeginValue(f.getValue());
            }
        }
        point.setCommand(ConnectorConstant.OPERTION_QUERY, command.get(ConnectorConstant.OPERTION_QUERY));
        point.setCommand(ConnectorConstant.OPERTION_QUERY_FILTER, JsonUtil.objToJson(filters));
        point.setCommand(ElasticsearchConnector._SOURCE_INDEX, command.get(ElasticsearchConnector._SOURCE_INDEX));
        return point;
    }
}
