/**
 * DBSyncer Copyright 2020-2023 All Rights Reserved.
 */
package org.realtimeetl.connector.postgresql.decoder.impl;

import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.postgresql.PostgreSQLException;
import org.realtimeetl.connector.postgresql.decoder.AbstractMessageDecoder;
import org.realtimeetl.connector.postgresql.enums.MessageDecoderEnum;
import org.realtimeetl.connector.postgresql.enums.MessageTypeEnum;
import org.realtimeetl.sdk.connector.DefaultConnectorServiceContext;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.listener.event.RowChangedEvent;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.spi.ConnectorService;

import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author AE86
 * @Version 1.0.0
 * @Date 2022-04-10 22:36
 */
public class PgOutputMessageDecoder extends AbstractMessageDecoder {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final LocalDateTime PG_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0);

    /**
     * General TableandminSector EntitiesMain Table；SonminSectorOID Into the root.Table name，Easy to pressMain TableMap FilterIncremental events。
     */
    private static final String GET_TABLE_SCHEMA = "SELECT t.oid, COALESCE(root.relname, t.relname) AS tableName "
            + "FROM pg_class t "
            + "INNER JOIN pg_namespace n ON n.oid = t.relnamespace AND n.nspname = ? "
            + "LEFT JOIN LATERAL ("
            + "  WITH RECURSIVE ancestors AS ("
            + "    SELECT i.inhparent AS parent_oid, 1 AS depth FROM pg_inherits i WHERE i.inhrelid = t.oid "
            + "    UNION ALL "
            + "    SELECT i.inhparent, a.depth + 1 FROM ancestors a "
            + "    INNER JOIN pg_inherits i ON i.inhrelid = a.parent_oid"
            + "  ) "
            + "  SELECT c.relname FROM ancestors a "
            + "  INNER JOIN pg_class c ON c.oid = a.parent_oid "
            + "  WHERE COALESCE(c.relispartition, false) = false "
            + "  ORDER BY a.depth DESC LIMIT 1"
            + ") root ON TRUE "
            + "WHERE t.relkind IN ('r', 'p')";
    private static final Map<Integer, TableId> tables = new ConcurrentHashMap<>();
    private ConnectorService connectorService;
    private DatabaseConnectorInstance connectorInstance;

    @Override
    public void postProcessBeforeInitialization(ConnectorService connectorService, DatabaseConnectorInstance connectorInstance, String database) {
        this.connectorService = connectorService;
        this.connectorInstance = connectorInstance;
        this.database = database;
        initPublication();
        readSchema();
    }

    @Override
    public RowChangedEvent processMessage(ByteBuffer buffer) {
        if (!buffer.hasArray()) {
            throw new IllegalStateException("Invalid buffer received from PG server during streaming replication");
        }

        MessageTypeEnum type = MessageTypeEnum.getType((char) buffer.get());
        switch (type) {
            case UPDATE:
            case INSERT:
            case DELETE:
                return parseData(type, buffer);

            case BEGIN:
                long beginLsn = buffer.getLong();
                long beginTs = buffer.getLong();
                long xid = buffer.getInt();
                logger.info("Begin LSN {}, timestamp {}, xid {} - {}", beginLsn, PG_EPOCH.plusNanos(beginTs * 1000L), xid, beginTs);
                break;

            case COMMIT:
                buffer.get();
                long commitLsn = buffer.getLong();
                long commitEndLsn = buffer.getLong();
                long commitTs = buffer.getLong();
                logger.info("Commit: LSN {}, end LSN {}, ts {}", commitLsn, commitEndLsn, PG_EPOCH.plusNanos(commitTs * 1000L));
                break;

            default:
                logger.info("Type {} not implemented", type.name());
        }

        return null;
    }

    @Override
    public String getOutputPlugin() {
        return MessageDecoderEnum.PG_OUTPUT.getType();
    }

    @Override
    public void withSlotOption(ChainedLogicalStreamBuilder builder) {
        builder.withSlotOption("proto_version", 1);
        builder.withSlotOption("publication_names", getPubName());
    }

    private String getPubName() {
        return String.format("dbs_pub_%s_%s", schema, config.getUsername()).toLowerCase();
    }

    private void initPublication() {
        String pubName = getPubName();
        String selectPublication = String.format("SELECT COUNT(1) FROM pg_publication WHERE pubname = '%s'", pubName);
        Integer count = connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForObject(selectPublication, Integer.class));
        if (count != null && count > 0) {
            ensurePublishViaPartitionRoot(pubName);
            return;
        }

        logger.info("Creating new publication '{}' for plugin '{}'", pubName, getOutputPlugin());
        try {
            // PG13+：ChangeEventPressminSector EntitiesMain TableReport,andPressMain TableConfigureMaps Same
            String createPublication = String.format(
                    "CREATE PUBLICATION %s FOR ALL TABLES WITH (publish_via_partition_root = true)", pubName);
            logger.info("Creating Publication with statement '{}'", createPublication);
            connectorInstance.execute(databaseTemplate -> {
                databaseTemplate.execute(createPublication);
                return true;
            });
        } catch (Exception e) {
            logger.warn("Create publication with publish_via_partition_root failed, fallback without option: {}", e.getMessage());
            try {
                String createPublication = String.format("CREATE PUBLICATION %s FOR ALL TABLES", pubName);
                logger.info("Creating Publication with statement '{}'", createPublication);
                connectorInstance.execute(databaseTemplate -> {
                    databaseTemplate.execute(createPublication);
                    return true;
                });
            } catch (Exception ex) {
                throw new PostgreSQLException(ex.getCause() != null ? ex.getCause() : ex);
            }
        }
    }

    /**
     * Existingpublication Try.OnPressMain TableReporting;FailedOrganisationOID Son→Fatherhood can still be mapped together.
     */
    private void ensurePublishViaPartitionRoot(String pubName) {
        try {
            String query = String.format("SELECT pubviaroot FROM pg_publication WHERE pubname = '%s'", pubName);
            Boolean viaRoot = connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForObject(query, Boolean.class));
            if (Boolean.TRUE.equals(viaRoot)) {
                return;
            }
            String alter = String.format("ALTER PUBLICATION %s SET (publish_via_partition_root = true)", pubName);
            logger.info("Updating publication to publish via partition root: {}", alter);
            connectorInstance.execute(databaseTemplate -> {
                databaseTemplate.execute(alter);
                return true;
            });
        } catch (Exception e) {
            logger.warn("Unable to ensure publish_via_partition_root for '{}': {}", pubName, e.getMessage());
        }
    }

    private void readSchema() {
        List<Map> schemas = connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(GET_TABLE_SCHEMA, schema));
        if (CollectionUtils.isEmpty(schemas)) {
            return;
        }
        // Same logic.Table name（HamminSector EntitiesMain Table）Only take one column metadata to avoid a childminSectorOID Repeat.JDBC
        Map<String, List<Field>> columnsByTable = new HashMap<>();
        for (Map map : schemas) {
            Long oid = (Long) map.get("oid");
            String tableName = (String) map.get("tableName");
            if (oid == null || StringUtil.isBlank(tableName)) {
                continue;
            }
            List<Field> columns = columnsByTable.get(tableName);
            if (columns == null) {
                MetaInfo metaInfo = getMetaInfo(tableName);
                Assert.notEmpty(metaInfo.getColumn(), String.format("The table column for '%s' must not be empty.", tableName));
                columns = metaInfo.getColumn();
                columnsByTable.put(tableName, columns);
            }
            tables.put(oid.intValue(), new TableId(oid.intValue(), tableName, columns));
        }
    }

    private RowChangedEvent parseData(MessageTypeEnum type, ByteBuffer buffer) {
        final int relationId = buffer.getInt();
        final TableId tableId = tables.get(relationId);
        if (null != tableId) {
            // UPDATE The scene may contain old ones.Value(O)and newValue(N)，Needs to be combined
            if (type == MessageTypeEnum.UPDATE) {
                return parseUpdateData(tableId, buffer);
            }
            
            String tupleType = new String(new byte[]{buffer.get()}, 0, 1);
            switch (tupleType) {
                case "N":
                case "K":
                case "O":
                    List<Object> data = new ArrayList<>();
                    readTupleData(tableId, buffer, data);
                    return new RowChangedEvent(tableId.tableName, type.name(), data, null, null);

                default:
                    logger.info("N, K, O not set, got instead {}", tupleType);
            }
        }
        return null;
    }
    
    /**
     * ParsingUPDATE Data FourthlyValueMerge
     * PostgreSQL pgoutput Yes.REPLICA IDENTITY FULL Send in mode:
     * 1. "O" - AppealValueDivision(Optional)
     * 2. "N" - NewValueDivision
     * 
     * 新值中可能包含 TOASTED ("u") 字段，表示该字段未变更，需要从旧值中获取
     */
    private RowChangedEvent parseUpdateData(TableId tableId, ByteBuffer buffer) {
        List<Object> oldData = null;
        List<Object> newData = null;
        
        String firstTupleType = new String(new byte[]{buffer.get()}, 0, 1);
        
        // If the firstYesAppealValue "O"，Read old firstValue
        if ("O".equals(firstTupleType)) {
            oldData = new ArrayList<>();
            readTupleData(tableId, buffer, oldData);
            
            // Continue reading newValuePresidency
            if (buffer.hasRemaining()) {
                String secondTupleType = new String(new byte[]{buffer.get()}, 0, 1);
                if ("N".equals(secondTupleType)) {
                    newData = new ArrayList<>();
                    readTupleData(tableId, buffer, newData);
                }
            }
        } else if ("N".equals(firstTupleType)) {
            // Read directly newValue（No old ones.Valueof the Convention)
            newData = new ArrayList<>();
            readTupleData(tableId, buffer, newData);
        } else {
            // In principle, it's not an entry, but it's not an exception.LogChanged format, so I'm just trying to increase it.LogOutput to facilitate external perception
            logger.error("UPDATE: unexpected tuple type '{}', expected 'O' or 'N'", firstTupleType);
            return null;
        }
        
        // Merge old and newValue：If it's newValueYes.TOASTED Field，Use oldValueFill
        List<Object> mergedData = mergeUpdateData(oldData, newData);
        return new RowChangedEvent(tableId.tableName, MessageTypeEnum.UPDATE.name(), mergedData, null, null);
    }
    
    /**
     * MergeUPDATE Old and newValue
     * If it's newValuesomeFieldYes TOASTED（No change)ValueCorrespondingField
     */
    private List<Object> mergeUpdateData(List<Object> oldData, List<Object> newData) {
        if (newData == null) {
            return oldData != null ? oldData : new ArrayList<>();
        }
        
        if (oldData == null) {
            return newData;
        }
        
        // Merged data: newValueMediumTOASTED FieldOldValueReplace
        List<Object> merged = new ArrayList<>(newData.size());
        for (int i = 0; i < newData.size(); i++) {
            Object newValue = newData.get(i);
            if ("TOASTED".equals(newValue) && i < oldData.size()) {
                // Use oldValueFillTOASTED Field
                merged.add(oldData.get(i));
            } else {
                merged.add(newValue);
            }
        }
        return merged;
    }

    private void readTupleData(TableId tableId, ByteBuffer msg, List<Object> data) {
        short nColumn = msg.getShort();
        if (nColumn != tableId.fields.size()) {
            logger.warn("The column size of table '{}' is {}, but we has been received column size is {}.", tableId.tableName, tableId.fields.size(), nColumn);

            // The table schema has been changed, we should be get a new table schema from db.
            MetaInfo metaInfo = getMetaInfo(tableId.tableName);
            tableId.fields = metaInfo.getColumn();
            return;
        }

        for (int n = 0; n < nColumn; n++) {
            String type = new String(new byte[]{msg.get()}, 0, 1);
            switch (type) {
                case "t":
                    int size = msg.getInt();
                    byte[] text = new byte[size];
                    for (int z = 0; z < size; z++) {
                        text[z] = msg.get();
                    }
                    data.add(resolveValue(tableId.fields.get(n).getTypeName(), new String(text, 0, size)));
                    break;

                case "n":
                    data.add(null);
                    break;

                case "u":
                    data.add("TOASTED");
                    break;
                default:
                    logger.info("t, n, u not set, got instead {}", type);
            }
        }
    }

    private MetaInfo getMetaInfo(String tableName) {
        DefaultConnectorServiceContext context = new DefaultConnectorServiceContext();
        context.setCatalog(database);
        context.setSchema(schema);
        context.addTablePattern(tableName);
        List<MetaInfo> metaInfos = connectorService.getMetaInfo(connectorInstance, context);
        MetaInfo metaInfo = CollectionUtils.isEmpty(metaInfos) ? null : metaInfos.get(0);
        Assert.isTrue(metaInfo != null, String.format("The table '%s' is not exist in schema '%s'.", tableName, schema));
        // AddDetailsLog，Facilitating diagnosis of problems
        if (CollectionUtils.isEmpty(metaInfo.getColumn())) {
            logger.error("Table '{}.{}' has no columns. This may be caused by unsupported column types. ", schema, tableName);
            throw new IllegalArgumentException(
                    String.format("The table column for '%s.%s' must not be empty. Please check table structure and ensure all column types are supported.", schema, tableName));
        }
        return metaInfo;
    }

    final class TableId {

        Integer oid;
        String tableName;
        List<Field> fields;

        public TableId(Integer oid, String tableName, List<Field> fields) {
            this.oid = oid;
            this.tableName = tableName;
            this.fields = fields;
        }
    }

}