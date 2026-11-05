/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.biz;

import org.realtimeetl.parser.model.NoticeRule;

import java.util.List;
import java.util.Map;

/**
 * Alert notificationRules.
 *
 * @author wuji
 * @version 1.0.0
 */
public interface NoticeRuleService {

    /**
     * AllRules.
     */
    List<NoticeRule> listAll();

    /**
     * Insertor update.
     */
    String save(Map<String, String> params);

    /**
     * PressPrimary keyDelete。
     */
    String remove(Map<String, String> params);
}
