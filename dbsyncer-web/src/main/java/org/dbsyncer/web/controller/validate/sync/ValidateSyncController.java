/**
 * DBSyncer Copyright 2020-2025 All Rights Reserved.
 */
package org.dbsyncer.web.controller.validate.sync;

import org.dbsyncer.biz.enums.ResourceAclActionEnum;
import org.dbsyncer.biz.TableGroupService;
import org.dbsyncer.biz.ValidateSyncService;
import org.dbsyncer.biz.vo.RestResult;
import org.dbsyncer.biz.vo.ValidateSyncTaskVO;
import org.dbsyncer.common.util.StringUtil;
import org.dbsyncer.parser.model.TableGroup;
import org.dbsyncer.sdk.constant.ConfigConstant;
import org.dbsyncer.web.controller.BaseController;
import org.dbsyncer.web.log.OperLog;
import org.dbsyncer.web.log.OperLogType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Data validationManagement
 *
 * @author wuji
 * @version 1.0.0
 * @date 2025-10-18 19:52
 */
@Controller
@RequestMapping("/validate-sync")
public class ValidateSyncController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ValidateSyncService validateSyncService;

    @Resource
    private TableGroupService tableGroupService;

    /**
     * TasksConfigureFirst page
     */
    @RequestMapping("/list")
    public String index(ModelMap model) {
        initEditionInfo(model);
        return "validate-sync/list";
    }

    /**
     * AddTasksPageNoodles.
     */
    @GetMapping("/pageAdd")
    public String pageAdd(ModelMap model) {
        return "validate-sync/add";
    }

    /**
     * UpdateTasksPageNoodles.
     */
    @GetMapping("/page/{page}")
    public String pageEdit(ModelMap model, @PathVariable("page") String page, @RequestParam("id") String id,
                           @RequestParam(value = "detailStatus", required = false) String detailStatus) {
        if (StringUtil.equals("detail", page)) {
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.READ.getCode());
            model.put("taskId", id);
            ValidateSyncTaskVO detailTask = validateSyncService.get(id);
            model.put("taskName", detailTask != null ? detailTask.getName() : id);
            model.put("detailStatus", detailStatus == null ? "" : detailStatus.trim());
        } else if (StringUtil.equals("editTableGroup", page)) {
            TableGroup tableGroup = tableGroupService.getTableGroup(id);
            requireResource(ConfigConstant.VALIDATE_SYNC, tableGroup.getTaskId(), ResourceAclActionEnum.READ.getCode());
            model.put("tableGroup", tableGroup);
            model.put("task", validateSyncService.get(tableGroup.getTaskId()));
        } else {
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.READ.getCode());
            model.put("task", validateSyncService.get(id));
        }
        initConditionConfig(model);
        return "validate-sync/" + page;
    }

    /**
     * AddTasks
     */
    @OperLog(type = OperLogType.MAPPING_INSERT, message = "Add Revised Validation")
    @PostMapping("/add")
    @ResponseBody
    public RestResult add(HttpServletRequest request) {
        try {
            requirePermission("mapping:edit");
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(validateSyncService.add(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * UpdateTasks
     */
    @OperLog(type = OperLogType.MAPPING_UPDATE, message = "Modify Revised Validation")
    @PostMapping("/edit")
    @ResponseBody
    public RestResult edit(HttpServletRequest request) {
        try {
            requirePermission("mapping:edit");
            Map<String, String> params = getParams(request);
            requireResource(ConfigConstant.VALIDATE_SYNC, params.get(ConfigConstant.CONFIG_MODEL_ID), ResourceAclActionEnum.WRITE.getCode());
            return RestResult.restSuccess(validateSyncService.edit(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.MAPPING_COPY, message = "Copy Revised Validation")
    @PostMapping("/copy")
    @ResponseBody
    public RestResult add(@RequestParam("id") String id) {
        try {
            requirePermission("mapping:edit");
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.WRITE.getCode());
            return RestResult.restSuccess(validateSyncService.copy(id, currentUsername()));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * DeleteTasks
     */
    @OperLog(type = OperLogType.MAPPING_DELETE, message = "Delete Revised Validation")
    @PostMapping("/remove")
    @ResponseBody
    public RestResult remove(@RequestParam(value = "id") String id) {
        try {
            requirePermission("mapping:edit");
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.WRITE.getCode());
            return RestResult.restSuccess(validateSyncService.delete(id));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * StartTasks
     */
    @OperLog(type = OperLogType.MAPPING_RUNNING, message = "Credentials modified.")
    @PostMapping("/start")
    @ResponseBody
    public RestResult start(@RequestParam(value = "id") String id) {
        try {
            requirePermission("mapping:operate");
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.OPERATE.getCode());
            return RestResult.restSuccess(validateSyncService.start(id));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * StopTasks
     */
    @OperLog(type = OperLogType.MAPPING_STOP, message = "Stop Revision Verification")
    @PostMapping("/stop")
    @ResponseBody
    public RestResult stop(@RequestParam(value = "id") String id) {
        try {
            requirePermission("mapping:operate");
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.OPERATE.getCode());
            return RestResult.restSuccess(validateSyncService.stop(id));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/search")
    @ResponseBody
    public RestResult list(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            return RestResult.restSuccess(validateSyncService.search(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/searchTableGroup")
    @ResponseBody
    public RestResult searchTableGroup(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            requireResource(ConfigConstant.VALIDATE_SYNC, params.get(ConfigConstant.CONFIG_MODEL_ID), ResourceAclActionEnum.READ.getCode());
            return RestResult.restSuccess(validateSyncService.searchTableGroup(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping("/refreshTables")
    @ResponseBody
    public RestResult refreshTables(@RequestParam(value = "id") String id) {
        try {
            requirePermission("mapping:edit");
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.WRITE.getCode());
            return RestResult.restSuccess(validateSyncService.refreshTables(id));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * minPageSearchVerify INTRODUCTIONTasksTable (for bottom frame remote)Search）
     */
    @PostMapping("/searchTables")
    @ResponseBody
    public RestResult searchTables(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            requireResource(ConfigConstant.VALIDATE_SYNC, params.get(ConfigConstant.CONFIG_MODEL_ID), ResourceAclActionEnum.READ.getCode());
            return RestResult.restSuccess(validateSyncService.searchTables(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @PostMapping(value = "/refreshFields")
    @ResponseBody
    public RestResult refreshFields(@RequestParam(value = "id") String id) {
        try {
            requirePermission("mapping:edit");
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.WRITE.getCode());
            return RestResult.restSuccess(validateSyncService.refreshFields(id));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.TABLE_GROUP_INSERT)
    @PostMapping("/addTableGroup")
    @ResponseBody
    public RestResult addTableGroup(HttpServletRequest request) {
        try {
            requirePermission("mapping:edit");
            Map<String, String> params = getParams(request);
            requireResource(ConfigConstant.VALIDATE_SYNC, params.get("taskId"), ResourceAclActionEnum.WRITE.getCode());
            return RestResult.restSuccess(validateSyncService.addTableGroup(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.TABLE_GROUP_UPDATE)
    @PostMapping("/editTableGroup")
    @ResponseBody
    public RestResult editTableGroup(HttpServletRequest request) {
        try {
            requirePermission("mapping:edit");
            Map<String, String> params = getParams(request);
            TableGroup tableGroup = tableGroupService.getTableGroup(params.get(ConfigConstant.CONFIG_MODEL_ID));
            requireResource(ConfigConstant.VALIDATE_SYNC, tableGroup.getTaskId(), ResourceAclActionEnum.WRITE.getCode());
            return RestResult.restSuccess(validateSyncService.editTableGroup(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.TABLE_GROUP_DELETE)
    @PostMapping("/removeTableGroup")
    @ResponseBody
    public RestResult removeTableGroup(
            @RequestParam(value = "taskId", required = false) String taskId,
            @RequestParam(value = "ids") String ids) {
        try {
            requirePermission("mapping:edit");
            requireResource(ConfigConstant.VALIDATE_SYNC, taskId, ResourceAclActionEnum.WRITE.getCode());
            return RestResult.restSuccess(validateSyncService.removeTableGroup(taskId, ids));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * ViewImplementation results
     */
    @PostMapping("/result")
    @ResponseBody
    public RestResult result(@RequestParam(value = "id") String id) {
        try {
            requireResource(ConfigConstant.VALIDATE_SYNC, id, ResourceAclActionEnum.READ.getCode());
            return RestResult.restSuccess(validateSyncService.result(id));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * minPageQuery VerificationResultDetails
     */
    @PostMapping("/searchResult")
    @ResponseBody
    public RestResult searchResult(HttpServletRequest request) {
        try {
            Map<String, String> params = getParams(request);
            requireResource(ConfigConstant.VALIDATE_SYNC, params.get("taskId"), ResourceAclActionEnum.READ.getCode());
            return RestResult.restSuccess(validateSyncService.searchResult(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * By DetailsID Query Single VerifyResult（Contains fullCONTENT），ForDetailsBlast windows are used Thereafter.
     */
    @PostMapping("/getResultDetail")
    @ResponseBody
    public RestResult getResultDetail(@RequestParam("taskId") String taskId, @RequestParam("id") String id) {
        try {
            requireResource(ConfigConstant.VALIDATE_SYNC, taskId, ResourceAclActionEnum.READ.getCode());
            Object detail = validateSyncService.getValidateResultDetail(taskId, id);
            if (detail == null) {
                return RestResult.restFail("Record does not exist", 404);
            }
            return RestResult.restSuccess(detail);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * ManualRevisedNot yet specifiedSuccessYes.DiffsAccording to.
     */
    @PostMapping("/manualRevise")
    @ResponseBody
    public RestResult manualRevise(@RequestParam("taskId") String taskId, @RequestParam("id") String id) {
        try {
            requirePermission("mapping:operate");
            requireResource(ConfigConstant.VALIDATE_SYNC, taskId, ResourceAclActionEnum.OPERATE.getCode());
            return RestResult.restSuccess(validateSyncService.manualReviseDetail(taskId, id));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

}
