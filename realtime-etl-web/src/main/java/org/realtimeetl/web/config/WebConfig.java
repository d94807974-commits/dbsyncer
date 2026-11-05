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
package org.realtimeetl.web.config;

import org.realtimeetl.web.controller.openapi.OpenApiInterceptor;
import org.realtimeetl.web.log.OperLogInterceptor;

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
 * @author Realtime ETL.
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