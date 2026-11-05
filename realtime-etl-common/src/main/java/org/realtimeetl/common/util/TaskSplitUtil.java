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
package org.realtimeetl.common.util;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-07-10 17:47
 */
public final class TaskSplitUtil {

    public static <T> void split(List<T> rows, int batchNum, Consumer<List<T>> consumer) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        int effectiveBatch = batchNum > 0 ? batchNum : rows.size();
        int total = rows.size();
        for (int from = 0; from < total; from += effectiveBatch) {
            consumer.accept(rows.stream().skip(from).limit(effectiveBatch).collect(Collectors.toList()));
        }
    }
}
