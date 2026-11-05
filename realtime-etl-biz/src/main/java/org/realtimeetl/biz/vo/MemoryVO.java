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
 * @Date 2025-12-20 21:17
 */
public final class MemoryVO extends HistoryStackVO {

    private BigDecimal jvmUsed;
    private BigDecimal jvmTotal;
    private BigDecimal sysUsed;
    private BigDecimal sysTotal;
    // Total usageminThat's right.
    private BigDecimal totalPercent;

    public BigDecimal getJvmUsed() {
        return jvmUsed;
    }

    public void setJvmUsed(BigDecimal jvmUsed) {
        this.jvmUsed = jvmUsed;
    }

    public BigDecimal getJvmTotal() {
        return jvmTotal;
    }

    public void setJvmTotal(BigDecimal jvmTotal) {
        this.jvmTotal = jvmTotal;
    }

    public BigDecimal getSysUsed() {
        return sysUsed;
    }

    public void setSysUsed(BigDecimal sysUsed) {
        this.sysUsed = sysUsed;
    }

    public BigDecimal getSysTotal() {
        return sysTotal;
    }

    public void setSysTotal(BigDecimal sysTotal) {
        this.sysTotal = sysTotal;
    }

    public BigDecimal getTotalPercent() {
        return totalPercent;
    }

    public void setTotalPercent(BigDecimal totalPercent) {
        this.totalPercent = totalPercent;
    }

}
