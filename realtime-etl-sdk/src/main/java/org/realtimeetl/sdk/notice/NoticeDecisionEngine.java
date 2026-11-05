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
package org.realtimeetl.sdk.notice;

import org.realtimeetl.sdk.model.NoticeContent;
import org.realtimeetl.sdk.model.NoticeStrategy;

import java.util.List;

/**
 * Message Notification Decision Engine: Matching by strategy, restricted flow, merged and delivered.
 *
 * @author Realtime ETL
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
