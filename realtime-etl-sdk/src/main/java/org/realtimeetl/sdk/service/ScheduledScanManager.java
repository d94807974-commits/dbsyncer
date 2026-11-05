/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.sdk.service;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wuji
 * @version 1.0.0
 * @date 2026-09-21 13:52
 */
@Component
public class ScheduledScanManager {

    @Resource
    private List<ScheduledScanService> scheduledScanServices;

    /**
     * StartConnectorHealth screeningScheduledTasks（I'm sorry.
     */
    public void start() {
        scheduledScanServices.forEach(ScheduledScanService::start);
    }

    /**
     * StopConnectorHealth screeningScheduledTasks（I'm sorry.
     */
    public void stop() {
        scheduledScanServices.forEach(ScheduledScanService::stop);
    }
}
