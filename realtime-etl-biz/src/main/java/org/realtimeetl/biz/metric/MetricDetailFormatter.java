package org.realtimeetl.biz.metric;

import org.realtimeetl.biz.vo.MetricResponseVO;

public interface MetricDetailFormatter {

    void format(MetricResponseVO vo);

}