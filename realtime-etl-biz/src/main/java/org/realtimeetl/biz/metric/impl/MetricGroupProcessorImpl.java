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
package org.realtimeetl.biz.metric.impl;

import org.realtimeetl.biz.enums.MetricEnum;
import org.realtimeetl.biz.metric.MetricGroupProcessor;
import org.realtimeetl.biz.vo.MetricResponseVO;
import org.realtimeetl.common.util.StringUtil;

import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public final class MetricGroupProcessorImpl implements MetricGroupProcessor {

    @Override
    public List<MetricResponseVO> process(List<MetricResponseVO> metrics) {
        Map<String, MetricResponseVO> group = new LinkedHashMap<>();
        Iterator<MetricResponseVO> iterator = metrics.iterator();
        while (iterator.hasNext()) {
            MetricResponseVO metric = iterator.next();
            // Application of performance indicators
            MetricEnum metricEnum = MetricEnum.getMetric(metric.getCode());
            if (metricEnum != null) {
                switch (metricEnum) {
                    case THREADS_LIVE:
                    case THREADS_PEAK:
                        buildMetricResponseVo(group, metricEnum.getGroup(), metricEnum.getMetricName(), metric.getDetail());
                        iterator.remove();
                        break;
                    default:
                        break;
                }
            }
        }
        metrics.addAll(0, group.values());
        group.clear();
        return metrics;
    }

    private void buildMetricResponseVo(Map<String, MetricResponseVO> group, String groupName, String metricName, String detail) {
        MetricResponseVO vo = getMetricResponseVo(group, groupName);
        vo.setDetail(vo.getDetail() + metricName + StringUtil.COLON + detail + StringUtil.SPACE);
    }

    private MetricResponseVO getMetricResponseVo(Map<String, MetricResponseVO> group, String groupName) {
        return group.compute(groupName, (k, v)-> {
            if (v == null) {
                MetricResponseVO responseVo = new MetricResponseVO();
                responseVo.setGroup(groupName);
                responseVo.setMetricName(StringUtil.EMPTY);
                responseVo.setDetail(StringUtil.EMPTY);
                return responseVo;
            }
            return v;
        });
    }
}