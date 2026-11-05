package org.realtimeetl.biz.metric.impl;

import org.realtimeetl.biz.metric.AbstractMetricDetailFormatter;
import org.realtimeetl.biz.model.Sample;
import org.realtimeetl.biz.vo.MetricResponseVO;

public final class ValueMetricDetailFormatter extends AbstractMetricDetailFormatter {

    @Override
    public void apply(MetricResponseVO vo) {
        Sample sample = vo.getMeasurements().get(0);
        vo.setDetail(String.valueOf(sample.getValue()));
    }

}