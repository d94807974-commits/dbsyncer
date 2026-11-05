/**
 * DBSyncer Copyright 2020-2024 All Rights Reserved.
 */
package org.realtimeetl.common.config;

import org.realtimeetl.common.util.StringUtil;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

/**
 * @author AE86
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
            copy.append("&copy;").append(LocalDate.now().getYear()).append(" Realtime ETL");
            if (StringUtil.isNotBlank(version)) {
                copy.append(" ").append(version);
            }
            copy.append(" · based on DBSyncer (Apache-2.0)");
            this.copyright = copy.toString();
        }
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}
