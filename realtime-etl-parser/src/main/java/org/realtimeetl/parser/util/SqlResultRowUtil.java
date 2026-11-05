/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.util;

import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SqlQuery ResultStandardize Revenue: IgnoreMappingBackCompetences for listing are written by a fixed camel.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-08-10
 */
public final class SqlResultRowUtil {

    private SqlResultRowUtil() {
    }

    /**
     * WillSQL ResultLine by aliasesListThe projection is a stable hump.Field（AvoidJSON Come on.TYPE/UPDATETIME）。
     *
     * @param sqlRow  MappingBackYes, I can.null
     * @param aliases Objective CombinedAli Physical aliases (maintain order)
     * @return New only with aliases.Map
     */
    public static Map<String, Object> toAliasRow(Map<String, Object> sqlRow, String[] aliases) {
        Map<String, Object> src = new LinkedCaseInsensitiveMap<>();
        if (sqlRow != null) {
            src.putAll(sqlRow);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        if (aliases == null) {
            return row;
        }
        for (String alias : aliases) {
            if (alias != null && src.containsKey(alias)) {
                row.put(alias, src.get(alias));
            }
        }
        return row;
    }
}
