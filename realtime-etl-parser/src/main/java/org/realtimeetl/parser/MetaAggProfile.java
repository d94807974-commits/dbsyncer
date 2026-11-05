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

import org.realtimeetl.parser.model.MetaAgg;

import java.util.List;

/**
 * Sync task Meta Day summary reading and writing.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
public interface MetaAggProfile {

    /**
     * Query by statistical day.
     */
    MetaAgg getByStatDate(String statDate);

    /**
     * Some recently.days（PressSTAT_DATE Raised, maximum{@code days} (Article Concerning Article).
     */
    List<MetaAgg> listRecent(int days);

    /**
     * InsertOr pressSTAT_DATE Updates the summary of the day.
     */
    void upsert(MetaAgg daily);

    /**
     * DeleteLines earlier than the date specified (excludingcutoff）。
     */
    void removeBefore(String cutoffStatDate);
}
