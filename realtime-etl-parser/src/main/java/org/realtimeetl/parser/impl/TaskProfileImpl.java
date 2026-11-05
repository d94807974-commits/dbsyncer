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
package org.realtimeetl.parser.impl;

import org.realtimeetl.common.config.PackageFormatConfig;
import org.realtimeetl.common.enums.CommonTaskTypeEnum;
import org.realtimeetl.common.model.ConfigModel;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.PackageZipUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.common.util.TaskSplitUtil;
import org.realtimeetl.parser.ParserException;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskDetailMetaProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.TaskProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.model.TaskImportResult;
import org.realtimeetl.parser.util.ConfigModelUtil;
import org.realtimeetl.parser.util.FullTableProgressUtil;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.SortEnum;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.filter.impl.InFilter;
import org.realtimeetl.sdk.model.DatabaseSyncTask;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.sdk.storage.StorageService;
import org.realtimeetl.storage.impl.SnowflakeIdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipFile;

/**
 * @author Realtime ETL
 * @version 1.0.0
 */
@Component
public class TaskProfileImpl implements TaskProfile {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    private TaskDetailMetaProfile taskDetailMetaProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private StorageService storageService;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Override
    public <T extends ConfigModel> T getTask(String id, Class<T> clazz) {
        return operationTemplate.queryObject(clazz, id);
    }

    @Override
    public <T extends ConfigModel> Paging<T> queryTasks(Class<T> clazz, int pageNum, int pageSize, String searchKey, List<String> ids) {
        Assert.notNull(clazz, "Task class can not be null.");
        int safePageNum = pageNum > 0 ? pageNum : 1;
        int safePageSize = pageSize > 0 ? pageSize : ConfigConstant.PAGE_SIZE;
        try {
            ConfigModel probe = clazz.newInstance();
            Query query = new Query(safePageNum, safePageSize);
            query.setType(StorageEnum.TASK);
            if (StringUtil.isNotBlank(probe.getType())) {
                query.addFilter(ConfigConstant.CONFIG_MODEL_TYPE, probe.getType());
            }
            if (StringUtil.isNotBlank(searchKey)) {
                query.addFilter(ConfigConstant.CONFIG_MODEL_NAME, searchKey, false);
            }
            if (!CollectionUtils.isEmpty(ids)) {
                query.getBooleanFilter().add(new InFilter(ConfigConstant.CONFIG_MODEL_ID, new ArrayList<>(ids)));
            }
            query.addOrderBy(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, SortEnum.DESC);
            Paging paging = storageService.query(query);
            Paging<T> result = new Paging<>(safePageNum, safePageSize);
            if (paging == null) {
                return result;
            }
            result.setTotal(paging.getTotal());
            if (CollectionUtils.isEmpty(paging.getData())) {
                return result;
            }
            List<T> tasks = new ArrayList<>(paging.getData().size());
            for (Object item : paging.getData()) {
                T task = ConfigModelUtil.parseFromRow((Map) item, clazz);
                if (task != null) {
                    tasks.add(task);
                }
            }
            result.setData(tasks);
            return result;
        } catch (Exception e) {
            throw new ParserException(e);
        }
    }

