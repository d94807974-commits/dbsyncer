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

import org.realtimeetl.biz.vo.MappingCustomTableVO;
import org.realtimeetl.biz.vo.MappingListVO;
import org.realtimeetl.biz.vo.MappingVO;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.sdk.model.Table;

import java.util.List;
import java.util.Map;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2019/10/17 23:14
 */
public interface MappingService {

    /**
     * InsertMapping
     *
     * @param params
     */
    String add(Map<String, String> params);

    /**
     * CopyMapping
     *
     * @param id       SourceTasks ID
     * @param username ActionsWhoUsername（For awarding newResource ACL）
     */
    String copy(String id, String username);

    /**
     * UpdateMapping
     *
     * @param params
     */
    String edit(Map<String, String> params);

    /**
     * DeleteMapping
     *
     * @param id
     */
    String remove(String id);

    /**
     * AccessMapping（Without AllVolumeTableList，Please leave.{@link #searchTables}）
     *
     * @param id
     * @return
     */
    MappingVO getMapping(String id);

    /**
     * AccessMappingCustom tableInfo
     *
     * @param id
     * @param type
     * @return
     */
    MappingCustomTableVO getMappingCustomTable(String id, String type);

    /**
     * 分页搜索（列表轻量字段，不含完整配置/连接器敏感信息）
     *
     * @param params
     * @return
     */
    Paging<MappingListVO> search(Map<String, String> params);

    /**
     * Sync taskDetails：Summary by MapSuccess/Failures。
     *
     * @param params id=mappingId, detailStatus=fail|success|Empty, pageNum, pageSize
     * @return minPageMap Summary
     */
    Paging searchTableGroupResult(Map<String, String> params);

    /**
     * StartMapping
     *
     * @param id
     */
    String start(String id);

    /**
     * StopMapping
     *
     * @param id
     */
    String stop(String id);

    /**
     * RefreshMappingSourceandTarget table
     *
     * @param id
     */
    String refreshMappingTables(String id);

    /**
     * minPageSearchMappingTable (for bottom frame remote)Search）
     *
     * @param params
     * @return
     */
    Paging<Table> searchTables(Map<String, String> params);

    /**
     * QuestionCustom table
     *
     * @param params
     * @return
     */
    List<Table> searchCustomTable(Map<String, String> params);

    /**
     * Save Custom Table
     *
     * @param params
     * @return
     */
    String saveCustomTable(Map<String, String> params);

    /**
     * Delete custom table
     *
     * @param params
     * @return
     */
    String removeCustomTable(Map<String, String> params);
}
