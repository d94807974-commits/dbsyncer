/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.notice;

import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.model.NoticeStrategy;

import java.util.List;

/**
 * Message Notification Decision Engine: Matching by strategy, restricted flow, merged and delivered.
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-06-16 12:00
 */
public interface NoticeDecisionEngine {

    /**
     * Receiving operationsEventAnd by strategic route.
     *
     * @param noticeContent Contents of notice
     */
    void dispatch(NoticeContent noticeContent);

    /**
     * Use given policyListRebuild Operating State(s)Registry / Management.
     * <p>Policy data loaded from storage by caller, engine not dependentConfigure Profile，AvoidStartPeriod cycle MISC.
     *
     * @param strategies running time policies; emptyListOrganisationClear
     */
    void reload(List<NoticeStrategy> strategies);
}
