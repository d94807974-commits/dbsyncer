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
package org.realtimeetl.connector.http;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import org.realtimeetl.common.model.OpenApiData;
import org.realtimeetl.common.model.Result;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.http.cdc.HttpQuartzListener;
import org.realtimeetl.connector.http.config.HttpConfig;
import org.realtimeetl.connector.http.constant.HttpConstant;
import org.realtimeetl.connector.http.enums.ContentTypeEnum;
import org.realtimeetl.connector.http.enums.HttpMethodEnum;
import org.realtimeetl.connector.http.model.HttpResponse;
import org.realtimeetl.connector.http.model.RequestBuilder;
import org.realtimeetl.connector.http.schema.HttpSchemaResolver;
import org.realtimeetl.connector.http.util.HttpUtil;
import org.realtimeetl.connector.http.validator.HttpConfigValidator;
import org.realtimeetl.sdk.config.CommandConfig;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.connector.ConnectorInstance;
import org.realtimeetl.sdk.connector.ConnectorServiceContext;
import org.realtimeetl.sdk.constant.ConnectorConstant;
import org.realtimeetl.sdk.enums.ListenerTypeEnum;
import org.realtimeetl.sdk.enums.TableTypeEnum;
import org.realtimeetl.sdk.listener.Listener;
import org.realtimeetl.sdk.model.MetaInfo;
import org.realtimeetl.sdk.model.Table;
import org.realtimeetl.sdk.plugin.BaseContext;
import org.realtimeetl.sdk.plugin.MetaContext;
import org.realtimeetl.sdk.plugin.PluginContext;
import org.realtimeetl.sdk.plugin.ReaderContext;
import org.realtimeetl.sdk.schema.SchemaResolver;
import org.realtimeetl.sdk.spi.ConnectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * HttpConnectorAchieved
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-02-02 00:01
 */
