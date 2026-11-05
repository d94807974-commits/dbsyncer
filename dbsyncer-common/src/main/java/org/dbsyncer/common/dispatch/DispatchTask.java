/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.common.dispatch;

import org.dbsyncer.common.enums.DispatchTaskEnum;

import java.util.function.Consumer;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2025-06-12 23:54
 */
public interface DispatchTask extends Runnable {

    /**
     * UniqueTasksid
     */
    String getUniqueId();

    DispatchTaskEnum getType();

    void destroy();

    void onDestroy(Consumer<DispatchTask> consumer);
}
