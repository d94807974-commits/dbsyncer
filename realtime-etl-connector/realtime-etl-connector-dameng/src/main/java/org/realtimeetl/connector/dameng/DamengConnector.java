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
package org.realtimeetl.connector.dameng;

import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.dameng.cdc.DamengListener;
import org.realtimeetl.connector.dameng.constant.DamengConstant;
import org.realtimeetl.connector.dameng.schema.DamengSchemaResolver;
import org.realtimeetl.connector.dameng.validator.DamengConfigValidator;
import org.realtimeetl.connector.oracle.OracleConnector;
import org.realtimeetl.sdk.SdkException;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.config.DatabaseConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.connector.database.DatabaseConnectorInstance;
import org.realtimeetl.sdk.connector.database.DatabaseTemplate;
import org.realtimeetl.sdk.connector.database.ds.SimpleConnection;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.listener.DatabaseQuartzListener;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.schema.BindParameter;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.util.PrimaryKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tamang.DM）Connector（CompatibilityOracle Syntax:
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-06-07 02:00
 */
public final class DamengConnector extends OracleConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(DamengConnector.class);

    /**
     * UseALL_USERS（NotDBA_USERS），NormalUserAnd...PermissionsView Schema List。
     */
    private static final String QUERY_SCHEMA = "SELECT USERNAME FROM ALL_USERS WHERE USERNAME NOT IN "
            + "('SYS','SYSSSO','SYSAUDITOR','SYSDBO','CTISYS') ORDER BY USERNAME";

    /**
     * Table of judgementYesNoHamIDENTITY AddSYSCOLUMNS.INFO2 bit0）。
     */
    private static final String QUERY_TABLE_IDENTITY =
            "SELECT COUNT(1) FROM SYSCOLUMNS COL, SYSOBJECTS TAB "
                    + "WHERE COL.ID = TAB.ID AND TAB.SUBTYPE$ = 'UTAB' AND BITAND(COL.INFO2, 1) = 1 AND TAB.NAME = ?";

    private static final String QUERY_TABLE_IDENTITY_WITH_SCHEMA =
            "SELECT COUNT(1) FROM SYSCOLUMNS COL, SYSOBJECTS TAB, SYSOBJECTS SCH "
                    + "WHERE COL.ID = TAB.ID AND TAB.SCHID = SCH.ID AND TAB.SUBTYPE$ = 'UTAB' "
                    + "AND BITAND(COL.INFO2, 1) = 1 AND TAB.NAME = ? AND SCH.NAME = ?";

    /**
     * getTargetCommand After packagingSQL：SET IDENTITY_INSERT ... ON;{merge};SET IDENTITY_INSERT ... OFF
     */
    private static final Pattern IDENTITY_WRAP = Pattern.compile(
            "^SET IDENTITY_INSERT (.+?) ON;(.+);SET IDENTITY_INSERT \\1 OFF$",
            Pattern.DOTALL);

    private final DamengConfigValidator configValidator = new DamengConfigValidator();
    private final DamengSchemaResolver schemaResolver = new DamengSchemaResolver();

    @Override
    public String getConnectorType() {
        return "Dameng";
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    @Override
    public ConnectorInstance connect(DatabaseConfig config, ConnectorServiceContext context) {
        DamengConstant.enrichJdbcProperties(config);
        // Correct historyError URL（I used to.LibraryName on the path.DriverYes.schema）
        config.setUrl(buildJdbcUrl(config, config.getDatabase()));
        return super.connect(config, context);
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isTiming(listenerType)) {
            return new DatabaseQuartzListener();
        }
        if (ListenerTypeEnum.isLog(listenerType)) {
            return new DamengListener();
        }
        return null;
    }

    @Override
    public List<String> getSchemas(DatabaseConnectorInstance connectorInstance, String catalog) {
        return connectorInstance.execute(databaseTemplate -> databaseTemplate.queryForList(QUERY_SCHEMA, String.class));
    }

    /**
     * Tam Revenue.JDBC URL Yes{@code jdbc:dm://host:port}；PathNameIt will.DriverConsider<strong>Modename</strong>，
     * Can't take an example.LibraryName (e.g.DAMENG）Spell it in. Default ModeandUsernameYes, please specifyConnection parameters {@code schema=}。
     */
    @Override
    public String buildJdbcUrl(DatabaseConfig config, String database) {
        return "jdbc:dm://" + config.getHost() + ":" + config.getPort();
    }

    /**
     * HamIDENTITY Since the addition,Full/OverwriteWriting needs temporary openingIDENTITY_INSERT，NoThen report--2723。
     */
    @Override
    public Map<String, String> getTargetCommand(CommandConfig commandConfig) {
        Map<String, String> targetCommand = super.getTargetCommand(commandConfig);
        if (!hasIdentityColumn(commandConfig)) {
            return targetCommand;
        }
        String qualified = qualifyIdentityTable(commandConfig.getSchema(), commandConfig.getTable().getName());
        wrapIdentityInsert(targetCommand, ConnectorConstant.OPERTION_INSERT, qualified);
        wrapIdentityInsert(targetCommand, ConnectorConstant.OPERTION_UPSERT, qualified);
        return targetCommand;
    }

    /**
     * Tam Revenue.MERGE SuccessOften.Back 0 or empty array, to be handled separatelySuccessCount.No, the interface progress is always as0。
     */
    @Override
    public Result writer(DatabaseConnectorInstance connectorInstance, PluginContext context) {
        String event = context.getEvent();
        List<Map> data = context.getTargetList();
        List<Field> targetFields = context.getTargetFields();

        if (CollectionUtils.isEmpty(targetFields)) {
            LOGGER.error("writer fields can not be empty.");
            throw new SdkException("writer fields can not be empty.");
        }
        if (CollectionUtils.isEmpty(data)) {
            LOGGER.error("writer data can not be empty.");
            throw new SdkException("writer data can not be empty.");
        }

        List<Field> fields = new ArrayList<>(targetFields);
        String executeSql = resolveWriterSql(context, event, fields);
        if (StringUtil.isBlank(executeSql)) {
            LOGGER.error("Event: }, executing SQL cannot be empty", event);
            throw new SdkException("Implementing SQL cannot be empty");
        }

        Result result = new Result();
        int[] execute = null;
        try {
            execute = connectorInstance.execute(databaseTemplate -> damengBatchUpdate(databaseTemplate, executeSql, fields, data));
        } catch (Exception e) {
            damengForceUpdate(connectorInstance, context, executeSql, fields, event, data, result);
        }
        collectWriteResult(execute, data, event, result);
        return result;
    }

    /**
     * MERGE INTO ... USING (SELECT ? FROM DUAL) At a time like this, Dream is great.FieldDefault PressVARCHAR I don't know.
     * RemarkableCAST，NoReport「NoneFrenchConvertDataType」。
     */
    @Override
    public boolean buildCustomValue(List<String> vs, Field field) {
        String type = DamengSchemaResolver.normalizeTypeName(field == null ? null : field.getTypeName());
        if (isClobFamily(type)) {
            vs.add("CAST(? AS CLOB)");
            return true;
        }
        if (isBlobFamily(type)) {
            vs.add("CAST(? AS BLOB)");
            return true;
        }
        return false;
    }

    /**
     * Use binary columnscreateBlob/setBytes，AvoidsetObject InferenceFailed。
     */
    @Override
    protected Object wrapBindParameter(Field field, Object val) {
        if (!(val instanceof byte[])) {
            return val;
        }
        final byte[] bytes = (byte[]) val;
        final String type = DamengSchemaResolver.normalizeTypeName(field == null ? null : field.getTypeName());
        if (isBlobFamily(type)) {
            return (BindParameter) (ps, paramIndex, connection) -> {
                if (bytes.length == 0) {
                    ps.setBytes(paramIndex, bytes);
                    return;
                }
                Blob blob = connection.createBlob();
                blob.setBytes(1, bytes);
                ps.setBlob(paramIndex, blob);
            };
        }
        return (BindParameter) (ps, paramIndex, connection) -> ps.setBytes(paramIndex, bytes);
    }

    private String resolveWriterSql(PluginContext context, String event, List<Field> fields) {
        if (isDelete(event)) {
            fields.clear();
            fields.addAll(PrimaryKeyUtil.findExistPrimaryKeyFields(context.getTargetFields()));
            return context.getCommand().get(event);
        }
        if (context.isForceUpdate()) {
            return context.getCommand().get(ConnectorConstant.OPERTION_UPSERT);
        }
        if (isUpdate(event)) {
            fields.addAll(PrimaryKeyUtil.findPrimaryKeyFields(fields));
            return context.getCommand().get(event);
        }
        return context.getCommand().get(event);
    }

    /**
     * Tam Revenue.PreparedStatement Supporting instability for multiple words willIDENTITY_INSERT and MERGE Open for execution.
     */
    private int[] damengBatchUpdate(DatabaseTemplate databaseTemplate, String executeSql, List<Field> fields, List<Map> data) throws Exception {
        Matcher matcher = IDENTITY_WRAP.matcher(StringUtil.trimToEmpty(executeSql));
        if (!matcher.matches()) {
            return damengBatchUpdateInternal(databaseTemplate, executeSql, fields, data);
        }
        String qualified = matcher.group(1);
        String mergeSql = matcher.group(2);
        String onSql = "SET IDENTITY_INSERT " + qualified + " ON";
        String offSql = "SET IDENTITY_INSERT " + qualified + " OFF";
        SimpleConnection connection = databaseTemplate.getSimpleConnection();
        try {
            connection.setAutoCommit(false);
            databaseTemplate.execute(onSql);
            int[] result = damengBatchUpdateInternal(databaseTemplate, mergeSql, fields, data);
            databaseTemplate.execute(offSql);
            connection.commit();
            return result;
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            try {
                databaseTemplate.execute(offSql);
            } catch (Exception ignore) {
                // The session will be automatically restored at the end.OFF
            }
            connection.setAutoCommit(true);
        }
    }

    private int[] damengBatchUpdateInternal(DatabaseTemplate databaseTemplate, String executeSql, List<Field> fields, List<Map> data) throws Exception {
        SimpleConnection connection = databaseTemplate.getSimpleConnection();
        try {
            connection.setAutoCommit(false);
            int[] result = databaseTemplate.batchUpdate(executeSql, batchRows(fields, data));
            connection.commit();
            return fillSuccessResult(result, data.size());
        } catch (Exception e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    /**
     * Tam Revenue.MERGE SuccessRegularBack 0；Multiple statementsbatch Maybe.BackEmpty array, pressSuccessDeal.
     */
    private static int[] fillSuccessResult(int[] result, int dataSize) {
        if (result == null || result.length == 0) {
            int[] filled = new int[dataSize];
            for (int i = 0; i < dataSize; i++) {
                filled[i] = -2;
            }
            return filled;
        }
        for (int i = 0; i < result.length; i++) {
            if (result[i] == 0) {
                result[i] = -2;
            }
        }
        return result;
    }

    private void collectWriteResult(int[] execute, List<Map> data, String event, Result result) {
        if (execute == null) {
            return;
        }
        int batchSize = Math.min(execute.length, data.size());
        for (int i = 0; i < batchSize; i++) {
            if (isDamengWriteSuccess(execute[i])) {
                result.getSuccessData().add(data.get(i));
                continue;
            }
            if (isInsert(event)) {
                result.getFailData().add(data.get(i));
                if (StringUtil.isBlank(result.getError())) {
                    result.getError().append("Possible 8.6 Sheet Data");
                }
            }
        }
    }

    private static boolean isDamengWriteSuccess(int affectedRows) {
        return affectedRows >= 0 || affectedRows == -2;
    }

    private void damengForceUpdate(DatabaseConnectorInstance connectorInstance, PluginContext context,
                                   String executeSql, List<Field> fields, String event, List<Map> data, Result result) {
        Matcher matcher = IDENTITY_WRAP.matcher(StringUtil.trimToEmpty(executeSql));
        if (!matcher.matches()) {
            forceUpdateRowByRow(connectorInstance, context, executeSql, fields, event, data, result);
            return;
        }
        String qualified = matcher.group(1);
        String mergeSql = matcher.group(2);
        String onSql = "SET IDENTITY_INSERT " + qualified + " ON";
        String offSql = "SET IDENTITY_INSERT " + qualified + " OFF";
        try {
            connectorInstance.execute(databaseTemplate -> {
                databaseTemplate.execute(onSql);
                return null;
            });
            forceUpdateRowByRow(connectorInstance, context, mergeSql, fields, event, data, result);
        } finally {
            try {
                connectorInstance.execute(databaseTemplate -> {
                    databaseTemplate.execute(offSql);
                    return null;
                });
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    private void forceUpdateRowByRow(DatabaseConnectorInstance connectorInstance, PluginContext context,
                                     String executeSql, List<Field> fields, String event, List<Map> data, Result result) {
        data.forEach(row -> {
            try {
                int affected = connectorInstance.execute(databaseTemplate -> {
                    List<Object[]> batch = batchRows(fields, Collections.singletonList(row));
                    return databaseTemplate.update(executeSql, batch.get(0));
                });
                if (!isDamengWriteSuccess(affected)) {
                    throw new SdkException("Data non-existent or performance abnormal");
                }
                result.getSuccessData().add(row);
            } catch (Exception e) {
                result.getFailData().add(row);
                result.getError().append(context.getTraceId()).append(" SQL:").append(executeSql)
                        .append(System.lineSeparator()).append("ERROR:").append(e.getMessage()).append(System.lineSeparator());
            }
        });
    }

    private boolean hasIdentityColumn(CommandConfig commandConfig) {
        String tableName = commandConfig.getTable() == null ? null : commandConfig.getTable().getName();
        if (StringUtil.isBlank(tableName)) {
            return false;
        }
        DatabaseConnectorInstance db = (DatabaseConnectorInstance) commandConfig.getConnectorInstance();
        if (db == null) {
            return false;
        }
        try {
            String schema = commandConfig.getSchema();
            Integer count = db.execute(databaseTemplate -> {
                if (StringUtil.isNotBlank(schema)) {
                    return databaseTemplate.queryForObject(QUERY_TABLE_IDENTITY_WITH_SCHEMA, Integer.class, tableName, schema);
                }
                return databaseTemplate.queryForObject(QUERY_TABLE_IDENTITY, Integer.class, tableName);
            });
            return count != null && count > 0;
        } catch (Exception e) {
            LOGGER.warn("Detect Dameng identity column failed, table={}: {}", tableName, e.getMessage());
            return false;
        }
    }

    private void wrapIdentityInsert(Map<String, String> targetCommand, String operation, String qualifiedTable) {
        String sql = targetCommand.get(operation);
        if (StringUtil.isBlank(sql)) {
            return;
        }
        targetCommand.put(operation, "SET IDENTITY_INSERT " + qualifiedTable + " ON;" + sql
                + ";SET IDENTITY_INSERT " + qualifiedTable + " OFF");
    }

    private String qualifyIdentityTable(String schema, String tableName) {
        if (StringUtil.isNotBlank(schema)) {
            return buildWithQuotation(schema) + "." + buildWithQuotation(tableName);
        }
        return buildWithQuotation(tableName);
    }

    private static boolean isClobFamily(String type) {
        if (StringUtil.isBlank(type)) {
            return false;
        }
        return type.contains("TEXT")
                || type.contains("CLOB")
                || "LONGVARCHAR".equals(type)
                || "LONG".equals(type)
                || "JSON".equals(type);
    }

    private static boolean isBlobFamily(String type) {
        if (StringUtil.isBlank(type)) {
            return false;
        }
        return type.contains("BLOB")
                || "IMAGE".equals(type)
                || "LONGVARBINARY".equals(type)
                || "LONG RAW".equals(type);
    }
}
