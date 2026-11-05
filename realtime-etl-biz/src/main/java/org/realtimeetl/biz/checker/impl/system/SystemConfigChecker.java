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
package org.realtimeetl.biz.checker.impl.system;

import org.realtimeetl.biz.checker.AbstractChecker;
import org.realtimeetl.biz.impl.ApiKeyManager;
import org.realtimeetl.common.model.ApiKeyConfig;
import org.realtimeetl.common.model.IpWhitelistConfig;
import org.realtimeetl.common.model.RsaConfig;
import org.realtimeetl.common.rsa.RsaManager;
import org.realtimeetl.common.util.BeanUtil;
import org.realtimeetl.common.util.NumberUtil;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.SystemConfigProfile;
import org.realtimeetl.parser.model.SystemConfig;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/1/8 15:17
 */
@Component
public class SystemConfigChecker extends AbstractChecker {

    @Resource
    private SystemConfigProfile systemConfigProfile;

    @Resource
    private ApiKeyManager apiKeyManager;

    @Resource
    private RsaManager rsaManager;

    @Override
    public SystemConfig checkAddConfigModel(Map<String, String> params) {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setName("System settings");

        // UpdateBasicConfigure
        this.modifyConfigModel(systemConfig, params);

        systemConfigProfile.saveSystemConfig(systemConfig);
        return systemConfig;
    }

    @Override
    public SystemConfig checkEditConfigModel(Map<String, String> params) {
        printParams(params);
        Assert.notEmpty(params, "Config check params is null.");
        Map<String, Object> newParams = new HashMap<>();
        newParams.putAll(params);
        newParams.put("enableStorageWriteSuccess", StringUtil.isNotBlank(params.get("enableStorageWriteSuccess")));
        newParams.put("enableStorageWriteFail", StringUtil.isNotBlank(params.get("enableStorageWriteFail")));
        newParams.put("enableStorageWriteFull", StringUtil.isNotBlank(params.get("enableStorageWriteFull")));
        newParams.put("enableWatermark", StringUtil.isNotBlank(params.get("enableWatermark")));
        newParams.put("enablePrintTraceInfo", StringUtil.isNotBlank(params.get("enablePrintTraceInfo")));
        newParams.put("enableOpenAPI", StringUtil.isNotBlank(params.get("enableOpenAPI")));
        String watermark = params.get("watermark");
        if (StringUtil.isNotBlank(watermark)) {
            Assert.isTrue(watermark.length() <= 64, "Allow watermarks to enter up to 64 words.");
        }
        params.put("watermark", watermark);

        SystemConfig systemConfig = systemConfigProfile.getSystemConfig();
        SystemConfig newConfig = systemConfig.clone();
        BeanUtil.mapToBean(newParams, newConfig);
        Assert.isTrue(newConfig.getLoginLockMinutes() >= 1 && newConfig.getLoginLockMinutes() <= 1440,
                "Login lock-in support range [1-1440] minutes");
        // Update API KeyConfigure
        saveApiKeyConfig(newConfig, params);
        // UpdateRSAConfigure
        saveRsaConfig(newConfig, params);
        // Update IP White list.Configure
        saveIpWhitelistConfig(newConfig, params);

        // UpdateBasicConfigure
        this.modifyConfigModel(newConfig, params);
        return newConfig;
    }

    private void saveApiKeyConfig(SystemConfig systemConfig, Map<String, String> params) {
        if (!systemConfig.isEnableOpenAPI()) {
            return;
        }
        String apiSecret = params.get("apiSecret");
        Assert.hasText(apiSecret, "API key cannot be empty");
        ApiKeyConfig apiKeyConfig = apiKeyManager.addCredential(systemConfig.getApiKeyConfig(), apiSecret);
        systemConfig.setApiKeyConfig(apiKeyConfig);
    }

    private void saveIpWhitelistConfig(SystemConfig systemConfig, Map<String, String> params) {
        if (!systemConfig.isEnableOpenAPI()) {
            return;
        }
        IpWhitelistConfig config = new IpWhitelistConfig();
        config.setEnabled(StringUtil.isNotBlank(params.get("ipWhitelistEnabled")));
        config.setAllowLocalhost(StringUtil.isNotBlank(params.get("ipWhitelistAllowLocalhost")));
        String ipWhitelist = params.get("ipWhitelist");
        if (StringUtil.isNotBlank(ipWhitelist)) {
            List<String> list = Arrays.stream(ipWhitelist.split("[,\n\r]+")).map(String::trim).filter(s->!s.isEmpty()).distinct().collect(Collectors.toList());
            config.setWhitelist(list);
        }
        systemConfig.setIpWhitelistConfig(config);
    }

    private void saveRsaConfig(SystemConfig systemConfig, Map<String, String> params) {
        if (!systemConfig.isEnableOpenAPI()) {
            return;
        }
        String publicKey = params.get("rsaPublicKey");
        String privateKey = params.get("rsaPrivateKey");
        String rsaKeyLength = params.get("rsaKeyLength");
        Assert.hasText(publicKey, "RSA public key cannot be empty");
        Assert.hasText(privateKey, "RSA private key cannot be empty");
        Assert.hasText(rsaKeyLength, "Key length cannot be empty");
        int keyLength = NumberUtil.toInt(rsaKeyLength);
        Assert.isTrue(keyLength >= 1024 && keyLength <= 8192, "Scope of key length support [1024-8192]");
        RsaConfig rsaConfig = rsaManager.addCredential(systemConfig.getRsaConfig(), publicKey, privateKey, keyLength);
        systemConfig.setRsaConfig(rsaConfig);
    }

}