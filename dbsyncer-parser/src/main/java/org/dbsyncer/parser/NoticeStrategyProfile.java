/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.parser;

import org.dbsyncer.parser.model.NoticeStrategyConfig;

import java.util.List;

/**
 * Police strategy CorruptRead and write.
 *
 * @author wuji
 * @version 1.0.0
 */
public interface NoticeStrategyProfile {

    /**
     * AllStrategy.
     */
    List<NoticeStrategyConfig> listAll();

    /**
     * PressPrimary keyquery.
     */
    NoticeStrategyConfig get(String id);

    /**
     * Insert。
     */
    String add(NoticeStrategyConfig config);

    /**
     * Update.
     */
    String update(NoticeStrategyConfig config);

    /**
     * PressPrimary keyDelete。
     */
    void remove(String id);
}
