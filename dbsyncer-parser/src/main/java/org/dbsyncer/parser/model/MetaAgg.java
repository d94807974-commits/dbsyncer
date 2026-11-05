/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser.model;

import org.dbsyncer.common.model.ConfigModel;
import org.dbsyncer.sdk.constant.ConfigConstant;

/**
 * Sync task Meta Day summary{@code dbsyncer_meta_agg}），ForFirst pageSync TrendsUse.
 *
 * @author wuji
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
