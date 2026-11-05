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
package org.realtimeetl.common.dispatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2025-06-22 23:34
 */
public abstract class AbstractDispatchTask implements DispatchTask, Cloneable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Consumer<DispatchTask> consumer;

    private volatile boolean running;

    public abstract void execute() throws Exception;

    @Override
    public void run() {
        try {
            running = true;
            execute();
        } catch (Exception e) {
            logger.error("dispatch task，uniqueId:{}", getUniqueId(), e);
        } finally {
            running = false;
            if (consumer != null) {
                consumer.accept(this);
            }
        }
    }

    @Override
    public void destroy() {
        this.running = false;
    }

    @Override
    public void onDestroy(Consumer<DispatchTask> consumer) {
        this.consumer = consumer;
    }

    protected boolean isRunning() {
        return running;
    }

    @Override
    public AbstractDispatchTask clone() {
        try {
            return (AbstractDispatchTask) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
