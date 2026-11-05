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
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.ConnectorConfigService;
import org.realtimeetl.common.scheduled.ScheduledTaskService;
import org.realtimeetl.sdk.service.ScheduledScanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * ConnectorHealth screening services
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-09-10
 */
@Service
public final class ConnectorHealthServiceImpl implements ScheduledScanService, DisposableBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String CONNECTOR_HEALTH_KEY = "connector-health";
    private static final long PERIOD_MS = 10_000L;

    @Resource
    private ConnectorConfigService connectorConfigService;

    @Resource
    private ScheduledTaskService scheduledTaskService;

    private volatile boolean started;

    @Override
    public synchronized void start() {
        if (started) {
            return;
        }
        scheduledTaskService.start(CONNECTOR_HEALTH_KEY, PERIOD_MS, connectorConfigService::refreshHealth);
        started = true;
        logger.info("connector health scheduler started");
    }

    @Override
    public synchronized void stop() {
        if (!started) {
            return;
        }
        scheduledTaskService.stop(CONNECTOR_HEALTH_KEY);
        started = false;
        logger.info("connector health scheduler stopped");
    }

    @Override
    public void destroy() {
        stop();
    }
}
