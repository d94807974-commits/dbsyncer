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
package org.realtimeetl.common.metric;

import java.util.concurrent.atomic.LongAdder;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2024-06-02 22:45
 */
public final class Bucket {

    private final LongAdder longAdder = new LongAdder();

    private long time;

    public Bucket(long time) {
        this.time = time;
    }

    public void add(long count) {
        longAdder.add(count);
    }

    public long get() {
        return longAdder.sum();
    }

    public void reset(long time) {
        this.time = time;
        this.longAdder.reset();
    }

    public long getTime() {
        return time;
    }
}
