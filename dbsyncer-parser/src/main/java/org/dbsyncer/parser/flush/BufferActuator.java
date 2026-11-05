package org.dbsyncer.parser.flush;

import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2022/3/27 17:34
 */
public interface BufferActuator {

    /**
     * SubmitTasks
     *
     * @param request
     */
    void offer(BufferRequest request);

    /**
     * Fetch 2.1
     *
     * @return
     */
    Queue getQueue();

    /**
     * Subprogramme 3
     *
     * @return
     */
    int getQueueCapacity();

    /**
     * AccessThreadsJi.
     *
     * @return
     */
    Executor getExecutor();
}
