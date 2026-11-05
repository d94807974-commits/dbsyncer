/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.web.controller.openapi;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.realtimeetl.common.util.JsonUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenAPI Request for packing group
 * Provision of external system integration
 *
 * @author The clouds.
 * @version 2.0.0
 */
public final class DecryptRequestWrapper extends HttpServletRequestWrapper {

    /** After decryptionParameterMap */
    private final Map<String, String[]> parameterMap;

    /** Dec Introducing declassified data */
    private final String decryptedData;

    /** Cache Request Body*/
    private final byte[] cachedBody;

    /**
     * ConstructFunctions
     *
     * @param request Original request
     * @param decryptedData Dec Introducing declassified data
     */
    public DecryptRequestWrapper(HttpServletRequest request, String decryptedData) {
        super(request);
        this.decryptedData = decryptedData;

        // Declassified dataConvertAs byte array (useUTF-8Encoding)
        if (decryptedData != null) {
            this.cachedBody = decryptedData.getBytes(StandardCharsets.UTF_8);
        } else {
            this.cachedBody = new byte[0];
        }
        // DecryptedJSONData,ConvertYesRequest parameters
        this.parameterMap = parseJsonToParameters(decryptedData);
    }

    /**
     * RewritegetInputStreamMethodology
     * BackInput stream of declassified data
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(cachedBody);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };
    }

    /**
     * RewritegetReaderMethodology
     * BackDecrypted DataReader
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Get declassified raw data
     *
     * @return Dec Introducing declassified data
     */
    public String getDecryptedData() {
        return decryptedData;
    }

    /**
     * Request bytes to get cache Group
     *
     * @return Request bytes
     */
    public byte[] getCachedBody() {
        return cachedBody;
    }

    /**
     * Get RequestLength
     */
    @Override
    public int getContentLength() {
        return cachedBody.length;
    }

    /**
     * Get RequestLength（longType）
     */
    @Override
    public long getContentLengthLong() {
        return cachedBody.length;
    }

    /**
     * AccessContent-Type
     * Keep originalContent-Type
     */
    @Override
    public String getContentType() {
        // If Decrypt DataYesJSON format，Yeah.SettingsYesapplication/json
        // NoKeep the original.Content-Type
        return "application/json;charset=UTF-8";
    }

    /**
     * Request string to get the cache (UTF-8Encoding)
     * For reading original requests before decryption Body
     *
     * @return Request Body String
     */
    public String getCachedBodyAsUTF8() {
        return new String(cachedBody, StandardCharsets.UTF_8);
    }

    /**
     * ParsingJSONData isRequest parameters
     */
    private Map<String, String[]> parseJsonToParameters(String json) {
        Map<String, String[]> params = new HashMap<>();

        JSONObject jsonObject = JSONObject.parseObject(json);

        // ThroughJSONAllField，ConvertYesRequest parameters
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);

            if (value == null) {
                continue;
            }

            // Based onValueYes.TypeProcessing
            if (value instanceof JSONArray) {
                // ArrayType
                JSONArray jsonArray = (JSONArray) value;
                String[] values = new String[jsonArray.size()];
                for (int i = 0; i < jsonArray.size(); i++) {
                    values[i] = jsonArray.getString(i);
                }
                params.put(key, values);

            } else if (value instanceof Map) {
                // ObjectType，ConvertYesJSONString
                String jsonValue = JsonUtil.objToJson(value);
                params.put(key, new String[]{jsonValue});

            } else {
                // BasicType
                String strValue = String.valueOf(value);
                params.put(key, new String[]{strValue});
            }
        }
        return params;
    }

    /**
     * RewritegetParameterMap
     * Merge OriginalParameterReconciliationParameter
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> originalParams = super.getParameterMap();

        Map<String, String[]> mergedParams = new HashMap<>();

        // FirstAddOriginalParameter
        if (originalParams != null) {
            mergedParams.putAll(originalParams);
        }

        // Again.AddDecryptParameter（Overwrite3) Original by/112Parameter）
        mergedParams.putAll(parameterMap);

        return mergedParams;
    }

    /**
     * RewritegetParameter
     */
    @Override
    public String getParameter(String name) {
        // First from decryption.ParameterCenter Find
        String[] values = parameterMap.get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }

        // No APPROACH.ParameterCenter Find
        return super.getParameter(name);
    }

    /**
     * RewritegetParameterValues
     */
    @Override
    public String[] getParameterValues(String name) {
        // First from decryption.ParameterCenter Find
        String[] values = parameterMap.get(name);
        if (values != null && values.length > 0) {
            return values;
        }

        // No APPROACH.ParameterCenter Find
        return super.getParameterValues(name);
    }

    /**
     * RewritegetParameterNames
     */
    @Override
    public Enumeration<String> getParameterNames() {
        Map<String, String[]> mergedMap = getParameterMap();
        return Collections.enumeration(mergedMap.keySet());
    }
}
