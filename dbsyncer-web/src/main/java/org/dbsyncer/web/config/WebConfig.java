/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.web.config;

import org.dbsyncer.web.controller.openapi.OpenApiInterceptor;
import org.dbsyncer.web.log.OperLogInterceptor;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import javax.servlet.MultipartConfigElement;

/**
 * WebConfigure
 * 
 * @author The clouds.
 * @version 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private OpenApiInterceptor openApiInterceptor;

    @Resource
    private OperLogInterceptor operLogInterceptor;

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // UploadSingleDocumentationMaxValueMB
        DataSize maxSize = DataSize.ofMegabytes(128);
        DataSize requestMaxSize = DataSize.ofMegabytes(128);
        factory.setMaxFileSize(maxSize);
        // SettingsOnce.UploadDocumentationTotal Size
        factory.setMaxRequestSize(requestMaxSize);
        return factory.createMultipartConfig();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // RegistrationOpenAPIInterceptor, Interceptor/openapi/**Path
        registry.addInterceptor(openApiInterceptor).addPathPatterns("/openapi/**");
        // Operation logs：Handled only on@OperLog Yes.Interface
        registry.addInterceptor(operLogInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/plugins/**", "/openapi/**", "/login", "/login.html");
    }
}