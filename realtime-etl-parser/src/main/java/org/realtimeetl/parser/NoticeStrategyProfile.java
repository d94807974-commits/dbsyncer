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

import org.realtimeetl.parser.model.NoticeStrategyConfig;

import java.util.List;

/**
 * Police strategy CorruptRead and write.
 *
 * @author Realtime ETL
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
