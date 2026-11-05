/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.sdk.model.NoticeStrategyDef;

import java.util.List;
import java.util.Map;

/**
 * Police strategy CorruptOperations.
 *
 * @author wuji
 * @version 1.0.0
 */
public interface NoticeStrategyService {

    /**
     * AllPolicyDTO，ForPageNoodles.andRun-timeConvert）。
     */
    List<NoticeStrategyDef> listAll();

    /**
     * InsertOr update a strategy, effective immediately.
     *
     * @param params id（EmptyInsert）、name、frequency、cron、enabled、noticeTypes（Commaminamend
     * @return PolicyID
     */
    String save(Map<String, String> params);

    /**
     * PressID DeleteStrategy, effective immediately.
     *
     * @param params must containid
     * @return DeletedOther OrganiserID
     */
    String remove(Map<String, String> params);
}
