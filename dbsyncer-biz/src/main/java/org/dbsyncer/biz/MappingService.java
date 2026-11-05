/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.biz.vo.MappingCustomTableVO;
import org.dbsyncer.biz.vo.MappingListVO;
import org.dbsyncer.biz.vo.MappingVO;
import org.dbsyncer.common.model.Paging;
import org.dbsyncer.sdk.model.Table;

import java.util.List;
import java.util.Map;

/**
 * @author AE86
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
     * minPageSearch（ListLightField，Other OrganiserConfigure/ConnectorSensitive.Info）
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
