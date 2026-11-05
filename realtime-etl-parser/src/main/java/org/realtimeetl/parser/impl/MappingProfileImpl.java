/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.parser.impl;

import org.realtimeetl.common.event.RemoveMappingCacheEvent;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.parser.AbstractConfigModelProfile;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.TaskProfile;
import org.realtimeetl.parser.model.Mapping;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-09-29 19:52
 */
@Component
public final class MappingProfileImpl extends AbstractConfigModelProfile<Mapping> implements MappingProfile, ApplicationListener<RemoveMappingCacheEvent> {

    @Resource
    private TaskProfile taskProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Override
    public Mapping get(String id) {
        return getCache(id);
    }

    @Override
    public String add(Mapping mapping) {
        return taskProfile.addTask(mapping);
    }

    @Override
    public void addBatch(List<Mapping> mappings) {
        taskProfile.addTaskBatch(mappings);
    }

    @Override
    public String update(Mapping mapping) {
        String id = taskProfile.updateTask(mapping);
        removeCacheAndNotice(mapping.getId());
        return id;
    }

    @Override
    public void delete(String id) {
        taskProfile.deleteTask(id);
        removeCacheAndNotice(id);
    }

    @Override
    public void clearRunData(String id) {
        taskProfile.deleteRunData(id);
    }

    @Override
    public void dropTaskDetailTable(String id) {
        taskProfile.dropTaskDetailTable(id);
    }

    @Override
    public Paging<Mapping> query(int pageNum, int pageSize, String searchKey, List<String> ids) {
        return taskProfile.queryTasks(Mapping.class, pageNum, pageSize, searchKey, ids);
    }

    @Override
    public void pageScanTasks(int pageSize, Consumer<List<Mapping>> pageConsumer) {
        taskProfile.pageScanTasks(Mapping.class, pageSize, pageConsumer);
    }

    @Override
    public void onApplicationEvent(RemoveMappingCacheEvent event) {
        removeCache(event.getCommonMessage().getId());
        taskMetaProfile.removeMetaCache(event.getCommonMessage().getId());
    }
}
