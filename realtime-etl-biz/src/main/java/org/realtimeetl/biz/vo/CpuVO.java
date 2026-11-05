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
package org.realtimeetl.biz.vo;

import java.math.BigDecimal;

/**
 * @Author Realtime ETL.
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
