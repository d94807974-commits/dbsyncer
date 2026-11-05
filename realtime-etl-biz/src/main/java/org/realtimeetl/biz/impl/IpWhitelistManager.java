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
package org.realtimeetl.biz.impl;

import org.realtimeetl.common.model.IpWhitelistConfig;
import org.realtimeetl.common.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * IP whitelistManager
 * ResponsibleIP whitelistCertification and management
 * Support individualIP、IPParagraphCIDR）andIPScope
 * 
 * @author Realtime ETL.
 * @version 1.0.0
 */
@Component
public class IpWhitelistManager {

    private static final Logger logger = LoggerFactory.getLogger(IpWhitelistManager.class);

    /**
     * AuthenticationIPYesNoIn the white list.
     * 
     * @param config CurrentConfigure
     * @param clientIp ClientIPAddress
     * @return YesNoIn the white list.
     */
    public boolean isAllowed(IpWhitelistConfig config, String clientIp) {
        try {
            if (StringUtil.isBlank(clientIp)) {
                logger.warn("Client IP is empty");
                return false;
            }

            if (config == null || !config.isEnabled()) {
                // If 212EnableWhite List, Default Allow AllIP
                return true;
            }

            // verifielocalhost（IfConfigureAllow 230
            if (config.isAllowLocalhost() && isLocalhost(clientIp)) {
                return true;
            }

            // Check the white list.
            List<String> whitelist = config.getWhitelist();
            if (whitelist == null || whitelist.isEmpty()) {
                logger.warn("The IP white list is empty, access denied");
                return false;
            }

            // Check the white list.YesNoMatch
            for (String ipPattern : whitelist) {
                if (StringUtil.isBlank(ipPattern)) {
                    continue;
                }

                if (matchesIpPattern(clientIp, ipPattern.trim())) {
                    logger.debug("IP {} Match white list rule: {", clientIp, ipPattern);
                    return true;
                }
            }

            logger.warn("IP{ Not on White List", clientIp);
            return false;
        } catch (Exception e) {
            logger.error("Forests", e);
            // AuthenticationFailedTime, for security reasons, denied access.
            return false;
        }
    }

    /**
     * InspectionIPYesNoMatch Rules
     * Support:
     * - SingleIP: 192.168.1.1
     * - CIDR: 192.168.1.0/24
     * - IPScope: 192.168.1.1-192.168.1.100
     * 
     * @param clientIp ClientIP
     * @param pattern IPRule
     * @return YesNoMatch
     */
    private boolean matchesIpPattern(String clientIp, String pattern) {
        try {
            // Prempt Match
            if (clientIp.equals(pattern)) {
                return true;
            }

            // CIDRFormat: 192.168.1.0/24
            if (pattern.contains("/")) {
                return matchesCidr(clientIp, pattern);
            }

            // IPRange Format: 192.168.1.1-192.168.1.100
            if (pattern.contains("-")) {
                return matchesIpRange(clientIp, pattern);
            }

            return false;
        } catch (Exception e) {
            logger.warn("Failed to match IP rule: } - {", clientIp, pattern, e);
            return false;
        }
    }

    /**
     * InspectionIPYesNoYes.CIDRScope
     * 
     * @param clientIp ClientIP
     * @param cidr CIDRInter alia,: 192.168.1.0/24
     * @return YesNoWithin Endosulfan
     */
    private boolean matchesCidr(String clientIp, String cidr) {
        try {
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                return false;
            }

            String networkIp = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);

            if (prefixLength < 0 || prefixLength > 32) {
                return false;
            }

            long clientIpLong = ipToLong(clientIp);
            long networkIpLong = ipToLong(networkIp);
            long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;

            return (clientIpLong & mask) == (networkIpLong & mask);
        } catch (Exception e) {
            logger.warn("CIDR matching failed:", clientIp, cidr, e);
            return false;
        }
    }

    /**
     * InspectionIPYesNoYes.IPScope
     * 
     * @param clientIp ClientIP
     * @param ipRange IPScope, if: 192.168.1.1-192.168.1.100
     * @return YesNoWithin Endosulfan
     */
    private boolean matchesIpRange(String clientIp, String ipRange) {
        try {
            String[] parts = ipRange.split("-");
            if (parts.length != 2) {
                return false;
            }

            String startIp = parts[0].trim();
            String endIp = parts[1].trim();

            long clientIpLong = ipToLong(clientIp);
            long startIpLong = ipToLong(startIp);
            long endIpLong = ipToLong(endIp);

            return clientIpLong >= startIpLong && clientIpLong <= endIpLong;
        } catch (Exception e) {
            logger.warn("IP range match failed: } - {", clientIp, ipRange, e);
            return false;
        }
    }

    /**
     * WillIPAddressConvertIt's long.
     * 
     * @param ip IPAddress
     * @return LongValue
     */
    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP address: " + ip);
        }

        long result = 0;
        for (int i = 0; i < 4; i++) {
            int part = Integer.parseInt(parts[i]);
            if (part < 0 || part > 255) {
                throw new IllegalArgumentException("Invalid IP address: " + ip);
            }
            result = (result << 8) + part;
        }
        return result;
    }

    /**
     * InspectionYesNoYeslocalhost
     * 
     * @param ip IPAddress
     * @return YesNoYeslocalhost
     */
    private boolean isLocalhost(String ip) {
        if (StringUtil.isBlank(ip)) {
            return false;
        }

        // Check the usual.localhostAddress
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip) || "localhost".equalsIgnoreCase(ip);
    }

}
