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
package org.realtimeetl.parser;

import org.realtimeetl.parser.model.UserConfig;
import org.realtimeetl.parser.model.UserInfo;

import java.util.List;

/**
 * User Configuration（Line one.UserDropLibrary；Aggregation Model{@link UserConfig} ForUI/ImportExport）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public interface UserProfile {

    /**
     * Get AggregationUser Configuration（NoneUserTimeBack null）。
     */
    UserConfig getUserConfig();

    /**
     * ListAllUserAll right.
     */
    List<UserInfo> listUserInfos();

    /**
     * PressAccountQuestionUserAll right.
     */
    UserInfo getByUsername(String username);

    /**
     * PressUser ID QuestionUserAll right.
     */
    UserInfo getById(String id);

    /**
     * PressAccountSync Add & DeleteUserRows (replacement)OperationTemplate.syncUserConfig）。
     *
     * @return FirstUser id（Old CompatibilityBackValue）
     */
    String saveUserConfig(UserConfig config);

    /**
     * UserLine number.
     */
    int countUsers();

    /**
     * Fromuser.json ArrayImport。
     */
    void importFromJson(String json);

    /**
     * UpdateLogin failedNumberandLock deadlineTime（SingleUserAll right.
     *
     * @param username       Account
     * @param loginFailCount FailedNumber
     * @param lockUntil      Lock deadlinemsTimePoke.0 Not locked
     * @return YesNoFind and update
     */
    boolean updateLoginLock(String username, int loginFailCount, long lockUntil);

    /**
     * Record it once.Login failed；Reaching the thresholdValueWrite lock deadlineTime。
     *
     * @param username    Account
     * @param maxAttempts ContinuousFailedUpper limit
     * @param lockMillis  LockScheduledLongms）
     * @return null OrganisationUser does not exist；NoIt's an update.YesNoLocked
     */
    Boolean recordLoginFailure(String username, int maxAttempts, long lockMillis);
}
