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
package org.realtimeetl.parser.util;

import org.realtimeetl.common.util.StringUtil;

/**
 * Meta Ensure:FullTogether{@code incrementMeta} and snapshot flush TotalUse to avoid line writing backOverwriteExclusive Refer POLICY Continuity Accommodation Moratorium
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-08-11
 */
public abstract class MetaLockUtil {

    /**
     * PresstaskId Accusation locks within the acquisition process ({@link String#intern()}）。
     *
     * @param taskId Task ID
     * @return Lock/42;
     */
    public static Object lock(String taskId) {
        String id = StringUtil.isBlank(taskId) ? StringUtil.EMPTY : taskId;
        return ("meta-write-" + id).intern();
    }
}
