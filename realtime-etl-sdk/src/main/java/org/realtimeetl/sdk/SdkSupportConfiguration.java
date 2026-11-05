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
package org.realtimeetl.sdk;

import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.sdk.enums.EditionEnum;
import org.realtimeetl.sdk.model.ProductInfo;
import org.realtimeetl.sdk.spi.LicenseService;
import org.realtimeetl.sdk.spi.ServiceFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.File;
import java.util.ServiceLoader;

/**
 * @Author Realtime ETL
 * @Version 1.0.0
 * @Date 2024-07-05 00:30
 */
@Configuration
public class SdkSupportConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LicenseService licenseService() {
        ServiceLoader<LicenseService> services = ServiceLoader.load(LicenseService.class, Thread.currentThread().getContextClassLoader());
        for (LicenseService s : services) {
            return s;
        }
        return new LicenseService() {

            @Override
            public EditionEnum getEditionEnum() {
                return EditionEnum.COMMUNITY;
            }

            @Override
            public String getLicensePath() {
                return System.getProperty("user.dir") + File.separatorChar + "conf" + File.separatorChar;
            }

            @Override
            public String getKey() {
                return StringUtil.EMPTY;
            }

            @Override
            public ProductInfo getProductInfo() {
                return null;
            }

            @Override
            public void updateLicense() {
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn(value = "licenseService")
    public ServiceFactory serviceFactory() {
        ServiceLoader<ServiceFactory> services = ServiceLoader.load(ServiceFactory.class, Thread.currentThread().getContextClassLoader());
        for (ServiceFactory s : services) {
            return s;
        }
        return new ServiceFactory() {

            @Override
            public <T> T get(Class<T> serviceClass) {
                return null;
            }
        };
    }
}
