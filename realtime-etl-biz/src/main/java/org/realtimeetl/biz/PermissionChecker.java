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
package org.realtimeetl.biz;

import org.realtimeetl.common.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * Functional PermissionsClaimService / Controller TotalUse it.
 *
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2026-10-21
 */
@Component
public class PermissionChecker {

    @Resource
    private PermissionService permissionService;

    public void requirePermission(String username, String permissionCode) {
        Assert.hasText(username, "Unable to get logged-in user.");
        Assert.isTrue(permissionService.hasPermission(username, permissionCode), "Permission denied");
    }

    /**
     * Have anyPermissionsCode COUNCILBusiness groupCrossModuleManagement).
     */
    public void requireAnyPermission(String username, String... permissionCodes) {
        Assert.hasText(username, "Unable to get logged-in user.");
        Assert.isTrue(permissionCodes != null && permissionCodes.length > 0, "Permission numbers cannot be empty");
        for (String code : permissionCodes) {
            if (StringUtil.isNotBlank(code) && permissionService.hasPermission(username, code)) {
                return;
            }
        }
        Assert.isTrue(false, "Permission denied");
    }
}
