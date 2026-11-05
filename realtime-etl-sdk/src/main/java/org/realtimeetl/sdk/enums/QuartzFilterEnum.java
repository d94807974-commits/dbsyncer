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
package org.realtimeetl.sdk.enums;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.listener.QuartzFilter;
import org.realtimeetl.sdk.listener.filter.DateFilter;
import org.realtimeetl.sdk.listener.filter.TimestampFilter;
import org.realtimeetl.sdk.listener.filter.YesDateFilter;
import org.realtimeetl.sdk.listener.filter.YesTimestampFilter;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/05/30 14:19
 */
public enum QuartzFilterEnum {

    /**
     * TimePoke(Start)
     */
    TIME_STAMP_BEGIN(1, "$timestamp_begin$", "System timestamp (start)", new TimestampFilter(true)),
    /**
     * TimePoke(End)
     */
    TIME_STAMP_END(2, "$timestamp_end$", "System timestamp (end)", new TimestampFilter(false)),
    /**
     * Date(Start)
     */
    DATE_BEGIN(3, "$date_begin$", "System date (start)", new DateFilter(true)),
    /**
     * Date(End)
     */
    DATE_END(4, "$date_end$", "System date (end)", new DateFilter(false)),
    /**
     * Yesterday.Date(Start)
     */
    DATE_YES_BEGIN(5, "$date_yes_begin$", "Yesterday date (start 00:00:00)", new YesDateFilter(true)),
    /**
     * Yesterday.Date(End)
     */
    DATE_YES_END(6, "$date_yes_end$", "Yesterday date (end 23:59:59)", new YesDateFilter(false)),
    /**
     * Yesterday.TimePoke(Start)
     */
    TIME_STAMP_YES_BEGIN(7, "$timestamp_yes_begin$", "Yesterday timestamp (start 00:00:00)", new YesTimestampFilter(true)),
    /**
     * Yesterday.TimePoke(End)
     */
    TIME_STAMP_YES_END(8, "$timestamp_yes_end$", "Yesterday timestamp (end 23:59:59)", new YesTimestampFilter(false));

    private final int index;
    private final String type;
    private final String message;
    private final QuartzFilter quartzFilter;

    QuartzFilterEnum(int index, String type, String message, QuartzFilter quartzFilter) {
        this.index = index;
        this.type = type;
        this.message = message;
        this.quartzFilter = quartzFilter;
    }

    public static QuartzFilterEnum getQuartzFilterEnum(String type) {
        for (QuartzFilterEnum e : QuartzFilterEnum.values()) {
            if (StringUtil.equals(type, e.getType())) {
                return e;
            }
        }
        return null;
    }

    /**
     * YesNoYesScheduledIncrementalSystem placeholderFull/count Waiting for unheard.Replacefrom time to time)
     */
    public static boolean isSystemPlaceholder(String value) {
        return StringUtil.isNotBlank(value) && getQuartzFilterEnum(value.trim()) != null;
    }

    public int getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public QuartzFilter getQuartzFilter() {
        return quartzFilter;
    }
}
