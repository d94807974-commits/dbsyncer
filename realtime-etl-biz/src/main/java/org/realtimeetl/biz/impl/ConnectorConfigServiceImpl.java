/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.biz.impl;

import org.realtimeetl.biz.BizException;
import org.realtimeetl.biz.ConnectorConfigService;
import org.realtimeetl.biz.GroupSearchFilter;
import org.realtimeetl.biz.GroupService;
import org.realtimeetl.biz.ResourceAclService;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.checker.impl.connector.ConnectorChecker;
import org.realtimeetl.biz.enums.ResourceAclActionEnum;
import org.realtimeetl.common.enums.YesNoEnum;
import org.realtimeetl.common.model.Paging;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.parser.ConnectorProfile;
import org.realtimeetl.parser.DatabaseSyncProfile;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.ValidateSyncProfile;
import org.realtimeetl.parser.model.Connector;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.database.AbstractDatabaseConnector;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.model.ConnectorConfig;
import org.realtimeetl.sdk.model.DatabaseMapping;
import org.realtimeetl.sdk.model.DatabaseSyncTask;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author AE86
 * @version 1.0.0
 * @date 2019/10/17 23:20
 */
@Service
public class ConnectorConfigServiceImpl extends BaseServiceImpl implements ConnectorConfigService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, Boolean> health = new ConcurrentHashMap<>();

    @Resource
    private ConnectorProfile connectorProfile;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private ValidateSyncProfile validateSyncProfile;

    @Resource
    private DatabaseSyncProfile databaseSyncProfile;

    @Resource
    private ConnectorFactory connectorFactory;

    @Resource
    private LogService logService;

    @Resource
    private ConnectorChecker connectorChecker;

    @Resource
    private GroupService groupService;

    @Resource
    private ResourceAclService resourceAclService;

    @Override
    public String add(Map<String, String> params) {
        Connector connector = connectorChecker.checkAddConfigModel(params);
        String id = connectorProfile.addConnector(connector);
        resourceAclService.grantOwner(params.get(UserConfigService.CURRENT_USER_NAME),
                ConfigConstant.CONNECTOR, id);
        return id;
    }

    @Override
    public String copy(String id, String username) {
        Connector connector = connectorProfile.getConnector(id);
        Assert.notNull(connector, "The connector id is invalid.");

        ConnectorConfig config = connector.getConfig();
        Map params = JsonUtil.parseMap(config);
        params.put("properties", config.getPropertiesText());
        params.put("extInfo", JsonUtil.objToJson(config.getExtInfo()));
        // PingextInfo，For each individualConfigValidator By FormFieldReadpluginName）
        if (config.getExtInfo() != null) {
            for (String key : config.getExtInfo().stringPropertyNames()) {
                String value = config.getExtInfo().getProperty(key);
                if (value != null && !params.containsKey(key)) {
                    params.put(key, value);
                }
            }
        }
        params.put(ConfigConstant.CONFIG_MODEL_NAME, connector.getName() + "(Copy)");
        // andForm SOLAS: YesValueOrganisationOn；CopySource Switches, AvoidcheckAdd The absence of reference.Failed
        if (connector.isSource()) {
            params.put(ConfigConstant.CONNECTOR_IS_SOURCE, "1");
        }
        if (connector.isTarget()) {
            params.put(ConfigConstant.CONNECTOR_IS_TARGET, "1");
        }
        Connector model = connectorChecker.checkAddConfigModel(params);

        String newId = connectorProfile.addConnector(model);
        //AuthorizationResource
        resourceAclService.grantOwner(username, ConfigConstant.CONNECTOR, newId);
        return newId;
    }

    @Override
    public String edit(Map<String, String> params) {
        Connector model = connectorChecker.checkEditConfigModel(params);
        return connectorProfile.updateConnector(model);
    }

    @Override
    public String remove(String id) {
        assertConnectorNotInUse(id);

        Connector connector = connectorProfile.getConnector(id);
        if (connector != null) {
            connectorFactory.disconnect(connector.getId());
            connectorProfile.removeConnector(id);
            groupService.bind(null, ConfigConstant.CONNECTOR, id);
            resourceAclService.revokeAllForResource(ConfigConstant.CONNECTOR, id);
        }
        return "DeleteConnectorSuccess!";
    }

    /**
     * DeletePrevaluation:Sync task、Data validation、Library Migration TaskI can't still quote it.Connector。
     */
    private void assertConnectorNotInUse(String id) {
        mappingProfile.pageScanTasks(ConfigConstant.PAGE_SIZE, mappingAll -> {
            if (CollectionUtils.isEmpty(mappingAll)) {
                return;
            }
            for (Mapping mapping : mappingAll) {
                if (mapping == null) {
                    continue;
                }
                if (StringUtil.equals(mapping.getSourceConnectorId(), id) || StringUtil.equals(mapping.getTargetConnectorId(), id)) {
                    String error = String.format("Driver“%s”In use，Please.FirstDelete", mapping.getName());
                    logger.error(error);
                    throw new BizException(error);
                }
            }
        });
        validateSyncProfile.pageScanTasks(ConfigConstant.PAGE_SIZE, tasks -> {
            if (CollectionUtils.isEmpty(tasks)) {
                return;
            }
            for (ValidateSyncTask task : tasks) {
                if (task == null) {
                    continue;
                }
                if (StringUtil.equals(task.getSourceConnectorId(), id) || StringUtil.equals(task.getTargetConnectorId(), id)) {
                    String error = String.format("Revised verification tasks“%s”In use，Please.FirstDelete", task.getName());
                    logger.error(error);
                    throw new BizException(error);
                }
            }
        });
        databaseSyncProfile.pageScanTasks(ConfigConstant.PAGE_SIZE, tasks -> {
            if (CollectionUtils.isEmpty(tasks)) {
                return;
            }
            for (DatabaseSyncTask task : tasks) {
                if (task == null || CollectionUtils.isEmpty(task.getDatabaseMappings())) {
                    continue;
                }
                for (DatabaseMapping mapping : task.getDatabaseMappings()) {
                    if (mapping == null) {
                        continue;
                    }
                    if (StringUtil.equals(mapping.getSourceConnectorId(), id) || StringUtil.equals(mapping.getTargetConnectorId(), id)) {
                        String error = String.format("Library Migration Task“%s”In use，Please.FirstDelete", task.getName());
                        logger.error(error);
                        throw new BizException(error);
                    }
                }
            }
        });
    }

    @Override
    public Connector getConnector(String id) {
        return connectorProfile.getConnector(id);
    }

    @Override
    public List<String> getDatabase(String id) {
        Connector connector = connectorProfile.getConnector(id);
        return connector != null ? connector.getDatabases() : Collections.emptyList();
    }

    @Override
    public List<String> getSchema(String id, String database) {
        Connector connector = connectorProfile.getConnector(id);
        if (connector == null) {
            return Collections.emptyList();
        }
        ConnectorConfig config = connector.getConfig();
        ConnectorService connectorService = connectorFactory.getConnectorService(config.getConnectorType());
        String catalog = StringUtil.getIfBlank(database, StringUtil.EMPTY);
        try {
            ConnectorInstance connectorInstance = connectorFactory.connect(connector.getId(), config, catalog, StringUtil.EMPTY);
            return connectorService.getSchemas(connectorInstance, database);
        } finally {
            releaseIdleConnector(id);
        }
    }

    @Override
    public List<Connector> getConnectorAll() {
        return connectorProfile.getConnectorAll().stream().sorted(Comparator.comparing(Connector::getUpdateTime).reversed()).collect(Collectors.toList());
    }

    @Override
    public Paging<Connector> search(Map<String, String> params) {
        int pageNum = NumberUtil.toInt(params.get("pageNum"), 1);
        int pageSize = NumberUtil.toInt(params.get("pageSize"), 10);
        int relationOnly = NumberUtil.toInt(params.get("relationOnly"), 0);
        String searchKey = params.get("searchKey");
        // FilterSource databaseorTarget databaseType
        String role = params.get("role");
        GroupSearchFilter groupFilter = groupService.resolveSearchFilter(params.get(ConfigConstant.GROUP_REL_GROUP_ID), ConfigConstant.CONNECTOR);
        Set<String> allowedIds = resourceAclService.resolveAllowedIds(params.get(UserConfigService.CURRENT_USER_NAME), ConfigConstant.CONNECTOR, ResourceAclActionEnum.READ.getCode());
        groupFilter = resourceAclService.applyToSearchFilter(groupFilter, allowedIds);
        if (groupFilter.isEmptyResult()) {
            return new Paging<>(pageNum, pageSize);
        }
        Paging<Connector> paging = connectorProfile.queryConnectors(pageNum, pageSize, searchKey, role, groupFilter.getIds());
        if (paging == null) {
            return null;
        }
        // Database migrationscene, only relationship data are supported for nowLibrary
        if (YesNoEnum.YES.getCode() == relationOnly && !CollectionUtils.isEmpty(paging.getData())) {
            paging.setData(paging.getData().stream().filter(this::isRelationalDatabaseConnector).collect(Collectors.toList()));
        }
        return paging;
    }

    @Override
    public List<String> getConnectorTypeAll() {
        ArrayList<String> connectorTypes = new ArrayList<>(connectorFactory.getConnectorTypeAll());
        connectorTypes.sort(Comparator.comparing(String::toString));
        return connectorTypes;
    }

    @Override
    public void refreshHealth() {
        List<Connector> list = connectorProfile.getConnectorAll();
        if (CollectionUtils.isEmpty(list)) {
            if (!CollectionUtils.isEmpty(health)) {
                health.clear();
            }
            return;
        }
        // Only detect caches and avoid health checks under clustersFullCompany
        Set<String> exist = new HashSet<>();
        for (Connector connector : list) {
            exist.add(connector.getId());
            if (connectorFactory.getPool().get(connector.getId()) == null) {
                connectorFactory.connect(connector.getId(), connector.getConfig(), StringUtil.EMPTY, StringUtil.EMPTY);
            }
            boolean alive = probeAlive(connector.getId(), connector.getConfig());
            health.put(connector.getId(), alive);
            persistStatusIfChanged(connector, alive);
        }
        // RemoveDeletedConnectorCache
        health.keySet().removeIf(id -> !exist.contains(id));
    }

    /**
     * HealthStatusWrite Back on ChangeConnector {@code STATUS}（HamJSON），If you do not change, skip, avoid writing each timeLibrary。
     */
    private void persistStatusIfChanged(Connector connector, boolean alive) {
        YesNoEnum newStatus = YesNoEnum.of(alive);
        if (connector.getStatus() == newStatus.getCode()) {
            return;
        }
        connector.setStatus(newStatus.getCode());
        connector.setUpdateTime(System.currentTimeMillis());
        try {
            connectorProfile.updateConnector(connector);
        } catch (Exception e) {
            logger.warn("UpdateConnectorStatusFailed, connectorId={}, status={}, err={}", connector.getId(), newStatus, e.getMessage());
        }
    }

    @Override
    public boolean isAlive(String id) {
        Connector connector = connectorProfile.getConnector(id);
        if (connector == null || connector.getConfig() == null) {
            return false;
        }
        boolean created = false;
        try {
            if (connectorFactory.getPool().get(id) == null) {
                connectorFactory.connect(id, connector.getConfig(), StringUtil.EMPTY, StringUtil.EMPTY);
                created = true;
            }
            boolean alive = probeAlive(id, connector.getConfig());
            health.put(id, alive);
            persistStatusIfChanged(connector, alive);
            return alive;
        } catch (Exception e) {
            LogType.ConnectorLog logType = LogType.ConnectorLog.FAILED;
            logService.log(logType, "%s%s", logType.getName(), e.getMessage());
            return false;
        } finally {
            if (created) {
                releaseIdleConnector(id);
            }
        }
    }

    @Override
    public Object getPosition(String mappingId) {
        Mapping mapping = mappingProfile.get(mappingId);
        Assert.notNull(mapping, "Mapping can not be null.");
        String instanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), mapping.getSourceConnectorId(), ConnectorInstanceUtil.SOURCE_SUFFIX);
        ConnectorInstance connectorInstance;
        Connector connector = connectorProfile.getConnector(mapping.getSourceConnectorId());
        Assert.notNull(connector, "Source connector does not exist");
        connectorInstance = connectorFactory.connect(instanceId, connector.getConfig(), mapping.getSourceDatabase(), mapping.getSourceSchema());
        return connectorFactory.getPosition(connectorInstance);
    }

    private boolean probeAlive(String connectorConfigId, ConnectorConfig config) {
        try {
            return connectorFactory.isAlive(connectorConfigId, config);
        } catch (Exception e) {
            LogType.ConnectorLog logType = LogType.ConnectorLog.FAILED;
            logService.log(logType, "%s%s", logType.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Under clusterNoneRunTasksTake that.Connector，Dis ExchangeConfigureClass Cache.
     *
     * @param connectorId Connector ID
     */
    private void releaseIdleConnector(String connectorId) {
        connectorFactory.disconnect(connectorId);
    }

    /**
     * Keep relationship data onlyLibraryConnector。
     */
    private boolean isRelationalDatabaseConnector(Connector connector) {
        if (connector == null || connector.getConfig() == null || StringUtil.isBlank(connector.getConfig().getConnectorType())) {
            return false;
        }
        try {
            ConnectorService connectorService = connectorFactory.getConnectorService(connector.getConfig().getConnectorType());
            return connectorService instanceof AbstractDatabaseConnector;
        } catch (Exception e) {
            logger.warn("Filter Relationship Connector Failed, connectorId={}, type={}", connector.getId(), connector.getConfig().getConnectorType(), e);
            return false;
        }
    }

}