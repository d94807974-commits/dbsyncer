package org.realtimeetl.biz.metric.impl;

import org.realtimeetl.biz.metric.AbstractMetricDetailFormatter;
import org.realtimeetl.biz.model.Sample;
import org.realtimeetl.biz.vo.MetricResponseVO;

public final class DoubleRoundMetricDetailFormatter extends AbstractMetricDetailFormatter {

    @Override
    public void apply(MetricResponseVO vo) {
        Sample sample = vo.getMeasurements().get(0);
        long round = Math.round((Double) sample.getValue());
        vo.setDetail(String.valueOf(round));
    }

}