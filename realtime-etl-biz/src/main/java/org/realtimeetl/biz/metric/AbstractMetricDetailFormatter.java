package org.realtimeetl.biz.metric;

import org.realtimeetl.biz.vo.MetricResponseVO;
import org.realtimeetl.common.util.CollectionUtils;

public abstract class AbstractMetricDetailFormatter implements MetricDetailFormatter {

    protected abstract void apply(MetricResponseVO vo);

    @Override
    public void format(MetricResponseVO vo) {
        if (CollectionUtils.isEmpty(vo.getMeasurements())) {
            return;
        }
        apply(vo);
    }
}
