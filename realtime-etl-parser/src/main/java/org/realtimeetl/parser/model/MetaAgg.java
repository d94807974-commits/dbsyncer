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
package org.realtimeetl.parser.model;

import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.sdk.constant.ConfigConstant;

/**
 * Sync task Meta Day summary{@code realtime_etl_meta_agg}），ForFirst pageSync TrendsUse.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public class MetaAgg extends ConfigModel {

    /**
     * Statistical date yyy-MM-dd
     */
    private String statDate;

    /**
     * Sync task Meta.total Total
     */
    private long total;

    /**
     * Sync task Meta.success Total
     */
    private long success;

    /**
     * Sync task Meta.fail Total
     */
    private long fail;

    public MetaAgg() {
        super.setName("");
        super.setType(ConfigConstant.META_DAILY);
    }

    public String getStatDate() {
        return statDate;
    }

    public void setStatDate(String statDate) {
        this.statDate = statDate;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSuccess() {
        return success;
    }

    public void setSuccess(long success) {
        this.success = success;
    }

    public long getFail() {
        return fail;
    }

    public void setFail(long fail) {
        this.fail = fail;
    }
}
