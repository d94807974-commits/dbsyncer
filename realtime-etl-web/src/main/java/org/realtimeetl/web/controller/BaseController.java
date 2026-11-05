package org.realtimeetl.web.controller;

import org.realtimeetl.biz.ConditionService;
import org.realtimeetl.biz.PermissionChecker;
import org.realtimeetl.biz.PluginService;
import org.realtimeetl.biz.ResourceAclService;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.vo.EditionInfoVO;
import org.realtimeetl.parser.enums.ConvertEnum;
import org.realtimeetl.sdk.spi.LicenseService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Realtime ETL
 * @version 1.0.0
 * @date 2020/1/7 22:46
 */
public abstract class BaseController {

    @Resource
    private ConditionService filterService;

    @Resource
    private PluginService pluginService;

    @Resource
    private LicenseService licenseService;

    @Resource
    private PermissionChecker permissionChecker;

    @Resource
    private ResourceAclService resourceAclService;

    /**
     * CurrentLoginUsername。
     */
    protected String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Assert.notNull(authentication, "Unable to get logged-in user.");
        String username = authentication.getName();
        Assert.hasText(username, "Unable to get logged-in user.");
        return username;
    }

    /**
     * Claim CurrentUserExistingPermissionscode.
     */
    protected void requirePermission(String permissionCode) {
        permissionChecker.requirePermission(currentUsername(), permissionCode);
    }

    /**
     * Claim CurrentUserHave anyPermissionscode.
     */
    protected void requireAnyPermission(String... permissionCodes) {
        permissionChecker.requireAnyPermission(currentUsername(), permissionCodes);
    }

    /**
     * Claim CurrentUserYeah.ResourceAction availablePermissions。
     */
    protected void requireResource(String resourceType, String resourceId, String action) {
        resourceAclService.require(currentUsername(), resourceType, resourceId, action);
    }

    /**
     * AccessRequest parameters（Attach CurrentLoginUsername，ForService LayerACL Filter.
     */
    protected Map<String, String> getParams(HttpServletRequest request) {
        Map<String, String[]> map = request.getParameterMap();
        Map<String, String> res = new HashMap<>();
        map.forEach((k, v)->res.put(k, v[0]));
        res.put(UserConfigService.CURRENT_USER_NAME, currentUsername());
        return res;
    }

    /**
     * Initialize: Condition/Convert/Plugin（PluginLoad only installedList，Without AllLibraryAssociation scan)
     */
    protected void initConfig(ModelMap model) {
        model.put("condition", filterService.getCondition());
        model.put("convert", Arrays.asList(ConvertEnum.values()));
        model.put("plugin", pluginService.listPlugins());
    }

    /**
     * Initialize: Condition
     */
    protected void initConditionConfig(ModelMap model) {
        model.put("condition", filterService.getCondition());
    }

    /**
     * Read Request
     */
    protected String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    protected void initEditionInfo(ModelMap model) {
        EditionInfoVO editionInfo = new EditionInfoVO();
        editionInfo.setEdition(licenseService.getEditionEnum().getCode());
        editionInfo.setEditionName(licenseService.getEditionEnum().getMessage());
        model.put("editionInfo", editionInfo);
    }
}
