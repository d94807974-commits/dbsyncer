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
package org.realtimeetl.common.message.impl;

import org.realtimeetl.common.enums.CommonMessageTypeEnum;
import org.realtimeetl.common.message.CommonMessage;

/**
 * DeleteConfigureCan not open message
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-09-23 00:27
 */
public final class RemoveCacheMessage implements CommonMessage {

    private String id;
    private String configModelType;

    @Override
    public CommonMessageTypeEnum getType() {
        return CommonMessageTypeEnum.REMOVE_CONFIG_MODEL_CACHE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigModelType() {
        return configModelType;
    }

    public void setConfigModelType(String configModelType) {
        this.configModelType = configModelType;
    }
}
