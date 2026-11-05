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
package org.realtimeetl.sdk.model;

import java.io.Serializable;

/**
 * Table migration snapshot (maps by table)index Index）。
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-05-29 11:30
 */
public class DatabaseSyncTableSnapshot extends CommonTaskSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private long successTotal;

    private long failTotal;

    public long getSuccessTotal() {
        return successTotal;
    }

    public void setSuccessTotal(long successTotal) {
        this.successTotal = successTotal;
    }

    public long getFailTotal() {
        return failTotal;
    }

    public void setFailTotal(long failTotal) {
        this.failTotal = failTotal;
    }
}
