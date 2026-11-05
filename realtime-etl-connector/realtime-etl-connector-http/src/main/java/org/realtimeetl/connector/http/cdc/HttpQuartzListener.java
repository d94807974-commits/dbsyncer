/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.http.cdc;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.http.constant.HttpConstant;
import org.realtimeetl.connector.http.util.HttpUtil;
import org.realtimeetl.sdk.enums.QuartzFilterEnum;
import org.realtimeetl.sdk.listener.AbstractQuartzListener;
import org.realtimeetl.sdk.listener.QuartzFilter;
import org.realtimeetl.sdk.model.Point;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.model.TableGroupQuartzCommand;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CDC-Http ScheduledListener
 * Yes.checkLastPoint One-time basissnapshot & CurrentTimeGenerateTime/date type placeholdersReplaceValue，
 * and $pageIndex$、$pageSize$、$cursor$ Yes.reader UnifiedReplace。
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-02-02 00:01
 */
public final class HttpQuartzListener extends AbstractQuartzListener {

    @Override
    public void init() {
        super.init();
        // Initialise CursorParameter
        getCommands().forEach(cmd -> {
            Properties extInfo = cmd.getTable().getExtInfo();
            String paramsTemplate = extInfo.getProperty(HttpConstant.PARAMS);
            List<String> cursorKeys = new ArrayList<>();
            if (StringUtil.isNotBlank(paramsTemplate)) {
                Properties template = HttpUtil.parse(paramsTemplate);
                for (Map.Entry<Object, Object> entry : template.entrySet()) {
                    String key = (String) entry.getKey();
                    String raw = (String) entry.getValue();
                    String val = raw != null ? StringUtil.trim(raw) : "";
                    if (HttpConstant.CURSOR.equals(val)) {
                        cursorKeys.add(key);
                    }
                }
            }
            cmd.setCursorKeys(cursorKeys);
        });
    }

    @Override
    protected Point checkLastPoint(TableGroupQuartzCommand cmd, int index) {
        Point point = new Point();

        // From TableConfigureReadPARAMS Template 3/Scan OFTime/Date class system placeholder
        String paramsTemplate = cmd.getTable() != null && cmd.getTable().getExtInfo() != null
                ? cmd.getTable().getExtInfo().getProperty(HttpConstant.PARAMS)
                : null;
        if (StringUtil.isBlank(paramsTemplate)) {
            return point;
        }

        // Finds out what happens in the templateQuartzFilterEnum Placeholder (byindex Sort,and DB Unanimously)
        List<QuartzFilterEnum> filterEnums = Stream.of(QuartzFilterEnum.values())
                .sorted(Comparator.comparing(QuartzFilterEnum::getIndex))
                .filter(f -> StringUtil.contains(paramsTemplate, f.getType()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(filterEnums)) {
            return point;
        }

        // HarmonizationGenerateStatic variable: placeholder -> ReplaceValue（Time/WORKING DATE) and MAINTENANCEsnapshot
        Map<String, Object> staticVars = new LinkedHashMap<>();
        for (QuartzFilterEnum filterEnum : filterEnums) {
            String type = filterEnum.getType();
            QuartzFilter f = filterEnum.getQuartzFilter();
            String key = index + type;

            if (f.begin()) {
                if (!snapshot.containsKey(key)) {
                    Object val = f.getObject();
                    String valueStr = f.toString(val);
                    snapshot.put(key, valueStr);
                    staticVars.put(type, valueStr);
                } else {
                    String valueStr = (String) snapshot.get(key);
                    staticVars.put(type, valueStr);
                    point.setBeginKey(key);
                    point.setBeginValue(f.toString(f.getObject()));
                }
            } else {
                Object val = f.getObject();
                String valueStr = f.toString(val);
                staticVars.put(type, valueStr);
                point.setBeginValue(valueStr);
            }
        }

        point.setCommand(HttpConstant.HTTP_INCREMENT_VARS, JsonUtil.objToJson(staticVars));
        return point;
    }

    @Override
    protected boolean isSupportedCursor(TableGroupQuartzCommand cmd) {
        Table table = cmd.getTable();
        String property = table.getExtInfo().getProperty(HttpConstant.PARAMS);
        return StringUtil.contains(property, HttpConstant.CURSOR);
    }

    @Override
    protected Object[] getLastCursors(TableGroupQuartzCommand cmd, List<Map> data) {
        return PrimaryKeyUtil.getLastCursors(data, cmd.getCursorKeys());
    }
}