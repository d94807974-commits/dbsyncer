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
package org.realtimeetl.sdk.listener;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.enums.QuartzFilterEnum;
import org.realtimeetl.sdk.model.Point;
import org.realtimeetl.sdk.model.TableGroupQuartzCommand;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Relationship DataLibraryScheduledExtract
 *
 * @version 1.0.0
 * @author Realtime ETL
 * @date 2021-09-01 20:35
 */
public final class DatabaseQuartzListener extends AbstractQuartzListener {

    @Override
    protected Point checkLastPoint(TableGroupQuartzCommand cmd, int index) {
        // InspectionYesNoExistenceSystem Parameters
        Map<String, String> command = cmd.getCommand();
        final String query = command.get(ConnectorConstant.OPERTION_QUERY);

        AtomicBoolean reversed = new AtomicBoolean();
        AtomicLong lastIndex = new AtomicLong();
        List<QuartzFilterEnum> filterEnums = Stream.of(QuartzFilterEnum.values()).sorted(Comparator.comparing(QuartzFilterEnum::getIndex)).filter(f-> {
            int currentIndex = StringUtil.indexOf(query, f.getType());
            Assert.isTrue((currentIndex == StringUtil.lastIndexOf(query, f.getType())), String.format("Multiple system parameters found for %s.", f.getType()));
            boolean exist = StringUtil.contains(query, f.getType());
            if (exist && !reversed.get()) {
                reversed.set(lastIndex.get() > currentIndex);
                lastIndex.set(currentIndex);
            }
            return exist;
        }).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(filterEnums)) {
            return new Point(command, new ArrayList<>());
        }

        Point point = new Point();
        // ExistenceSystem Parameters，Replace
        String replaceQuery = query;
        String replaceQueryCursor = command.get(ConnectorConstant.OPERTION_QUERY_CURSOR);
        for (QuartzFilterEnum quartzFilter : filterEnums) {
            final String type = quartzFilter.getType();
            final QuartzFilter f = quartzFilter.getQuartzFilter();

            // ReplaceCharacter
            replaceQuery = replaceType(replaceQuery, type);
            replaceQueryCursor = replaceType(replaceQueryCursor, type);

            // CreateParameterIndexkey
            final String key = index + type;

            // Organisation Authorities
            if (f.begin()) {
                if (!snapshot.containsKey(key)) {
                    final Object val = f.getObject();
                    point.addArg(val);
                    snapshot.put(key, f.toString(val));
                    continue;
                }

                // Read HistoryIncrementalPoints 71
                Object val = f.getObject((String) snapshot.get(key));
                point.addArg(val);
                point.setBeginKey(key);
                point.setBeginValue(f.toString(f.getObject()));
                continue;
            }
            // End Location(Refresh)
            Object val = f.getObject();
            point.addArg(val);
            point.setBeginValue(f.toString(val));
        }
        point.setCommand(ConnectorConstant.OPERTION_QUERY, replaceQuery);
        if (StringUtil.isNotBlank(replaceQueryCursor)) {
            point.setCommand(ConnectorConstant.OPERTION_QUERY_CURSOR, replaceQueryCursor);
        }
        if (reversed.get()) {
            point.reverseArgs();
        }

        return point;
    }

    private String replaceType(String replaceQuery, String type) {
        if (StringUtil.isNotBlank(replaceQuery)) {
            replaceQuery = StringUtil.replace(replaceQuery, "'" + type + "'", "?");
            return StringUtil.replace(replaceQuery, type, "?");
        }
        return replaceQuery;
    }

}