public class HttpConnector implements ConnectorService<HttpConnectorInstance, HttpConfig> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HttpConfigValidator configValidator = new HttpConfigValidator();
    private final HttpSchemaResolver schemaResolver = new HttpSchemaResolver();

    @Override
    public String getConnectorType() {
        return "Http";
    }

    @Override
    public TableTypeEnum getExtendedTableType() {
        return TableTypeEnum.SEMI;
    }

    @Override
    public Class<HttpConfig> getConfigClass() {
        return HttpConfig.class;
    }

    @Override
    public ConnectorInstance connect(HttpConfig config, ConnectorServiceContext context) {
        return new HttpConnectorInstance(config);
    }

    @Override
    public ConfigValidator getConfigValidator() {
        return configValidator;
    }

    @Override
    public void disconnect(HttpConnectorInstance connectorInstance) {
        connectorInstance.close();
    }

    @Override
    public boolean isAlive(HttpConnectorInstance connectorInstance) {
        try {
            return true;
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new HttpException(e);
        }
    }

    @Override
    public List<String> getDatabases(HttpConnectorInstance connectorInstance) {
        return new ArrayList<>();
    }

    @Override
    public List<Table> getTable(HttpConnectorInstance connectorInstance, ConnectorServiceContext context) {
        return new ArrayList<>();
    }

    @Override
    public List<MetaInfo> getMetaInfo(HttpConnectorInstance connectorInstance, ConnectorServiceContext context) {
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
    public long getCount(HttpConnectorInstance connectorInstance, MetaContext context) {
        Table sourceTable = context.getSourceTable();
        Map<String, Object> params = getCountParams(sourceTable.getExtInfo());
        RequestBuilder builder = buildRequestBuilder(connectorInstance, context, sourceTable, params);
        HttpResponse execute = builder.execute();
        String data = execute.getBody();
        if (StringUtil.isNotBlank(data)) {
            data = decryptData(connectorInstance, context, data);
            Object rootObject = JSON.parse(data);
            if (rootObject != null) {
                String extractTotal = sourceTable.getExtInfo().getProperty(HttpConstant.EXTRACT_TOTAL);
                Object total = JSONPath.eval(rootObject, extractTotal);
                if (total instanceof Number) {
                    return ((Number) total).longValue();
                }
            }
        }
        return 0;
    }

    @Override
    public Result reader(HttpConnectorInstance connectorInstance, ReaderContext context) {
        Table sourceTable = context.getSourceTable();
        Map<String, Object> params = getParams(context);
        RequestBuilder builder = buildRequestBuilder(connectorInstance, context, sourceTable, params);
        HttpResponse execute = builder.execute();
        String data = execute.getBody();
        if (StringUtil.isNotBlank(data)) {
            data = decryptData(connectorInstance, context, data);
            Object rootObject = JSON.parse(data);
            if (rootObject != null) {
                String extractData = sourceTable.getExtInfo().getProperty(HttpConstant.EXTRACT_PATH);
                Object list = JSONPath.eval(rootObject, extractData);
                if (list instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) list;
                    return new Result(jsonArray.toList(Map.class));
                }
            }
        }
        return new Result();
    }

    @Override
    public Result writer(HttpConnectorInstance connectorInstance, PluginContext context) {
        List<Map> data = context.getTargetList();
        if (CollectionUtils.isEmpty(data)) {
            logger.error("writer data can not be empty.");
            throw new HttpException("writer data can not be empty.");
        }

        Result result = new Result();
        try {
            RequestBuilder builder = genRequestBuilder(connectorInstance, context.getTargetTable());
            // Parsing the requested template asJSON Object
            Object dataObj = encryptData(connectorInstance, context, data);
            builder.setBodyAsJsonString(JSON.toJSONString(dataObj));
            builder.execute();
            result.addSuccessData(data);
        } catch (Exception e) {
            // RecordsErrorData Fact
            result.addFailData(data);
            result.getError().append(e.getMessage()).append(System.lineSeparator());
            logger.error(e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, String> getSourceCommand(CommandConfig commandConfig) {
        Map<String, String> map = new HashMap<>();
        map.put(ConnectorConstant.OPERTION_QUERY, "HTTP");
        return map;
    }

    @Override
    public Map<String, String> getTargetCommand(CommandConfig commandConfig) {
        return new HashMap<>();
    }

    @Override
    public Listener getListener(String listenerType) {
        if (ListenerTypeEnum.isTiming(listenerType)) {
            return new HttpQuartzListener();
        }
        return null;
    }

    @Override
    public SchemaResolver getSchemaResolver() {
        return schemaResolver;
    }

    private RequestBuilder buildRequestBuilder(HttpConnectorInstance connectorInstance, BaseContext context, Table sourceTable, Map<String, Object> params) {
        if (connectorInstance.getConfig().isEnableEncrypt() && context.getRsaManager() != null && context.getRsaConfig() != null) {
            boolean publicNetwork = connectorInstance.getConfig().isPublicNetwork();
            OpenApiData dataObj = context.getRsaManager().encrypt(context.getRsaConfig(), params, publicNetwork);
            RequestBuilder builder = genRequestBuilder(connectorInstance, sourceTable);
            builder.setBodyAsJsonString(JSON.toJSONString(dataObj));
            builder.addHeader(HttpConstant.PUBLIC_NETWORK_HEADER, String.valueOf(publicNetwork));
            return builder;
        }

        String contentType = sourceTable.getExtInfo().getProperty(HttpConstant.CONTENT_TYPE);
        ContentTypeEnum contentTypeEnum = ContentTypeEnum.fromValue(contentType);
        Assert.notNull(contentTypeEnum, "content type can not be null");
        RequestBuilder builder = genRequestBuilder(connectorInstance, sourceTable);
        builder.setContentType(contentTypeEnum);
        if (contentTypeEnum == ContentTypeEnum.JSON) {
            builder.setBodyAsJson(params);
        } else {
            builder.addBodyParams(params);
        }
        return builder;
    }

    private String decryptData(HttpConnectorInstance connectorInstance, BaseContext context, String data) {
        if (connectorInstance.getConfig().isEnableEncrypt() && context.getRsaManager() != null && context.getRsaConfig() != null) {
            OpenApiData openApiData = JsonUtil.jsonToObj(data, OpenApiData.class);
            boolean publicNetwork = connectorInstance.getConfig().isPublicNetwork();
            return context.getRsaManager().decrypt(context.getRsaConfig(), openApiData, publicNetwork);
        }
        return data;
    }

    private Object encryptData(HttpConnectorInstance connectorInstance, PluginContext context, List<Map> data) {
        if (connectorInstance.getConfig().isEnableEncrypt() && context.getRsaManager() != null && context.getRsaConfig() != null) {
            boolean publicNetwork = connectorInstance.getConfig().isPublicNetwork();
            return context.getRsaManager().encrypt(context.getRsaConfig(), data, publicNetwork);
        }

        // []
        Table targetTable = context.getTargetTable();
        String params = targetTable.getExtInfo().getProperty(HttpConstant.PARAMS);
        if (StringUtil.isNotBlank(params)) {
            Object rootJSON = JSON.parse(params);
            // Willdata ConvertYesJSONArray
            JSONArray dataArray = JSON.parseArray(JSON.toJSONString(data));
            if (rootJSON instanceof JSONArray) {
                // Special handling: if templateYesArray
                ((JSONArray) rootJSON).clear();
                ((JSONArray) rootJSON).addAll(dataArray);
            } else {
                // General: useJSONPath.set Pass.writePath PathSettingsValue, Like$.data，For positioning requirementsReplaceLocation
                String writePath = targetTable.getExtInfo().getProperty(HttpConstant.WRITE_PATH);
                Assert.hasText(writePath, "writePath can not be empty.");
                JSONPath.set(rootJSON, writePath, dataArray);
            }
            return rootJSON;
        }
        return data;
    }

    private RequestBuilder genRequestBuilder(HttpConnectorInstance connectorInstance, Table sourceTable) {
        String serviceUrl = connectorInstance.getServiceUrl();
        String api = sourceTable.getExtInfo().getProperty(HttpConstant.API);
        String method = sourceTable.getExtInfo().getProperty(HttpConstant.METHOD);
        String url = serviceUrl;
        if (!url.endsWith("/")) {
            url += "/";
        }
        url += api != null && api.startsWith("/") ? api.substring(1) : (api != null ? api : "");
        HttpMethodEnum httpMethod = HttpMethodEnum.valueOf(method);

        Assert.notNull(httpMethod, "method can not be null");
        RequestBuilder builder = new RequestBuilder(connectorInstance.getConnection(), url, httpMethod);

        // AccessConnection parameters
        HttpConfig config = connectorInstance.getConfig();
        if (config != null && config.getProperties() != null) {
            Properties props = config.getProperties();
            int connectionTimeout = NumberUtil.toInt(props.getProperty(HttpConstant.CONNECTION_TIMEOUT_MS), 30000);
            int socketTimeout = NumberUtil.toInt(props.getProperty(HttpConstant.SOCKET_TIMEOUT_MS), 30000);
            int connectionRequestTimeout = NumberUtil.toInt(props.getProperty(HttpConstant.CONNECTION_REQUEST_TIMEOUT_MS), 30000);
            int retryTimes = NumberUtil.toInt(props.getProperty(HttpConstant.RETRY_TIMES), 0);
            String charset = props.getProperty(HttpConstant.CHARSET, "UTF-8");
            builder.setConnectionTimeout(connectionTimeout);
            builder.setSocketTimeout(socketTimeout);
            builder.setConnectionRequestTimeout(connectionRequestTimeout);
            builder.setRetryTimes(retryTimes);
            builder.setCharset(charset);
        }
        return builder;
    }

    /**
     * HarmonizationIncrementalParameterReplace。Fromcontext Yes.command Centre to read static variables (Time/This post is part of our special coverage Syria Protests 2011.
     * and pageIndex、pageSize、cursors Do a placeholder together.Replace。
     */
    private Map<String, Object> getParams(ReaderContext context) {
        Properties extInfo = context.getSourceTable().getExtInfo();
        String paramsTemplate = extInfo.getProperty(HttpConstant.PARAMS);
        if (StringUtil.isBlank(paramsTemplate)) {
            return new HashMap<>();
        }
        Properties template = HttpUtil.parse(paramsTemplate);
        Map<String, Object> varMap = new HashMap<>();
        varMap.put(HttpConstant.PAGE_INDEX, context.getPageIndex());
        varMap.put(HttpConstant.PAGE_SIZE, context.getPageSize());
        String staticVarsJson = context.getCommand() != null ? context.getCommand().get(HttpConstant.HTTP_INCREMENT_VARS) : null;
        if (StringUtil.isNotBlank(staticVarsJson)) {
            try {
                JSONObject jo = JSON.parseObject(staticVarsJson);
                if (jo != null) {
                    for (String k : jo.keySet()) {
                        varMap.put(k, jo.get(k));
                    }
                }
            } catch (Exception e) {
                logger.warn("Parse HTTP_INCREMENT_VARS failed: {}", e.getMessage());
            }
        }

        List<String> cursorKeys = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : template.entrySet()) {
            String key = (String) entry.getKey();
            String raw = (String) entry.getValue();
            String val = raw != null ? StringUtil.trim(raw) : "";
            if (HttpConstant.CURSOR.equals(val)) {
                cursorKeys.add(key);
                continue;
            }
            result.put(key, varMap.getOrDefault(val, val));
        }
        Object[] cursors = context.getCursors();
        if (!cursorKeys.isEmpty() && cursors != null) {
            for (int i = 0; i < cursorKeys.size() && i < cursors.length; i++) {
                result.put(cursorKeys.get(i), cursors[i]);
            }
        }
        return result;
    }

    /** ForgetCount Wait.None ReaderContext Site Use*/
    private Map<String, Object> getCountParams(Properties extInfo) {
        String paramsTemplate = extInfo.getProperty(HttpConstant.PARAMS);
        if (StringUtil.isBlank(paramsTemplate)) {
            return new HashMap<>();
        }
        Properties template = HttpUtil.parse(paramsTemplate);
        String pageIndexKey = "";
        String pageSizeKey = "";
        Map<String, Object> params = new HashMap<>();
        for (Map.Entry<Object, Object> entry : template.entrySet()) {
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            if (HttpConstant.PAGE_INDEX.equals(val)) {
                pageIndexKey = key;
                continue;
            }
            if (HttpConstant.PAGE_SIZE.equals(val)) {
                pageSizeKey = key;
                continue;
            }
            params.put(key, val);
        }
        if (StringUtil.isNotBlank(pageIndexKey)) {
            params.put(pageIndexKey, 1);
        }
        if (StringUtil.isNotBlank(pageSizeKey)) {
            params.put(pageSizeKey, 1);
        }
        return params;
    }
}