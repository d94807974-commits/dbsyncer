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
package org.realtimeetl.common.config;

import org.realtimeetl.common.util.StringUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2022/5/18 0:04
 */
@Configuration
@ConfigurationProperties(prefix = "info.app")
public class AppConfig {

    private String name = "Realtime ETL";

    private String version;

    private String copyright;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCopyright() {
        if (StringUtil.isBlank(copyright)) {
            StringBuilder copy = new StringBuilder();
            copy.append("&copy;2026 Realtime ETL");
            if (StringUtil.isNotBlank(version)) {
                copy.append(" ").append(version);
            }
            this.copyright = copy.toString();
        }
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}
