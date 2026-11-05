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
import org.realtimeetl.common.event.RemoveConnectorCacheEvent;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.common.util.TaskSplitUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.parser.AbstractConfigModelProfile;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.enums.CommandEnum;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.enums.SortEnum;
import org.realtimeetl.sdk.enums.StorageEnum;
import org.realtimeetl.sdk.filter.Query;
import org.realtimeetl.sdk.filter.impl.InFilter;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.realtimeetl.sdk.storage.StorageService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link ConnectorProfile} Achieved.
 *
 * @author Realtime ETL
 * @version 1.0.0
 */
@Component
public class ConnectorProfileImpl extends AbstractConfigModelProfile<Connector> implements ConnectorProfile, ApplicationListener<RemoveConnectorCacheEvent> {

    @Resource
    private OperationTemplate operationTemplate;

    @Resource
    private StorageService storageService;

    @Resource
    private ConnectorFactory connectorFactory;

    @Override
    public Connector parseConnector(String json) {
        Map conn = JsonUtil.parseMap(json);
        Map config = (Map) conn.remove("config");
        Connector connector = JsonUtil.jsonToObj(conn.toString(), Connector.class);
        Assert.notNull(connector, "Connector can not be null.");
        if (config != null) {
            String connectorType = (String) config.get("connectorType");
            ConnectorService connectorService = connectorFactory.getConnectorService(connectorType);
            Class<ConnectorConfig> configClass = connectorService.getConfigClass();
            connector.setConfig(JsonUtil.jsonToObj(config.toString(), configClass));
        }
        return connector;
    }

    @Override
    public Connector getCache(String id) {
        String cacheKey = buildCacheKey(id);
        Connector cached = cacheService.get(cacheKey, Connector.class);
        if (cached != null) {
            return cached;
        }
        return cacheService.executeWithLock(buildLockKey(id), () -> {
            Connector again = cacheService.get(cacheKey, Connector.class);
            if (again != null) {
                return again;
            }
            Query query = new Query();
            query.setType(StorageEnum.CONNECTOR);
            query.addFilter(ConfigConstant.CONFIG_MODEL_ID, id);
            Map map = storageService.queryObject(query);
            if (CollectionUtils.isEmpty(map)) {
                return null;
            }
            Object json = map.get(ConfigConstant.CONFIG_MODEL_JSON);
            Connector connector = json == null ? null : parseConnector(String.valueOf(json));
            if (connector != null) {
                cacheService.put(cacheKey, connector, EXPIRED_1_HOURS);
            }
            return connector;
        });

    }

    @Override
    public Connector getConnector(String connectorId) {
        return getCache(connectorId);
    }

    @Override
    public List<Connector> getConnectorAll() {
        List<Connector> result = new ArrayList<>();
        Query query = new Query();
        query.setType(StorageEnum.CONNECTOR);
        query.setPageSize(ConfigConstant.PAGE_SIZE);
        while (true) {
            Paging paging = storageService.query(query);
            if (paging == null || CollectionUtils.isEmpty(paging.getData())) {
                break;
            }
            List<Map> data = (List<Map>) paging.getData();
            for (Map row : data) {
                Object json = row.get(ConfigConstant.CONFIG_MODEL_JSON);
                if (json != null) {
                    Connector connector = parseConnector(String.valueOf(json));
                    if (connector != null) {
                        result.add(connector);
                    }
                }
            }
            query.setPageNum(query.getPageNum() + 1);
        }
        return result;
    }

    @Override
    public Paging<Connector> queryConnectors(int pageNum, int pageSize, String searchKey, String role, List<String> ids) {
        int safePageNum = pageNum > 0 ? pageNum : 1;
        int safePageSize = pageSize > 0 ? pageSize : ConfigConstant.PAGE_SIZE;
        Query query = new Query(safePageNum, safePageSize);
        query.setType(StorageEnum.CONNECTOR);
        if (StringUtil.isNotBlank(searchKey)) {
            query.addFilter(ConfigConstant.CONFIG_MODEL_NAME, searchKey, false);
        }
        if (StringUtil.equalsIgnoreCase("source", role)) {
            query.addFilter(ConfigConstant.CONNECTOR_IS_SOURCE, 1);
        } else if (StringUtil.equalsIgnoreCase("target", role)) {
            query.addFilter(ConfigConstant.CONNECTOR_IS_TARGET, 1);
        }
        if (!CollectionUtils.isEmpty(ids)) {
            // InFilter Yes.and IN The one-to-one binding.Value，I can't afford another one.singletonList
            query.getBooleanFilter().add(new InFilter(ConfigConstant.CONFIG_MODEL_ID, new ArrayList<>(ids)));
        }
        query.addOrderBy(ConfigConstant.CONFIG_MODEL_UPDATE_TIME, SortEnum.DESC);
        Paging paging = storageService.query(query);
        Paging<Connector> result = new Paging<>(safePageNum, safePageSize);
        if (paging == null) {
            return result;
        }
        result.setTotal(paging.getTotal());
        if (CollectionUtils.isEmpty(paging.getData())) {
            return result;
        }
        List<Connector> rows = new ArrayList<>(paging.getData().size());
        for (Object item : paging.getData()) {
            if (!(item instanceof Map)) {
                continue;
            }
            Object json = ((Map) item).get(ConfigConstant.CONFIG_MODEL_JSON);
            if (json == null) {
                continue;
            }
            Connector connector = parseConnector(String.valueOf(json));
            if (connector != null) {
                rows.add(connector);
            }
        }
        result.setData(rows);
        return result;
    }

    @Override
    public String addConnector(Connector connector) {
        return operationTemplate.execute(connector, CommandEnum.OPR_ADD);
    }

    @Override
    public void addConnectorBatch(List<Connector> connectors) {
        if (CollectionUtils.isEmpty(connectors)) {
            return;
        }
        TaskSplitUtil.split(connectors, ConfigConstant.PAGE_SIZE, batch ->
                operationTemplate.executeBatch(batch, CommandEnum.OPR_ADD));
    }

    @Override
    public String updateConnector(Connector connector) {
        return operationTemplate.execute(connector, CommandEnum.OPR_EDIT);
    }

    @Override
    public void removeConnector(String id) {
        storageService.remove(StorageEnum.CONNECTOR, id);
    }

    @Override
    public int countConnectors() {
        return operationTemplate.count(StorageEnum.CONNECTOR, null);
    }

    @Override
    public void importConnectorsFromJson(String json) {
        if (StringUtil.isBlank(json)) {
            return;
        }
        List list = JsonUtil.parseList(json);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        List<Connector> connectors = new ArrayList<>(list.size());
        for (Object item : list) {
            Connector connector = parseConnector(JsonUtil.objToJson(item));
            if (connector != null) {
                connectors.add(connector);
            }
        }
        TaskSplitUtil.split(connectors, PackageFormatConfig.IMPORT_BATCH_SIZE, this::addConnectorBatch);
    }

    @Override
    public void onApplicationEvent(RemoveConnectorCacheEvent event) {
        removeCache(event.getCommonMessage().getId());
    }
}
