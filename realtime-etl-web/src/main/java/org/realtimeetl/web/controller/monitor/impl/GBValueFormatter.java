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
package org.realtimeetl.web.controller.monitor.impl;

import org.realtimeetl.web.controller.monitor.ValueFormatter;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2025-12-21 00:29
 */
@Service
public final class GBValueFormatter implements ValueFormatter<Object, BigDecimal> {

    @Override
    public BigDecimal formatValue(Object value) {
        BigDecimal decimal = new BigDecimal(String.valueOf(value));
        BigDecimal bt = divide(decimal, 0);
        BigDecimal mb = divide(bt, 0);
        return divide(mb, 2);
    }

    private BigDecimal divide(BigDecimal d1, int scale) {
        return d1.divide(new BigDecimal("1024"), scale, RoundingMode.HALF_UP);
    }
}
