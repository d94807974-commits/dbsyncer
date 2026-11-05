/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.realtimeetl.common.dispatch;

/**
 * @Author The clouds.
 * @Version 1.0.0
 * @Date 2025-06-12 23:45
 */
public interface DispatchTaskService {

    void execute(DispatchTask task);

    void stop(String uniqueId);

    boolean isRunning(String uniqueId);
}
