/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.plugin;

import org.realtimeetl.sdk.filter.BooleanFilter;

/**
 * @author wuji
 * @version 1.0.0
 * @date 2026-04-22 13:47
 */
public interface CustomContext {

    /**
     * Get FilterCondition
     */
    BooleanFilter getFilter();

}