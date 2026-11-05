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

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.sdk.model.Table;

/**
 * TableInfo
 *
 * @Author Realtime ETL.
 * @Version 1.0.0
 * @Date 2026-01-08 21:41
 */
public final class TableVO extends Table {

    public String getColumnJson() {
        return JsonUtil.objToJson(super.getColumn());
    }

    public String getExtInfoJson() {
        return JsonUtil.objToJson(super.getExtInfo());
    }
}
