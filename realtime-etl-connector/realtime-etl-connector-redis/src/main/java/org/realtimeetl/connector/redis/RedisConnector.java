/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.connector.redis;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.redis.cdc.RedisListener;
import org.realtimeetl.connector.redis.config.RedisConfig;
import org.realtimeetl.connector.redis.constant.RedisConstant;
import org.realtimeetl.connector.redis.schema.RedisSchemaResolver;
import org.realtimeetl.connector.redis.util.RedisUtil;
import org.realtimeetl.connector.redis.validator.RedisConfigValidator;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.connector.AbstractConnector;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.plugin.MetaContext;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RedisConnectorAchieved
 *
 * @author The clouds.
 * @version 1.0.0
 * @date 2026-05-29 23:20
 */
public class RedisConnector extends AbstractConnector implements ConnectorService<RedisConnectorInstance, RedisConfig> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final RedisConfigValidator configValidator = new RedisConfigValidator();
    private final RedisSchemaResolver schemaResolver = new RedisSchemaResolver();

    @Override
    public String getConnectorType() {
        return "Redis";
    }

    @Override
    public TableTypeEnum getExtendedTableType() {
        return TableTypeEnum.SEMI;
    }

    @Override
    public Class<RedisConfig> getConfigClass() {
        return RedisConfig.class;
    }

    @Override
    public ConnectorInstance connect(RedisConfig config, ConnectorServiceContext context) {
        return new RedisConnectorInstance(config);
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public void disconnect(RedisConnectorInstance connectorInstance) {
        connectorInstance.close();
    }

    @Override
    public boolean isAlive(RedisConnectorInstance connectorInstance) {
        try {
            connectorInstance.ping();
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new RedisException(e);
        }
    }

    @Override
    public List<String> getDatabases(RedisConnectorInstance connectorInstance) {
        List<String> list = new ArrayList<>();
        list.add("db" + connectorInstance.getConfig().getDatabase());
        return list;
    }

    @Override
    public List<Table> getTable(RedisConnectorInstance connectorInstance, ConnectorServiceContext context) {
        return new ArrayList<>();
    }

    @Override
    public List<MetaInfo> getMetaInfo(RedisConnectorInstance connectorInstance, ConnectorServiceContext context) {
        List<MetaInfo> metaInfos = new ArrayList<>();
        for (Table table : context.getTablePatterns()) {
            MetaInfo metaInfo = new MetaInfo();
            metaInfo.setTable(table.getName());
            metaInfo.setTableType(getExtendedTableType().getCode());
            metaInfo.setColumn(table.getColumn());
            metaInfo.setExtInfo(table.getExtInfo());
            metaInfos.add(metaInfo);
        }
        return metaInfos;
    }

    @Override
    public long getCount(RedisConnectorInstance connectorInstance, MetaContext metaContext) {
        return 0;
    }

    @Override
    public Result reader(RedisConnectorInstance connectorInstance, ReaderContext context) {
        throw new RedisException("Redis does not support full reading");
    }

    @Override
    public Result writer(RedisConnectorInstance connectorInstance, PluginContext context) {
        List<Map> data = context.getTargetList();
        if (CollectionUtils.isEmpty(data)) {
            throw new RedisException("writer data can not be empty.");
        }

        Result result = new Result();
        final List<Field> pkFields = PrimaryKeyUtil.findExistPrimaryKeyFields(context.getTargetFields());
        String keyPrefix = context.getCommand().get(RedisConstant.KEY_PREFIX);
        String dataStructure = context.getCommand().get(RedisConstant.DATA_STRUCTURE);
        String keyJoiner = context.getCommand().get(RedisConstant.KEY_JOINER);
        String expireType = context.getCommand().get(RedisConstant.EXPIRE_TYPE);
        String expireSeconds = context.getCommand().get(RedisConstant.EXPIRE_SECONDS);
        String event = context.getEvent();
        Jedis jedis = null;
        try {
            jedis = connectorInstance.borrowJedis();
            for (Map row : data) {
                String redisKey = buildRedisKey(keyPrefix, keyJoiner, pkFields, row);
                if (StringUtil.equals(event, ConnectorConstant.OPERTION_DELETE)) {
                    jedis.del(redisKey);
                    continue;
                }
                String value = JsonUtil.objToJson(row);
                if (!StringUtil.equals(dataStructure, RedisConstant.DATA_TYPE_STRING)) {
                    throw new RedisException("Data structure not supported: " + dataStructure);
                }
                if (StringUtil.equals(expireType, RedisConstant.EXPIRE)) {
                    long ttlSeconds = NumberUtil.toLong(expireSeconds);
                    jedis.set(redisKey, value, SetParams.setParams().ex(ttlSeconds));
                } else {
                    jedis.set(redisKey, value);
                }
            }
            result.addSuccessData(data);
        } catch (Exception e) {
            result.addFailData(data);
            result.getError().append(e.getMessage()).append(System.lineSeparator());
            logger.error(e.getMessage(), e);
        } finally {
            RedisUtil.returnResource(connectorInstance.getConnection(), jedis);
        }
        return result;
    }

    private String buildRedisKey(String keyPrefix, String keyJoiner, List<Field> pkFields, Map row) {
        List<String> parts = pkFields.stream()
                .map(f -> String.valueOf(row.get(f.getName())))
                .collect(Collectors.toList());
        String joiner = StringUtil.isBlank(keyJoiner) ? StringUtil.COLON : keyJoiner;
        String key = StringUtil.join(parts, joiner);
        if (StringUtil.isBlank(keyPrefix)) {
            return key;
        }
        return keyPrefix + StringUtil.COLON + key;
    }

    @Override
    public Map<String, String> getSourceCommand(CommandConfig commandConfig) {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getTargetCommand(CommandConfig commandConfig) {
        Map<String, String> cmd = new HashMap<>();
        Table table = commandConfig.getTable();
        String keyPrefix = table.getExtInfo().getProperty(RedisConstant.KEY_PREFIX);
        if (StringUtil.isBlank(keyPrefix)) {
            keyPrefix = table.getName();
        }
        cmd.put(RedisConstant.KEY_PREFIX, keyPrefix);
        String dataStructure = table.getExtInfo().getProperty(RedisConstant.DATA_STRUCTURE);
        cmd.put(RedisConstant.DATA_STRUCTURE, StringUtil.isBlank(dataStructure) ? RedisConstant.DATA_TYPE_STRING : dataStructure);
        String keyJoiner = table.getExtInfo().getProperty(RedisConstant.KEY_JOINER);
        cmd.put(RedisConstant.KEY_JOINER, StringUtil.isBlank(keyJoiner) ? StringUtil.COLON : keyJoiner);
        cmd.put(RedisConstant.EXPIRE_TYPE, table.getExtInfo().getProperty(RedisConstant.EXPIRE_TYPE, RedisConstant.EXPIRE_NEVER));
        String expireSeconds = table.getExtInfo().getProperty(RedisConstant.EXPIRE_SECONDS);
        if (StringUtil.isNotBlank(expireSeconds)) {
            cmd.put(RedisConstant.EXPIRE_SECONDS, expireSeconds);
        }
        return cmd;
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isLog(listenerType)) {
            return new RedisListener();
        }
        return null;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }
}
