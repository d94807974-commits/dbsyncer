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

import org.apache.catalina.connector.Connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2025/10/23 18:30
 */
@Configuration
@ConditionalOnProperty(name = {"server.ssl.enabled", "server.http.enabled"}, havingValue = "true")
public class HttpRedirectConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Value("${server.port:18686}")
    private int httpsPort;

    @Value("${server.http.port:8080}")
    private int httpPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        logger.info("Configuring HTTP on port: {}", httpPort);
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);
        factory.addAdditionalTomcatConnectors(connector);
    }

    // ConfigureFilter sequence to ensure implementation before security filter
    @Bean
    @ConditionalOnProperty(value = "server.http.redirect", havingValue = "true")
    public FilterRegistrationBean<Filter> httpsRedirectFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HttpsRedirectFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1); // High Priority
        return registrationBean;
    }

    // Redirect filter achieved
    public class HttpsRedirectFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // IfYesHTTPRequest andPortYesHTTPPort，Redirect toHTTPS
            if (!httpRequest.isSecure() && httpRequest.getLocalPort() == httpPort) {
                StringBuffer url = httpRequest.getRequestURL();
                String queryString = httpRequest.getQueryString();

                // ICALLYHTTPS URL
                String httpsUrl = url.toString().replaceFirst("http://", "https://").replace(":" + httpPort, ":" + httpsPort);

                if (queryString != null) {
                    httpsUrl += "?" + queryString;
                }

                httpResponse.sendRedirect(httpsUrl);
                return;
            }

            chain.doFilter(request, response);
        }
    }
}