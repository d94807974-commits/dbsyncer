/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.dbsyncer.plugin.impl;

import org.dbsyncer.common.config.AppConfig;
import org.dbsyncer.sdk.plugin.PluginContext;
import org.dbsyncer.sdk.spi.PluginService;

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