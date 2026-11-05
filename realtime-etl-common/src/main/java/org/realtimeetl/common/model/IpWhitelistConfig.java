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
package org.realtimeetl.common.model;

import java.util.ArrayList;
import java.util.List;

/**
 * IP whitelistConfigure
 * Support individualIPandIPParagraphCIDR）Configure
 *
 * @author Realtime ETL.
 * @version 1.0.0
 */
public class IpWhitelistConfig {

    /**
     * EnableIP whitelist
     */
    private boolean enabled = false;

    /**
     * IP whitelistList
     * Support format:
     * - SingleIP: 192.168.1.1
     * - IPParagraphCIDR）: 192.168.1.0/24
     * - IPScope: 192.168.1.1-192.168.1.100
     */
    private List<String> whitelist;

    /**
     * YesNoAllow IntranetIP（127.0.0.1, localhostWait.
     */
    private boolean allowLocalhost = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getWhitelist() {
        if (whitelist == null) {
            whitelist = new ArrayList<>();
        }
        return whitelist;
    }

    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }

    /**
     * AddIPTo White List
     *
     * @param ip IPAddress orIPParagraph
     */
    public void addIp(String ip) {
        if (whitelist == null) {
            whitelist = new ArrayList<>();
        }
        if (ip != null && !ip.trim().isEmpty() && !whitelist.contains(ip.trim())) {
            whitelist.add(ip.trim());
        }
    }

    /**
     * RemoveIP
     *
     * @param ip IPAddress orIPParagraph
     */
    public void removeIp(String ip) {
        if (whitelist != null) {
            whitelist.remove(ip);
        }
    }

    public boolean isAllowLocalhost() {
        return allowLocalhost;
    }

    public void setAllowLocalhost(boolean allowLocalhost) {
        this.allowLocalhost = allowLocalhost;
    }
}
