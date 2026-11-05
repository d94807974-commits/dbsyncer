/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.dbsyncer.biz;

import org.dbsyncer.common.util.StringUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * Functional PermissionsClaimService / Controller TotalUse it.
 *
 * @author wuji
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
