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
 * @Date 2025-12-20 20:50
 */
public final class DiskSpaceVO {

    private BigDecimal used;
    private BigDecimal free;
    private BigDecimal total;
    private BigDecimal usedPercent;

    public BigDecimal getUsed() {
        return used;
    }

    public void setUsed(BigDecimal used) {
        this.used = used;
    }

    public BigDecimal getFree() {
        return free;
    }

    public void setFree(BigDecimal free) {
        this.free = free;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getUsedPercent() {
        return usedPercent;
    }

    public void setUsedPercent(BigDecimal usedPercent) {
        this.usedPercent = usedPercent;
    }
}
