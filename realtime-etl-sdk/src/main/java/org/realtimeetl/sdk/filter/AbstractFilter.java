/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.sdk.filter;

import org.realtimeetl.sdk.enums.FilterTypeEnum;
import org.realtimeetl.sdk.model.Filter;

/**
 * Filter Syntax:
 *
 * @Author AE86
 * @Version 1.0.0
 * @Date 2019-11-17 23:56
 */
public abstract class AbstractFilter extends Filter {

    /**
     * BackYesNoHighlight
     */
    private boolean enableHighLightSearch;

    /**
     * ParameterType
     *
     * @return
     */
    public abstract FilterTypeEnum getFilterTypeEnum();

    public boolean isEnableHighLightSearch() {
        return enableHighLightSearch;
    }

    public void setEnableHighLightSearch(boolean enableHighLightSearch) {
        this.enableHighLightSearch = enableHighLightSearch;
    }
}
