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
package org.realtimeetl.parser;

import org.realtimeetl.parser.flush.impl.DefaultBufferActuatorRouter;
import org.realtimeetl.parser.flush.impl.TableGroupBufferActuator;
import org.realtimeetl.sdk.spi.BufferActuatorRouterService;
import org.realtimeetl.sdk.spi.ServiceFactory;
import org.realtimeetl.sdk.spi.TableGroupBufferActuatorService;
import org.realtimeetl.sdk.spi.ValidateSyncDetailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.annotation.Resource;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2024-01-25 23:43
 */
@Configuration
public class ParserSupportConfiguration {

    @Resource
    private ServiceFactory serviceFactory;

    @Bean
    @ConditionalOnMissingBean
    @DependsOn(value = "serviceFactory")
    public TableGroupBufferActuatorService tableGroupBufferActuatorService() {
        TableGroupBufferActuatorService service = serviceFactory.get(TableGroupBufferActuatorService.class);
        if (service != null) {
            return service;
        }
        return new TableGroupBufferActuator();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn(value = "serviceFactory")
    public BufferActuatorRouterService bufferActuatorRouterService() {
        BufferActuatorRouterService service = serviceFactory.get(BufferActuatorRouterService.class);
        if (service != null) {
            return service;
        }
        return new DefaultBufferActuatorRouter();
    }

    @Bean
    @ConditionalOnMissingBean
    @DependsOn(value = {"serviceFactory"})
    public ValidateSyncDetailService validateSyncerDetailService() {
        ValidateSyncDetailService service = serviceFactory.get(ValidateSyncDetailService.class);
        if (service != null) {
            return service;
        }
        return new ValidateSyncDetailService() {
        };
    }
}
