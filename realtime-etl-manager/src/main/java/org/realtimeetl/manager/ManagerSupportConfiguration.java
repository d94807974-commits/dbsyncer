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
package org.realtimeetl.manager;

import org.realtimeetl.manager.deployment.StandaloneService;
import org.realtimeetl.parser.impl.FullFlushServiceImpl;
import org.realtimeetl.sdk.spi.ClusterService;
import org.realtimeetl.sdk.spi.FullFlushService;
import org.realtimeetl.sdk.spi.ServiceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2023-11-19 23:29
 */
@Configuration
public class ManagerSupportConfiguration {

    @Resource
    private ServiceFactory serviceFactory;

    @Bean
    @ConditionalOnMissingBean(ClusterService.class)
    @DependsOn(value = "serviceFactory")
    public ClusterService clusterService() {
        ClusterService spi = serviceFactory.get(ClusterService.class);
        if (spi != null) {
            return spi;
        }
        return new StandaloneService();
    }

    /**
     * FullPageLevelLibrary：PrioritySPI，No.
     */
    @Bean
    @ConditionalOnMissingBean(FullFlushService.class)
    @DependsOn(value = "serviceFactory")
    public FullFlushService fullFlushService() {
        FullFlushService spi = serviceFactory.get(FullFlushService.class);
        if (spi != null) {
            return spi;
        }
        return new FullFlushServiceImpl();
    }

}