    @Override
    public <T extends ConfigModel> void pageScanTasks(Class<T> clazz, int pageSize, Consumer<List<T>> pageConsumer) {
        if (clazz == null || pageConsumer == null) {
            return;
        }
        int safePageSize = pageSize > 0 ? pageSize : ConfigConstant.PAGE_SIZE;
        int pageNum = 1;
        while (true) {
            Paging<T> paging = queryTasks(clazz, pageNum, safePageSize, null, null);
            if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
                break;
            }
            List<T> page = new ArrayList<>(paging.getData());
            pageConsumer.accept(page);
            if (page.size() < safePageSize) {
                break;
            }
            pageNum++;
        }
    }

    @Override
    public String addTask(ConfigModel task) {
        Assert.notNull(task, "Task can not be null.");
        if (StringUtil.isBlank(task.getId())) {
            task.setId(String.valueOf(snowflakeIdWorker.nextId()));
        }
        storageService.add(StorageEnum.TASK, ConfigModelUtil.convertModelToMap(task));
        return task.getId();
    }

    @Override
    public String updateTask(ConfigModel task) {
        Assert.notNull(task, "Task can not be null.");
        Assert.hasText(task.getId(), "Task id can not be empty.");
        storageService.edit(StorageEnum.TASK, ConfigModelUtil.convertModelToMap(task));
        return task.getId();
    }

    @Override
    public void addTaskBatch(List<? extends ConfigModel> tasks) {
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        List<Map> paramsList = new ArrayList<>(tasks.size());
        for (ConfigModel task : tasks) {
            Assert.notNull(task, "Task can not be null.");
            if (StringUtil.isBlank(task.getId())) {
                task.setId(String.valueOf(snowflakeIdWorker.nextId()));
            }
            paramsList.add(ConfigModelUtil.convertModelToMap(task));
        }
        storageService.addBatch(StorageEnum.TASK, null, paramsList);
        tasks.stream().map(ConfigModel::getId).collect(Collectors.toList());
    }

    @Override
    public void deleteTask(String id) {
        if (StringUtil.isBlank(id)) {
            return;
        }
        storageService.remove(StorageEnum.TASK, id);
    }

    @Override
    public int countTasks(String type) {
        Query condition = new Query();
        if (StringUtil.isNotBlank(type)) {
            condition.addFilter(ConfigConstant.CONFIG_MODEL_TYPE, type);
        }
        return operationTemplate.count(StorageEnum.TASK, condition);
    }

    @Override
    public int countAllTasks() {
        return countTasks(null);
    }

    @Override
    public List<Map<String, Object>> listAllTaskJsonMaps() {
        List<Map<String, Object>> result = new ArrayList<>();
        Query query = new Query();
        query.setType(StorageEnum.TASK);
        query.setPageSize(ConfigConstant.PAGE_SIZE);
        while (true) {
            Paging paging = storageService.query(query);
            if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
                break;
            }
            List<Map> data = (List<Map>) paging.getData();
            for (Map row : data) {
                Object json = row.get(ConfigConstant.CONFIG_MODEL_JSON);
                if (json == null) {
                    continue;
                }
                Map<String, Object> task = JsonUtil.parseMap(String.valueOf(json));
                if (task != null) {
                    result.add(task);
                }
            }
            query.setPageNum(query.getPageNum() + 1);
        }
        return result;
    }

    @Override
    public String importTask(ConfigModel task) {
        Assert.notNull(task, "Task can not be null.");
        Assert.hasText(task.getId(), "Import task id can not be empty.");
        storageService.add(StorageEnum.TASK, ConfigModelUtil.convertModelToMap(task));
        return task.getId();
    }

    @Override
    public TaskImportResult importTasksFromJson(String json) {
        if (StringUtil.isBlank(json)) {
            return new TaskImportResult(0, Collections.emptyList());
        }
        List list = JsonUtil.parseList(json);
        if (CollectionUtils.isEmpty(list)) {
            return new TaskImportResult(0, Collections.emptyList());
        }
        List<Mapping> mappings = new ArrayList<>();
        List<ConfigModel> enterpriseTasks = new ArrayList<>();
        for (Object item : list) {
            Map map = item instanceof Map ? (Map) item : JsonUtil.parseMap(JsonUtil.objToJson(item));
            if (map == null) {
                continue;
            }
            String type = map.get(ConfigConstant.CONFIG_MODEL_TYPE) == null
                    ? null : String.valueOf(map.get(ConfigConstant.CONFIG_MODEL_TYPE));
            // ImportHistory CompatibilityTYPE，Regulation before resolution/downLibrary
            type = CommonTaskTypeEnum.normalizeImportType(type);
            if (StringUtil.isNotBlank(type)) {
                map.put(ConfigConstant.CONFIG_MODEL_TYPE, type);
            }
            String itemJson = JsonUtil.objToJson(map);
            if (StringUtil.equals(ConfigConstant.MAPPING, type) || StringUtil.isBlank(type)) {
                Mapping mapping = JsonUtil.jsonToObj(itemJson, Mapping.class);
                if (mapping != null) {
                    if (StringUtil.isBlank(mapping.getType())) {
                        mapping.setType(ConfigConstant.MAPPING);
                    }
                    mappings.add(mapping);
                }
                continue;
            }
            CommonTaskTypeEnum taskType = CommonTaskTypeEnum.parse(type);
            if (taskType == CommonTaskTypeEnum.VALIDATE_SYNC) {
                ValidateSyncTask task = JsonUtil.jsonToObj(itemJson, ValidateSyncTask.class);
                if (task != null) {
                    task.setType(taskType.getCode());
                    importTask(task);
                    enterpriseTasks.add(task);
                }
            } else if (taskType == CommonTaskTypeEnum.DATABASE_SYNC) {
                DatabaseSyncTask task = JsonUtil.jsonToObj(itemJson, DatabaseSyncTask.class);
                if (task != null) {
                    task.setType(taskType.getCode());
                    importTask(task);
                    enterpriseTasks.add(task);
                }
            } else {
                logger.warn("Skip unknown task type: type={, id={", type, map.get(ConfigConstant.CONFIG_MODEL_ID));
            }
        }
        if (!CollectionUtils.isEmpty(mappings)) {
            TaskSplitUtil.split(mappings, PackageFormatConfig.IMPORT_BATCH_SIZE, this::addTaskBatch);
        }
        return new TaskImportResult(mappings.size(), enterpriseTasks);
    }

    @Override
    public void importTaskDetailSchemasFromJson(String json) {
        if (StringUtil.isBlank(json)) {
            return;
        }
        Map map = JsonUtil.parseMap(json);
        if (map == null) {
            return;
        }
        Object taskIdsObj = map.get("taskIds");
        if (!(taskIdsObj instanceof List)) {
            return;
        }
        List<String> taskIds = new ArrayList<>();
        for (Object item : (List) taskIdsObj) {
            if (item == null) {
                continue;
            }
            String taskId = String.valueOf(item);
            if (StringUtil.isNotBlank(taskId)) {
                taskIds.add(taskId);
            }
        }
        createRunDetailTables(taskIds);
    }

    @Override
    public TaskImportResult importTasksFromZip(ZipFile zip) throws IOException {
        if (zip == null) {
            return new TaskImportResult(0, Collections.emptyList());
        }
        String json = PackageZipUtil.readOptionalEntry(zip, PackageFormatConfig.TASK);
        if (StringUtil.isBlank(json)) {
            json = PackageZipUtil.readOptionalEntry(zip, PackageFormatConfig.MAPPING);
        }
        return importTasksFromJson(json);
    }

    @Override
    public String exportTaskDetailSchemasJson(List<String> taskIds) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskIds", taskIds == null ? Collections.emptyList() : taskIds);
        return JsonUtil.objToJson(payload);
    }

    @Override
    public void deleteRunData(String taskId) {
        if (StringUtil.isBlank(taskId)) {
            return;
        }
        // ClearDetailsminTable dataTasksStill in place; scheduleMeta ResetNotDelete，ForminSelectedJOIN
        taskDetailMetaProfile.clearData(taskId);
        resetTableDetailMetas(taskId);
    }

    @Override
    public void resetTableDetailMetas(String taskId) {
        if (StringUtil.isBlank(taskId)) {
            return;
        }
        tableGroupProfile.pageScanTableGroups(taskId, ConfigConstant.PAGE_SIZE, tableGroups -> {
            if (CollectionUtils.isEmpty(tableGroups)) {
                return;
            }
            List<String> groupIds = tableGroups.stream().map(TableGroup::getId).collect(Collectors.toList());
            FullTableProgressUtil.resetAll(taskMetaProfile, groupIds);
        });
    }

    @Override
    public void dropTaskDetailTable(String taskId) {
        if (StringUtil.isBlank(taskId)) {
            return;
        }
        // PhysicsDROP DetailsminTable, no reconstruction
        taskDetailMetaProfile.dropTaskDetailTable(taskId);
        deleteTableGroupDetailMetas(taskId);
    }

    private void deleteTableGroupDetailMetas(String taskId) {
        tableGroupProfile.pageScanTableGroups(taskId, ConfigConstant.PAGE_SIZE, tableGroups -> {
            List<String> groupIds = tableGroups.stream().map(TableGroup::getId).collect(Collectors.toList());
            taskDetailMetaProfile.deleteMetaByTableGroupIds(groupIds);
        });
    }

    @Override
    public void createRunDetailTables(List<String> taskIds) {
        if (CollectionUtils.isEmpty(taskIds)) {
            return;
        }
        for (String taskId : taskIds) {
            taskDetailMetaProfile.clearData(taskId);
        }
    }
}
