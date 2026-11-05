package org.realtimeetl.manager;

import org.realtimeetl.parser.model.Mapping;

import java.util.List;

public interface Puller {

    void start(Mapping mapping);

    /**
     * StartSync task
     *
     * @param mapping      Sync task
     * @param autoRecovery YesNoRestart Autorestore for Servicetrue Right on time.CDC ListenStart FailedPressConfigureTry again）
     */
    void start(Mapping mapping, boolean autoRecovery);

    void close(String taskId);

    /**
     * Get AllRunningYes.Tasksid
     */
    List<String> getTaskId();
}
