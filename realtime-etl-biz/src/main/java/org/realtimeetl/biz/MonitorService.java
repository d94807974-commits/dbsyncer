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
package org.realtimeetl.biz;

import org.realtimeetl.biz.model.AppReportMetric;
import org.realtimeetl.biz.model.DashboardMetric;
import org.realtimeetl.biz.model.MetricResponse;
import org.realtimeetl.biz.vo.LogVO;
import org.realtimeetl.biz.vo.MetaVO;
import org.realtimeetl.common.model.Paging;

import java.util.List;
import java.util.Map;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/1/14 0:02
 */
public interface MonitorService {

    /**
     * minPageQuestionMappingMetaInfo（ForMonitorPageDraw Down RemoteSearch）
     *
     * @param params pageNum、pageSize、searchKey
     * @return minPageResult
     */
    Paging<MetaVO> queryMeta(Map<String, String> params);

    /**
     * QuestionSync data
     */
    Paging queryData(Map<String, String> params);

    /**
     * Empty Sync Data；tableGroupId Non-empty hours onlyClearThe table is fine.andTableMeta。
     */
    String clearData(String id, String tableGroupId);

    /**
     * QuestionOperation logs（MonitorPage，KeywordSearch.JSON）
     */
    Paging queryLog(Map<String, String> params);

    /**
     * QuestionLogin logs（TYPE 90/91）
     */
    Paging queryLoginLog(Map<String, String> params);

    /**
     * QuestionOperation logs（ExcludeLogin TYPE）
     */
    Paging queryOperLog(Map<String, String> params);

    /**
     * PressID QuestionLogDetails（Request/included requestBackParameter）
     */
    LogVO getLogDetail(String id);

    /**
     * ClearOperation logs
     */
    String clearLog();

    /**
     * DeleteOverdue data andLog
     */
    void deleteExpiredDataAndLog();

    /**
     * Access to application reports
     *
     * @return
     */
    AppReportMetric queryAppMetric(List<MetricResponse> metrics);

    /**
     * AccessDashboardReport
     *
     * @return
     */
    DashboardMetric queryDashboardMetric();

    /**
     * QuestionTable-group actuator
     *
     * @return
     */
    Paging<MetricResponse> queryActuator(Map<String, String> params);
}
