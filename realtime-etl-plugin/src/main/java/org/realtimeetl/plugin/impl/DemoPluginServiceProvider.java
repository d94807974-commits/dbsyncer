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
package org.realtimeetl.plugin.impl;

import org.realtimeetl.common.config.AppConfig;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.spi.PluginService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public final class DemoPluginServiceProvider implements PluginService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private AppConfig appConfig;

    @Override
    public void postProcessBefore(PluginContext context) {
        logger.info("Plugin processing sync. Target source table: {}", context.getTargetTable().getName());
    }

    @Override
    public void convert(PluginContext context) {
        context.setTerminated(true);
        logger.info("Plugin processing {}, data source table: }, target source table: }, event: {}, number of bars: }", context.getModelEnum().getName(), context.getSourceTable().getName(), context.getTargetTable().getName(), context.getEvent(), context.getTargetList()
                .size());
    }

    @Override
    public void postProcessAfter(PluginContext context) {
        logger.info("Plugin is processing synchronized data, target source table: {, event: {}, number of bars: }", context.getTargetTable().getName(), context.getEvent(), context.getTargetList().size());
    }

    @Override
    public String getVersion() {
        return appConfig.getVersion();
    }

    public String getName() {
        return "Demo";
    }
}