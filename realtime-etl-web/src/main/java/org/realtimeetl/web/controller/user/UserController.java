package org.realtimeetl.web.controller.user;

import org.realtimeetl.biz.PermissionService;
import org.realtimeetl.biz.RoleService;
import org.realtimeetl.biz.UserConfigService;
import org.realtimeetl.biz.vo.RestResult;
import org.realtimeetl.biz.vo.UserInfoVO;
import org.realtimeetl.common.util.StringUtil;
import org.realtimeetl.parser.LogService;
import org.realtimeetl.parser.LogType;
import org.realtimeetl.web.controller.BaseController;
import org.realtimeetl.web.log.OperLog;
import org.realtimeetl.web.log.OperLogType;
import org.realtimeetl.web.security.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * UserInfoManagement
 *
 * @author AE86
 * @ClassName: UserController
 * @date: 2017Year7Intern7Morning10:03:33
 */
@Controller
@RequestMapping(value = "/user")
public class UserController extends BaseController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private UserConfigService userConfigService;

    @Resource
    private PermissionService permissionService;

    @Resource
    private RoleService roleService;

    @Resource
    private LoginService loginService;

    @Resource
    private LogService logService;

    @RequestMapping("")
    public String index(ModelMap model) {
        model.put("currentUser", getUserInfoVo());
        List<UserInfoVO> users = userConfigService.getUserInfoAll(getUserName());
        if (users != null) {
            for (UserInfoVO u : users) {
                if (u != null) {
                    u.setLocked(loginService.isLocked(u.getUsername()));
                }
            }
        }
        model.put("users", users);
        return "user/list";
    }

    @GetMapping("/page/add")
    public String pageAdd(ModelMap model) {
        requirePermission("user:edit");
        model.put("roles", roleService.listRoles());
        return "user/add";
    }

    @GetMapping("/page/edit")
    public String pageEdit(ModelMap model, String username) {
        String currentUserName = getUserName();
        username = StringUtil.isBlank(username) ? currentUserName : username;
        model.put(UserConfigService.CURRENT_USER_NAME, currentUserName);
        model.put("currentUser", userConfigService.getUserInfoVo(currentUserName, username));
        model.put("roles", roleService.listRoles());
        model.put("canAssignRole", permissionService.hasPermission(currentUserName, "user:edit"));
        return "user/edit";
    }

    @GetMapping("/getUserInfo.json")
    @ResponseBody
    public RestResult getUserInfo() {
        return RestResult.restSuccess(getUserInfoVo());
    }

    @OperLog(type = OperLogType.USER_INSERT)
    @PostMapping(value = "/add")
    @ResponseBody
    public RestResult add(HttpServletRequest request) {
        try {
            Map<String, String> params = getParamsWithUserName(request);
            return RestResult.restSuccess(userConfigService.add(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.USER_UPDATE)
    @PostMapping(value = "/edit")
    @ResponseBody
    public RestResult edit(HttpServletRequest request) {
        try {
            Map<String, String> params = getParamsWithUserName(request);
            return RestResult.restSuccess(userConfigService.edit(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    @OperLog(type = OperLogType.USER_DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public RestResult remove(HttpServletRequest request) {
        try {
            Map<String, String> params = getParamsWithUserName(request);
            return RestResult.restSuccess(userConfigService.remove(params));
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    /**
     * Early release.Login failedLocked.
     */
    @OperLog(type = OperLogType.USER_UPDATE, message = "Unlock user")
    @PostMapping("/unlock")
    @ResponseBody
    public RestResult unlock(HttpServletRequest request) {
        try {
            requirePermission("user:edit");
            Map<String, String> params = getParamsWithUserName(request);
            String username = params.get("username");
            Assert.hasText(username, "Username cannot be empty");
            loginService.unlock(username);
            logService.log(LogType.UserLog.UPDATE,
                    String.format("[%s] Unlock account [%s]", getUserName(), username));
            return RestResult.restSuccess("Unlocked.");
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            return RestResult.restFail(e.getMessage());
        }
    }

    private Map<String, String> getParamsWithUserName(HttpServletRequest request) {
        Map<String, String> params = getParams(request);
        params.put(UserConfigService.CURRENT_USER_NAME, getUserName());
        return params;
    }

    /**
     * AccessLoginUserInfo（HamPermissionsCode for the front end.
     */
    private UserInfoVO getUserInfoVo() {
        String currentUserName = getUserName();
        UserInfoVO vo = userConfigService.getUserInfoVo(currentUserName, currentUserName);
        if (vo != null) {
            vo.setAdmin(permissionService.isAdmin(currentUserName));
            vo.setPermissions(new ArrayList<>(permissionService.listPermissionCodes(currentUserName)));
        }
        return vo;
    }

    private String getUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Assert.hasText(username, "Unable to get logged-in user.");
        return username;
    }

}
