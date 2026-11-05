/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.model;

import java.io.Serializable;

/**
 * Table migration snapshot (maps by table)index Index）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-05-29 11:30
 */
public class DatabaseSyncTableSnapshot extends CommonTaskSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private long successTotal;

    private long failTotal;

    public long getSuccessTotal() {
        return successTotal;
    }

    public void setSuccessTotal(long successTotal) {
        this.successTotal = successTotal;
    }

    public long getFailTotal() {
        return failTotal;
    }

    public void setFailTotal(long failTotal) {
        this.failTotal = failTotal;
    }
}
