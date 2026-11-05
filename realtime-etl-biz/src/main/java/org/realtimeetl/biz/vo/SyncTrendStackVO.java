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
package org.realtimeetl.biz.vo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Sync TrendsData Fact
 *
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2025-12-20 02:33
 */
public class SyncTrendStackVO {

    private List<String> labels;

    private List<Long> success;

    private List<Long> fail;

    public SyncTrendStackVO() {
        this.labels = new CopyOnWriteArrayList<>();
        this.success = new CopyOnWriteArrayList<>();
        this.fail = new CopyOnWriteArrayList<>();
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Long> getSuccess() {
        return success;
    }

    public void setSuccess(List<Long> success) {
        this.success = success;
    }

    public List<Long> getFail() {
        return fail;
    }

    public void setFail(List<Long> fail) {
        this.fail = fail;
    }
}
