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
package org.realtimeetl.sdk.connector;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.schema.SchemaResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractConnector {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void convertProcessBeforeWriter(PluginContext context, SchemaResolver targetResolver) {
        if (CollectionUtils.isEmpty(context.getTargetFields()) || CollectionUtils.isEmpty(context.getTargetList()) || targetResolver == null) {
            return;
        }

        for (Map row : context.getTargetList()) {
            for (Field f : context.getTargetFields()) {
                if (null == f) {
                    continue;
                }
                try {
                    row.compute(f.getName(), (k, v)->targetResolver.convert(v, f));
                } catch (Exception e) {
                    logger.error(String.format("convert value error: (%s, %s, %s)", context.getTargetTable().getName(), f.getName(), row.get(f.getName())), e);
                    throw new SdkException(e);
                }
            }
        }
    }

    protected boolean isUpdate(String event) {
        return StringUtil.equals(ConnectorConstant.OPERTION_UPDATE, event);
    }

    protected boolean isInsert(String event) {
        return StringUtil.equals(ConnectorConstant.OPERTION_INSERT, event);
    }

    protected boolean isDelete(String event) {
        return StringUtil.equals(ConnectorConstant.OPERTION_DELETE, event);
    }
}
