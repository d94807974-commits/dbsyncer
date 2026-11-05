/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
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
 * @author AE86
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
