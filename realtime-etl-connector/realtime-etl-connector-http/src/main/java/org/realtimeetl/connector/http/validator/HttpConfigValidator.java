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
package org.realtimeetl.connector.http.validator;

import org.realtimeetl.common.util.JsonUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.http.HttpConnector;
import org.realtimeetl.connector.http.config.HttpConfig;
import org.realtimeetl.connector.http.constant.HttpConstant;
import org.realtimeetl.connector.http.util.HttpUtil;
import org.realtimeetl.sdk.connector.ConfigValidator;
import org.realtimeetl.sdk.model.Field;
import org.realtimeetl.sdk.model.Table;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * HttpConnectConfigureVerifyer Achieved
 *
 * @author Realtime ETL.
 * @version 1.0.0
 * @date 2026-02-02 00:01
 */
@Component
public class HttpConfigValidator implements ConfigValidator<HttpConnector, HttpConfig> {

    @Override
    public void modify(HttpConnector connectorService, HttpConfig connectorConfig, Map<String, String> params) {
        String url = params.get("url");
        String properties = params.get("properties");
        Assert.hasText(url, "url is empty.");
        Assert.hasText(properties, "properties is empty.");
        connectorConfig.setUrl(url);
        connectorConfig.setEnableEncrypt(isEnable(params.get("enableEncrypt")));
        connectorConfig.setPublicNetwork(isEnable(params.get("publicNetwork")));
        connectorConfig.setProperties(HttpUtil.parse(properties));
    }

    private boolean isEnable(Object key) {
        if (key != null) {
            String enabled = String.valueOf(key);
            return StringUtil.equals(enabled, "true") || StringUtil.equals(enabled, "on");
        }
        return false;
    }

    @Override
    public Table modifyExtendedTable(HttpConnector connectorService, Map<String, String> params) {
        Table table = new Table();
        String type = params.get("type");
        if ("source".equals(type)) {
            String extractPath = params.get(HttpConstant.EXTRACT_PATH);
            String extractTotal = params.get(HttpConstant.EXTRACT_TOTAL);
            Assert.hasText(extractPath, "Data parsing rules cannot be empty");
            Assert.hasText(extractTotal, "The number rule cannot be empty");
            table.getExtInfo().put(HttpConstant.EXTRACT_PATH, extractPath);
            table.getExtInfo().put(HttpConstant.EXTRACT_TOTAL, extractTotal);
        } else {
            String writePath = params.get(HttpConstant.WRITE_PATH);
            if (StringUtil.isNotBlank(writePath)) {
                table.getExtInfo().put(HttpConstant.WRITE_PATH, writePath);
            }
        }

        String tableName = params.get("tableName");
        String columnList = params.get("columnList");
        String method = params.get(HttpConstant.METHOD);
        String api = params.get(HttpConstant.API);
        String contentType = params.get(HttpConstant.CONTENT_TYPE);
        String requestParams = params.get(HttpConstant.PARAMS);
        Assert.hasText(tableName, "TableName is empty");
        Assert.hasText(columnList, "ColumnList is empty");
        Assert.hasText(method, "Request Mode is empty");
        Assert.hasText(api, "Interface cannot be empty");
        Assert.hasText(contentType, "ContentType can't be empty.");
        List<Field> fields = JsonUtil.jsonToArray(columnList, Field.class);
        Assert.notEmpty(fields, "Field cannot be empty.");
        table.setName(tableName);
        table.setColumn(fields);
        table.setType(connectorService.getExtendedTableType().getCode());
        table.getExtInfo().put(HttpConstant.METHOD, method);
        table.getExtInfo().put(HttpConstant.API, api);
        table.getExtInfo().put(HttpConstant.CONTENT_TYPE, contentType);
        if (StringUtil.isNotBlank(requestParams)) {
            table.getExtInfo().put(HttpConstant.PARAMS, requestParams);
        }
        return table;
    }

}