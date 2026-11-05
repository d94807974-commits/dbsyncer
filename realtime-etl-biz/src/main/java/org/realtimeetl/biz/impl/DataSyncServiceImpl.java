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
package org.realtimeetl.biz.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import org.realtimeetl.biz.DataSyncService;
import org.realtimeetl.biz.model.DataSyncEvent;
import org.realtimeetl.biz.model.DataSyncRequest;
import org.realtimeetl.biz.vo.BinlogColumnVO;
import org.realtimeetl.biz.vo.MessageVO;
import org.realtimeetl.common.binlog.proto.BinlogMap;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.DateFormatUtil;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.TableGroupProfile;
import org.realtimeetl.parser.TaskDetailProfile;
import org.realtimeetl.parser.TaskMetaProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.parser.model.Meta;
import org.realtimeetl.parser.model.Picker;
import org.realtimeetl.parser.model.TableGroup;
import org.realtimeetl.parser.util.ConnectorInstanceUtil;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.listener.event.RowChangedEvent;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.MetaIncrement;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.spi.BufferActuatorRouterService;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data syncServices
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2022/12/19 23:56
 */
@Service
public class DataSyncServiceImpl implements DataSyncService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private BufferActuatorRouterService bufferActuatorRouter;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private TaskMetaProfile taskMetaProfile;

    @Resource
    private TaskDetailProfile taskDetailProfile;

    @Resource
    private TableGroupProfile tableGroupProfile;

    @Resource
    private ConnectorFactory connectorFactory;

    @Override
    public MessageVO getMessageVo(String taskId, String messageId) {
        Assert.hasText(taskId, "The taskId is null.");
        Assert.hasText(messageId, "The messageId is null.");
        MessageVO messageVo = new MessageVO();
        try {
            Map row = taskDetailProfile.getData(taskId, messageId);
            Map binlogData = getBinlogData(row, true);
            String tableGroupId = (String) row.get(ConfigConstant.DATA_TABLE_GROUP_ID);
            TableGroup tableGroup = tableGroupProfile.getTableGroup(tableGroupId);
            messageVo.setSourceTableName(tableGroup.getSourceTable().getName());
            messageVo.setTargetTableName(tableGroup.getTargetTable().getName());
            messageVo.setId(messageId);

            if (!CollectionUtils.isEmpty(binlogData)) {
                Map<String, String> columnMap = tableGroup.getTargetTable().getColumn().stream().collect(Collectors.toMap(Field::getName, Field::getTypeName));
                List<BinlogColumnVO> columns = new ArrayList<>();
                binlogData.forEach((k, v) -> columns.add(new BinlogColumnVO((String) k, v, columnMap.get(k))));
                messageVo.setColumns(columns);
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
        return messageVo;
    }

    @Override
    public Map getBinlogData(Map row, boolean prettyBytes) throws InvalidProtocolBufferException {
        String tableGroupId = (String) row.get(ConfigConstant.DATA_TABLE_GROUP_ID);
        // 1、AccessConfiguration
        final TableGroup tableGroup = tableGroupProfile.getTableGroup(tableGroupId);
        if (tableGroup == null) {
            return Collections.EMPTY_MAP;
        }
        // 2、Access to recorded data
        byte[] bytes = (byte[]) row.get(ConfigConstant.BINLOG_DATA);
        if (null == bytes) {
            if (prettyBytes) {
                String json = (String) row.get(ConfigConstant.CONFIG_MODEL_JSON);
                return JsonUtil.parseMap(json);
            }
            return Collections.EMPTY_MAP;
        }
        // 3、AccessDDL
        Map<String, Object> target = new HashMap<>();
        BinlogMap message = BinlogMap.parseFrom(bytes);
        String event = (String) row.get(ConfigConstant.CONFIG_MODEL_TYPE);
        if (StringUtil.equals(event, ConnectorConstant.OPERTION_ALTER)) {
            message.getRowMap().forEach((k, v) -> target.put(k, v.toStringUtf8()));
            return target;
        }

        // 4、AccessConnectorServices
        Mapping mapping = mappingProfile.get(tableGroup.getTaskId());
        String targetInstanceId = ConnectorInstanceUtil.buildConnectorInstanceId(mapping.getId(), mapping.getTargetConnectorId(), ConnectorInstanceUtil.TARGET_SUFFIX);
        ConnectorInstance connectorInstance = connectorFactory.connect(targetInstanceId);
        ConnectorService sourceConnector = connectorFactory.getConnectorService(connectorInstance.getConfig());
        SchemaResolver schemaResolver = sourceConnector.getSchemaResolver();
        if (schemaResolver == null) {
            logger.warn("Inverse sequence failed and SchemaResolver failed");
            return target;
        }
        // 5、Case
        final Picker picker = new Picker(tableGroup);
        final Map<String, Field> fieldMap = picker.getTargetFieldMap();
        message.getRowMap().forEach((k, v) -> {
            if (fieldMap.containsKey(k)) {
                try {
                    Object val = schemaResolver.deserialize(v, fieldMap.get(k));
                    // Process binary object display
                    if (prettyBytes) {
                        if (val instanceof byte[]) {
                            byte[] b = (byte[]) val;
                            if (b.length > 128) {
                                target.put(k, String.format("byte[%d]", b.length));
                                return;
                            }
                            target.put(k, Arrays.toString(b));
                            return;
                        }
                    }
                    target.put(k, val);
                } catch (Exception e) {
                    logger.warn("Parsing Binlog data type anomalies: typeName=[}, valueType=[}, value=[}]", fieldMap.get(k).getTypeName(), (v == null ? null : v.getClass().getName()), v);
                }
            }
        });
        return target;
    }

    @Override
    public String sync(Map<String, String> params) throws InvalidProtocolBufferException {
        String messageId = params.get("messageId");
        String taskId = params.get("mappingId");
        Assert.hasText(messageId, "The messageId is null.");

        Map row = taskDetailProfile.getData(taskId, messageId);
        Map binlogData = getBinlogData(row, false);
        if (CollectionUtils.isEmpty(binlogData)) {
            return messageId;
        }
        String tableGroupId = (String) row.get(ConfigConstant.DATA_TABLE_GROUP_ID);
        String event = (String) row.get(ConfigConstant.CONFIG_MODEL_TYPE);
        // Yes.EditSyncValue
        String retryDataParams = params.get("retryDataParams");
        if (StringUtil.isNotBlank(retryDataParams)) {
            JsonUtil.parseMap(retryDataParams).forEach((k, v) -> binlogData.put(k, convertValue(binlogData.get(k), (String) v)));
        }
        TableGroup tableGroup = tableGroupProfile.getTableGroup(tableGroupId);
        String sourceTableName = tableGroup.getSourceTable().getName();

        // ConvertAs SourceField
        final Picker picker = new Picker(tableGroup);
        List<Object> changedRow = picker.pickSourceData(tableGroup.getSourceTable().getColumn(), binlogData);
        RowChangedEvent changedEvent = new RowChangedEvent(sourceTableName, event, changedRow, null, null);

        // Execute SyncYesNoSuccess
        bufferActuatorRouter.execute(taskId, changedEvent);
        // DetailsminTable: FromTasksminTable(realtime_etl_task_detail_{taskId})DeleteArticleSync data
        taskDetailProfile.delete(taskId, messageId);
        Meta meta = taskMetaProfile.getMeta(taskId);
        Assert.notNull(meta, "Meta can not be null.");
        // UpdateFailures：fail YesLibraryIt's the other way.IncrementalColumns, atom reduction(Meanwhile...Refresh updateTime)
        taskMetaProfile.incrementMeta(MetaIncrement.of(taskId).fail(-1L));
        return messageId;
    }

    @Override
    public void syncBatch(DataSyncRequest request) {
        Mapping mapping = mappingProfile.get(request.getMappingId());
        Assert.notNull(mapping, "Mapping can not be null.");
        TableGroup tableGroup = tableGroupProfile.getTableGroup(request.getTableGroupId());
        Assert.notNull(tableGroup, "Meta can not be null.");
        Meta meta = taskMetaProfile.getMeta(mapping.getId());
        Assert.notNull(meta, "Meta can not be null.");
        List<DataSyncEvent> dataList = request.getDataList();
        Assert.notEmpty(dataList, "DataList can not be null.");

        for (DataSyncEvent changedData : dataList) {
            String sourceTableName = tableGroup.getSourceTable().getName();
            RowChangedEvent changedEvent = new RowChangedEvent(sourceTableName, changedData.getEvent(), changedData.getData(), null, null);
            // Execute SyncYesNoSuccess
            bufferActuatorRouter.execute(meta.getTaskId(), changedEvent);
        }
    }

    private Object convertValue(Object oldValue, String newValue) {
        if (oldValue == null) {
            return newValue;
        }

        Object newVal;
        String type = oldValue.getClass().getName();
        switch (type) {
            case "java.sql.Date":
                newVal = DateFormatUtil.stringToDate(newValue);
                break;
            case "java.sql.Timestamp":
                newVal = DateFormatUtil.stringToTimestamp(newValue);
                break;
            case "java.lang.Integer":
            case "java.lang.Short":
                newVal = NumberUtil.toInt(newValue);
                break;
            case "java.lang.Long":
                newVal = NumberUtil.toLong(newValue);
                break;
            case "java.lang.Float":
                newVal = Float.valueOf(newValue);
                break;
            case "java.lang.Double":
                newVal = Double.valueOf(newValue);
                break;
            case "[B":
                newVal = stringToBytes(newValue);
                break;
            default:
                newVal = newValue;
        }

        return newVal;
    }

    private byte[] stringToBytes(String s) {
        byte[] b = null;
        if (s.startsWith("[") && s.endsWith("]")) {
            s = StringUtil.substring(s, 1, s.length() - 1);
            String[] split = StringUtil.split(s, StringUtil.COMMA);
            int length = split.length;
            b = new byte[length];
            for (int i = 0; i < length; i++) {
                b[i] = Byte.valueOf(split[i].trim());
            }
        }
        return b;
    }

}