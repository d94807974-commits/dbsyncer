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
package org.realtimeetl.storage;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.connector.base.ConnectorFactory;
import org.realtimeetl.connector.h2.storage.H2StorageService;
import org.realtimeetl.sdk.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Properties;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2021/11/18 21:36
 */
@Configuration
public class StorageSupportConfiguration {

    private final String PREFIX_STORAGE = "realtime-etl.storage";

    private final String STORAGE_TYPE = "realtime-etl.storage.type";

    @Resource
    private Environment environment;

    @Resource
    private ConnectorFactory connectorFactory;

    @Bean
    @ConditionalOnMissingBean
    public StorageService storageService() {
        Properties properties = new Properties();
        if (environment instanceof AbstractEnvironment) {
            AbstractEnvironment ae = (AbstractEnvironment) environment;
            MutablePropertySources propertySources = ae.getPropertySources();
            for (PropertySource<?> propertySource : propertySources) {
                boolean applicationConfig = propertySource.getName().contains("application");
                if (!applicationConfig) {
                    continue;
                }
                Map<String, OriginTrackedValue> props = (Map<String, OriginTrackedValue>) propertySource.getSource();
                props.forEach((k, v)-> {
                    if (StringUtil.startsWith(k, PREFIX_STORAGE)) {
                        properties.put(k, v.getValue());
                    }
                });
            }
        }

        // Specified storageType
        String storageType = properties.getProperty(STORAGE_TYPE);
        if (StringUtil.isNotBlank(storageType)) {
            String connectorType = getConnectorType(storageType);
            if (StringUtil.isNotBlank(connectorType)) {
                StorageService storageService = connectorFactory.getConnectorService(connectorType).getStorageService();
                if (storageService != null) {
                    storageService.init(properties);
                    return storageService;
                }
            }
        }

        // DefaultH2Storage
        H2StorageService storageService = new H2StorageService();
        storageService.init(properties);
        return storageService;
    }

    private String getConnectorType(String storageType) {
        for (String connectorType : connectorFactory.getConnectorTypeAll()) {
            if (StringUtil.equalsIgnoreCase(storageType, connectorType)) {
                return connectorType;
            }
        }
        return null;
    }

}