package org.realtimeetl.web.controller.monitor.impl;

import org.realtimeetl.web.controller.monitor.ValueFormatter;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public final class CpuValueFormatter implements ValueFormatter<Object, Object> {

    @Override
    public Object formatValue(Object value) {
        Double val = (Double) value;
        // Micrometer system.cpu.usage When not availableBack -1（If you like, you like.min Windows JVM）
        if (val == null || Double.isNaN(val) || val < 0) {
            return 0.0;
        }
        val *= 100;
        if (val > 100) {
            val = 100.0;
        }
        String percent = String.format("%.2f", val);
        return new BigDecimal(percent).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
