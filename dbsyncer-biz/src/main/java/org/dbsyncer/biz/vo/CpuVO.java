/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.biz.vo;

import java.math.BigDecimal;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-12-20 21:22
 */
public final class CpuVO extends HistoryStackVO {

    private final int core = Runtime.getRuntime().availableProcessors();

    // UserUse 100minThat's right.
    private BigDecimal userPercent;
    // Systematically use 100%minThat's right.
    private BigDecimal sysPercent;
    // Total usageminThat's right.
    private BigDecimal totalPercent;

    public int getCore() {
        return core;
    }

    public BigDecimal getUserPercent() {
        return userPercent;
    }

    public void setUserPercent(BigDecimal userPercent) {
        this.userPercent = userPercent;
    }

    public BigDecimal getSysPercent() {
        return sysPercent;
    }

    public void setSysPercent(BigDecimal sysPercent) {
        this.sysPercent = sysPercent;
    }

    public BigDecimal getTotalPercent() {
        return totalPercent;
    }

    public void setTotalPercent(BigDecimal totalPercent) {
        this.totalPercent = totalPercent;
    }
}
