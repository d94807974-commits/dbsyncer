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
package org.realtimeetl.biz.metric;

import org.realtimeetl.biz.vo.MetricResponseVO;

import java.util.List;

/**
 * MergeGroupIndicators
 *
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2025-04-15 23:13
 */
public interface MetricGroupProcessor {

    List<MetricResponseVO> process(List<MetricResponseVO> metrics);
}