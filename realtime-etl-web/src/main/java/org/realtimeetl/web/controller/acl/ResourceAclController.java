/**
 * DBSyncer Copyright 2020-2026 All Rights Reserved.
 */
package org.realtimeetl.web.controller.acl;

import org.realtimeetl.biz.ConnectorConfigService;
import org.realtimeetl.biz.ResourceAclService;
import org.realtimeetl.biz.RoleService;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.common.util.CollectionUtils;
import org.realtimeetl.parser.DatabaseSyncProfile;
import org.realtimeetl.parser.GroupProfile;
import org.realtimeetl.parser.MappingProfile;
import org.realtimeetl.parser.ValidateSyncProfile;
import org.realtimeetl.parser.model.Mapping;
import org.realtimeetl.sdk.constant.ConfigConstant;
import org.realtimeetl.sdk.model.DatabaseSyncTask;
import org.realtimeetl.sdk.model.ValidateSyncTask;
import org.realtimeetl.web.controller.BaseController;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Resource authorization（Sidebar「Resource authorization」）。
 *
 * @author wuji
 * @version 1.0.0
 * @date 2026-10-21
 */
@Controller
@RequestMapping("/acl")
public class ResourceAclController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ResourceAclService resourceAclService;

    @Resource
    private RoleService roleService;

    @Resource
    private GroupProfile groupProfile;

    @Resource
    private ConnectorConfigService connectorConfigService;

    @Resource
    private MappingProfile mappingProfile;

    @Resource
    private ValidateSyncProfile validateSyncProfile;

    @Resource
    private DatabaseSyncProfile databaseSyncProfile;

    @Resource
    private UserConfigService userConfigService;

    @RequestMapping("")
    public String index(ModelMap model) {
        requirePermission("acl:view");
        model.put("users", userConfigService.getUserInfoAll(currentUsername()));
        model.put("roles", roleService.listRoles());
        return "acl/list";
    }

    @PostMapping("/query")
    @ResponseBody
    public RestResult query(HttpServletRequest request) {
        try {
            requirePermission("acl:view");
            return RestResult.restSuccess(resourceAclService.query(getParams(request)));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @GetMapping("/page/add")
    public String pageAdd(ModelMap model) {
        requirePermission("acl:edit");
        fillOptions(model);
        return "acl/edit";
    }

    @GetMapping("/page/edit")
    public String pageEdit(ModelMap model, String id) {
        requirePermission("acl:view");
        model.put("acl", resourceAclService.get(id));
        fillOptions(model);
        return "acl/edit";
    }

    @OperLog(type = OperLogType.USER_UPDATE, message = "Authority to save resources")
    @PostMapping("/save")
    @ResponseBody
    public RestResult save(HttpServletRequest request) {
        try {
            requirePermission("acl:edit");
            Map<String, String> params = getParams(request);
            params.put(UserConfigService.CURRENT_USER_NAME, currentUsername());
            return RestResult.restSuccess(resourceAclService.save(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.USER_DELETE, message = "Delete Resource Authorization")
    @PostMapping("/remove")
    @ResponseBody
    public RestResult remove(HttpServletRequest request) {
        try {
            requirePermission("acl:edit");
            Map<String, String> params = getParams(request);
            params.put(UserConfigService.CURRENT_USER_NAME, currentUsername());
            return RestResult.restSuccess(resourceAclService.remove(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    private void fillOptions(ModelMap model) {
        model.put("roles", roleService.listRoles());
        model.put("groups", groupProfile.listGroups());
        model.put("users", userConfigService.getUserInfoAll(currentUsername()));
        model.put("connectors", connectorConfigService.getConnectorAll());
        model.put("mappings", listMappings());
        model.put("validateSyncTasks", listValidateSyncTasks());
        model.put("databaseSyncTasks", listDatabaseSyncTasks());
    }

    private List<Mapping> listMappings() {
        List<Mapping> list = new ArrayList<>();
        mappingProfile.pageScanTasks(ConfigConstant.PAGE_SIZE, page -> {
            if (!CollectionUtils.isEmpty(page)) {
                list.addAll(page);
            }
        });
        list.sort(Comparator.comparing(Mapping::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return list;
    }

    private List<ValidateSyncTask> listValidateSyncTasks() {
        List<ValidateSyncTask> list = new ArrayList<>();
        validateSyncProfile.pageScanTasks(ConfigConstant.PAGE_SIZE, page -> {
            if (!CollectionUtils.isEmpty(page)) {
                list.addAll(page);
            }
        });
        list.sort(Comparator.comparing(ValidateSyncTask::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return list;
    }

    private List<DatabaseSyncTask> listDatabaseSyncTasks() {
        List<DatabaseSyncTask> list = new ArrayList<>();
        databaseSyncProfile.pageScanTasks(ConfigConstant.PAGE_SIZE, page -> {
            if (!CollectionUtils.isEmpty(page)) {
                list.addAll(page);
            }
        });
        list.sort(Comparator.comparing(DatabaseSyncTask::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
        return list;
    }
}
