/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
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
 * @author AE86
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
